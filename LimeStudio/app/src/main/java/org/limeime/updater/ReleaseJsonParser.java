package org.limeime.updater;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Pure JSON parser for GitHub Releases API responses.
 */
public class ReleaseJsonParser {

    public static UpdateCheckResult parse(String jsonString, long localVersionCode, String localVersionName) {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return UpdateCheckResult.error(UpdateCheckResult.CODE_ERROR_PARSE, "Empty JSON response");
        }

        try {
            JSONObject obj = new JSONObject(jsonString);

            // Handle GitHub rate limit response
            if (obj.has("message") && obj.getString("message").contains("API rate limit exceeded")) {
                return UpdateCheckResult.error(UpdateCheckResult.CODE_ERROR_RATE_LIMIT, obj.getString("message"));
            }

            String tagName = obj.optString("tag_name", "");
            String title = obj.optString("name", tagName);
            String notes = obj.optString("body", "");

            JSONArray assets = obj.optJSONArray("assets");
            if (assets == null || assets.length() == 0) {
                return UpdateCheckResult.error(UpdateCheckResult.CODE_ERROR_NO_ASSET, "No assets found in release");
            }

            String apkUrl = null;
            String apkFileName = null;
            long apkSize = 0;
            long remoteVersionCode = 0;

            for (int i = 0; i < assets.length(); i++) {
                JSONObject asset = assets.getJSONObject(i);
                String name = asset.optString("name", "");
                if (name.toLowerCase().endsWith(".apk")) {
                    apkFileName = name;
                    apkUrl = asset.optString("browser_download_url", "");
                    apkSize = asset.optLong("size", 0);
                    remoteVersionCode = VersionComparator.extractVersionCodeFromFileName(name);
                    break;
                }
            }

            if (apkUrl == null || apkUrl.isEmpty()) {
                return UpdateCheckResult.error(UpdateCheckResult.CODE_ERROR_NO_ASSET, "No APK asset found in release");
            }

            ReleaseInfo info = new ReleaseInfo(tagName, title, notes, apkUrl, apkFileName, apkSize, remoteVersionCode);

            boolean hasUpdate = VersionComparator.isRemoteNewer(localVersionCode, localVersionName, remoteVersionCode, tagName);
            if (hasUpdate) {
                return UpdateCheckResult.updateAvailable(info);
            } else {
                return UpdateCheckResult.upToDate(info);
            }

        } catch (JSONException e) {
            return UpdateCheckResult.error(UpdateCheckResult.CODE_ERROR_PARSE, "Failed to parse JSON: " + e.getMessage());
        }
    }
}
