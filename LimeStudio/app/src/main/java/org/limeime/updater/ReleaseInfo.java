package org.limeime.updater;

/**
 * Encapsulates release metadata fetched from the GitHub Releases API.
 */
public class ReleaseInfo {
    private final String tagName;
    private final String title;
    private final String notes;
    private final String apkDownloadUrl;
    private final String apkFileName;
    private final long apkSize;
    private final long remoteVersionCode;

    public ReleaseInfo(String tagName, String title, String notes,
                       String apkDownloadUrl, String apkFileName,
                       long apkSize, long remoteVersionCode) {
        this.tagName = tagName != null ? tagName : "";
        this.title = title != null ? title : "";
        this.notes = notes != null ? notes : "";
        this.apkDownloadUrl = apkDownloadUrl != null ? apkDownloadUrl : "";
        this.apkFileName = apkFileName != null ? apkFileName : "";
        this.apkSize = apkSize;
        this.remoteVersionCode = remoteVersionCode;
    }

    public String getTagName() {
        return tagName;
    }

    public String getTitle() {
        return title;
    }

    public String getNotes() {
        return notes;
    }

    public String getApkDownloadUrl() {
        return apkDownloadUrl;
    }

    public String getApkFileName() {
        return apkFileName;
    }

    public long getApkSize() {
        return apkSize;
    }

    public long getRemoteVersionCode() {
        return remoteVersionCode;
    }
}
