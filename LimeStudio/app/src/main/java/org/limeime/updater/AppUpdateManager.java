package org.limeime.updater;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;

import androidx.core.content.FileProvider;
import androidx.core.content.pm.PackageInfoCompat;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Coordinates in-app update checks, background APK downloading, and installation intents.
 */
public class AppUpdateManager {

    private static final String TAG = "AppUpdateManager";
    public static final String GITHUB_RELEASE_API =
            "https://api.github.com/repos/boboidvtw/limeime/releases/latest";

    private static final String PREF_NAME = "lime_app_update_prefs";
    private static final String KEY_LAST_CHECK_TIME = "last_check_time";
    private static final long AUTO_CHECK_INTERVAL_MS = 24L * 60 * 60 * 1000; // 24 hours

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

    public interface UpdateCheckCallback {
        void onResult(UpdateCheckResult result);
    }

    public interface DownloadProgressListener {
        void onProgress(long bytesDownloaded, long totalBytes, int percent);
    }

    public interface DownloadResultCallback {
        void onSuccess(File apkFile);
        void onError(String errorCode, String message);
    }

    /**
     * Checks for available updates from GitHub Releases.
     *
     * @param context Context
     * @param isManualCheck If true, forces remote query; if false, throttles to once every 24 hours.
     * @param callback Result callback executed on the main UI thread.
     */
    public static void checkUpdate(Context context, boolean isManualCheck, UpdateCheckCallback callback) {
        if (context == null || callback == null) return;

        final Context appContext = context.getApplicationContext();
        final SharedPreferences prefs = appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        long lastCheck = prefs.getLong(KEY_LAST_CHECK_TIME, 0);
        long now = System.currentTimeMillis();

        if (!isManualCheck && (now - lastCheck) < AUTO_CHECK_INTERVAL_MS) {
            // Throttled: skip auto background query
            return;
        }

        EXECUTOR.execute(() -> {
            UpdateCheckResult result;
            try {
                PackageInfo pInfo = appContext.getPackageManager().getPackageInfo(appContext.getPackageName(), 0);
                long localVersionCode = PackageInfoCompat.getLongVersionCode(pInfo);
                String localVersionName = pInfo.versionName != null ? pInfo.versionName : "";

                HttpURLConnection conn = openConnectionWithRedirects(GITHUB_RELEASE_API, "LIME-IME-Android/" + localVersionName);
                conn.setRequestProperty("Accept", "application/vnd.github.v3+json");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(15000);

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    String json = readStreamToString(conn.getInputStream());
                    result = ReleaseJsonParser.parse(json, localVersionCode, localVersionName);
                    if (result.isSuccess()) {
                        prefs.edit().putLong(KEY_LAST_CHECK_TIME, now).apply();
                    }
                } else if (responseCode == 403) {
                    result = UpdateCheckResult.error(UpdateCheckResult.CODE_ERROR_RATE_LIMIT, "GitHub API rate limit reached (HTTP 403)");
                } else {
                    result = UpdateCheckResult.error(UpdateCheckResult.CODE_ERROR_NETWORK, "HTTP " + responseCode);
                }
                conn.disconnect();
            } catch (Exception e) {
                Log.w(TAG, "Update check failed", e);
                result = UpdateCheckResult.error(UpdateCheckResult.CODE_ERROR_NETWORK, e.getMessage());
            }

            final UpdateCheckResult finalResult = result;
            MAIN_HANDLER.post(() -> callback.onResult(finalResult));
        });
    }

    /**
     * Downloads the APK file in the background with progress reporting.
     */
    public static AtomicBoolean downloadApk(Context context, ReleaseInfo info,
                                            DownloadProgressListener progressListener,
                                            DownloadResultCallback resultCallback) {
        final AtomicBoolean isCancelled = new AtomicBoolean(false);
        final Context appContext = context.getApplicationContext();

        EXECUTOR.execute(() -> {
            File targetDir = new File(appContext.getExternalCacheDir(), "updates");
            if (!targetDir.exists()) {
                targetDir.mkdirs();
            }

            File tempFile = new File(targetDir, info.getApkFileName() + ".download");
            File finalFile = new File(targetDir, info.getApkFileName());

            InputStream in = null;
            OutputStream out = null;
            HttpURLConnection conn = null;

            try {
                conn = openConnectionWithRedirects(info.getApkDownloadUrl(), "LIME-IME-Android-Downloader");
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(30000);

                long totalBytes = conn.getContentLengthLong();
                if (totalBytes <= 0 && info.getApkSize() > 0) {
                    totalBytes = info.getApkSize();
                }

                in = new BufferedInputStream(conn.getInputStream());
                out = new FileOutputStream(tempFile);

                byte[] buffer = new byte[8192];
                long downloadedBytes = 0;
                int bytesRead;
                long lastProgressTime = 0;

                while ((bytesRead = in.read(buffer)) != -1) {
                    if (isCancelled.get()) {
                        tempFile.delete();
                        return;
                    }
                    out.write(buffer, 0, bytesRead);
                    downloadedBytes += bytesRead;

                    long now = System.currentTimeMillis();
                    if (now - lastProgressTime > 100 || downloadedBytes == totalBytes) {
                        lastProgressTime = now;
                        final long current = downloadedBytes;
                        final long total = totalBytes;
                        final int percent = total > 0 ? (int) ((current * 100) / total) : 0;
                        if (progressListener != null) {
                            MAIN_HANDLER.post(() -> progressListener.onProgress(current, total, percent));
                        }
                    }
                }

                out.flush();
                out.close();
                out = null;
                in.close();
                in = null;

                if (finalFile.exists()) {
                    finalFile.delete();
                }
                if (!tempFile.renameTo(finalFile)) {
                    throw new IllegalStateException("Failed to finalize downloaded APK file");
                }

                MAIN_HANDLER.post(() -> {
                    if (resultCallback != null) {
                        resultCallback.onSuccess(finalFile);
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "APK download failed", e);
                if (tempFile.exists()) {
                    tempFile.delete();
                }
                MAIN_HANDLER.post(() -> {
                    if (resultCallback != null) {
                        resultCallback.onError("DOWNLOAD_FAILED", e.getMessage());
                    }
                });
            } finally {
                try {
                    if (out != null) out.close();
                } catch (Exception ignored) {}
                try {
                    if (in != null) in.close();
                } catch (Exception ignored) {}
                if (conn != null) conn.disconnect();
            }
        });

        return isCancelled;
    }

    /**
     * Checks install permissions and triggers system package installation for the downloaded APK.
     *
     * @return true if installation Intent was launched; false if user was redirected to grant permission.
     */
    public static boolean installApk(Activity activity, File apkFile) {
        if (activity == null || apkFile == null || !apkFile.exists()) return false;

        // On Android 8.0+ (API 26+), check if app is permitted to request package installation
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PackageManager pm = activity.getPackageManager();
            if (!pm.canRequestPackageInstalls()) {
                Intent permissionIntent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
                permissionIntent.setData(Uri.parse("package:" + activity.getPackageName()));
                activity.startActivity(permissionIntent);
                return false;
            }
        }

        Uri apkUri = FileProvider.getUriForFile(
                activity,
                activity.getPackageName() + ".fileprovider",
                apkFile
        );

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
        return true;
    }

    private static HttpURLConnection openConnectionWithRedirects(String urlString, String userAgent) throws Exception {
        String currentUrl = urlString;
        int redirectCount = 0;
        final int MAX_REDIRECTS = 5;

        while (redirectCount < MAX_REDIRECTS) {
            URL url = new URL(currentUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setInstanceFollowRedirects(false);
            conn.setRequestProperty("User-Agent", userAgent);

            int code = conn.getResponseCode();
            if (code == HttpURLConnection.HTTP_MOVED_TEMP ||
                code == HttpURLConnection.HTTP_MOVED_PERM ||
                code == HttpURLConnection.HTTP_SEE_OTHER ||
                code == 307 || code == 308) {
                String newLocation = conn.getHeaderField("Location");
                conn.disconnect();
                if (newLocation != null && !newLocation.isEmpty()) {
                    currentUrl = newLocation;
                    redirectCount++;
                    continue;
                }
            }
            return conn;
        }
        throw new IllegalStateException("Too many redirects: " + urlString);
    }

    private static String readStreamToString(InputStream inputStream) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            char[] buffer = new char[4096];
            int read;
            while ((read = reader.read(buffer)) != -1) {
                sb.append(buffer, 0, read);
            }
        }
        return sb.toString();
    }
}
