package org.limeime.updater;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import org.limeime.R;

import java.io.File;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Handles presentation of update alerts, download progress, and installation prompts.
 */
public class AppUpdateDialogHelper {

    public static void showUpdateResultDialog(Activity activity, UpdateCheckResult result, boolean isManualCheck) {
        if (activity == null || activity.isFinishing()) return;

        if (!result.isSuccess()) {
            if (isManualCheck) {
                new MaterialAlertDialogBuilder(activity)
                        .setTitle(R.string.update_check_failed_title)
                        .setMessage(result.getMessage())
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
            }
            return;
        }

        if (!result.hasUpdate()) {
            if (isManualCheck) {
                Toast.makeText(activity, R.string.update_already_latest, Toast.LENGTH_SHORT).show();
            }
            return;
        }

        final ReleaseInfo info = result.getReleaseInfo();
        String sizeStr = formatFileSize(info.getApkSize());
        String dialogMessage = activity.getString(R.string.update_dialog_size, sizeStr) + "\n\n" + info.getNotes();

        new MaterialAlertDialogBuilder(activity)
                .setTitle(activity.getString(R.string.update_dialog_title, info.getTagName()))
                .setMessage(dialogMessage)
                .setPositiveButton(R.string.update_dialog_action_now, (dialog, which) -> startDownload(activity, info))
                .setNegativeButton(R.string.update_dialog_action_later, null)
                .setNeutralButton(R.string.update_dialog_action_github, (dialog, which) -> {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(info.getApkDownloadUrl()));
                    activity.startActivity(browserIntent);
                })
                .show();
    }

    public static void startDownload(Activity activity, ReleaseInfo info) {
        if (activity == null || activity.isFinishing()) return;

        LayoutInflater inflater = LayoutInflater.from(activity);
        View dialogView = inflater.inflate(R.layout.dialog_update_download, null, false);
        LinearProgressIndicator progressBar = dialogView.findViewById(R.id.progressUpdateDownload);
        TextView txtDetails = dialogView.findViewById(R.id.txtUpdateProgressDetails);

        AtomicBoolean cancelRef = new AtomicBoolean(false);

        AlertDialog downloadDialog = new MaterialAlertDialogBuilder(activity)
                .setTitle(R.string.update_downloading_title)
                .setView(dialogView)
                .setCancelable(false)
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> cancelRef.set(true))
                .create();

        downloadDialog.show();

        AtomicBoolean activeCancellation = AppUpdateManager.downloadApk(
                activity,
                info,
                (bytesDownloaded, totalBytes, percent) -> {
                    if (downloadDialog.isShowing()) {
                        progressBar.setProgress(percent);
                        double currentMb = bytesDownloaded / (1024.0 * 1024.0);
                        double totalMb = totalBytes / (1024.0 * 1024.0);
                        txtDetails.setText(String.format(Locale.getDefault(), "%.1f MB / %.1f MB (%d%%)", currentMb, totalMb, percent));
                    }
                },
                new AppUpdateManager.DownloadResultCallback() {
                    @Override
                    public void onSuccess(File apkFile) {
                        if (downloadDialog.isShowing()) {
                            downloadDialog.dismiss();
                        }
                        AppUpdateManager.installApk(activity, apkFile);
                    }

                    @Override
                    public void onError(String errorCode, String message) {
                        if (downloadDialog.isShowing()) {
                            downloadDialog.dismiss();
                        }
                        new MaterialAlertDialogBuilder(activity)
                                .setTitle(R.string.update_download_failed_title)
                                .setMessage(activity.getString(R.string.update_download_failed_msg, message))
                                .setPositiveButton(R.string.update_open_browser_btn, (d, w) -> {
                                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(info.getApkDownloadUrl()));
                                    activity.startActivity(browserIntent);
                                })
                                .setNegativeButton(android.R.string.ok, null)
                                .show();
                    }
                }
        );

        // Link dialog cancel button to active task cancellation
        downloadDialog.setOnDismissListener(d -> activeCancellation.set(cancelRef.get()));
    }

    private static String formatFileSize(long bytes) {
        if (bytes <= 0) return "0 MB";
        double mb = bytes / (1024.0 * 1024.0);
        return String.format(Locale.getDefault(), "%.1f MB", mb);
    }
}
