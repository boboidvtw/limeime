package org.limeime.updater;

/**
 * Result contract for update check operations adhering to Rule 6 (Zero-Exception & Result Pattern).
 */
public class UpdateCheckResult {
    public static final String CODE_SUCCESS_UPDATE_AVAILABLE = "UPDATE_AVAILABLE_001";
    public static final String CODE_SUCCESS_UP_TO_DATE = "UPDATE_UP_TO_DATE_002";
    public static final String CODE_ERROR_NETWORK = "UPDATE_NETWORK_ERROR_003";
    public static final String CODE_ERROR_PARSE = "UPDATE_PARSE_ERROR_004";
    public static final String CODE_ERROR_NO_ASSET = "UPDATE_NO_ASSET_ERROR_005";
    public static final String CODE_ERROR_RATE_LIMIT = "UPDATE_RATE_LIMIT_ERROR_006";

    private final boolean success;
    private final boolean hasUpdate;
    private final String errorCode;
    private final String message;
    private final ReleaseInfo releaseInfo;

    private UpdateCheckResult(boolean success, boolean hasUpdate, String errorCode, String message, ReleaseInfo releaseInfo) {
        this.success = success;
        this.hasUpdate = hasUpdate;
        this.errorCode = errorCode != null ? errorCode : "";
        this.message = message != null ? message : "";
        this.releaseInfo = releaseInfo;
    }

    public static UpdateCheckResult updateAvailable(ReleaseInfo info) {
        return new UpdateCheckResult(true, true, CODE_SUCCESS_UPDATE_AVAILABLE, "New version available", info);
    }

    public static UpdateCheckResult upToDate(ReleaseInfo info) {
        return new UpdateCheckResult(true, false, CODE_SUCCESS_UP_TO_DATE, "App is up to date", info);
    }

    public static UpdateCheckResult error(String errorCode, String message) {
        return new UpdateCheckResult(false, false, errorCode, message, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public boolean hasUpdate() {
        return hasUpdate;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getMessage() {
        return message;
    }

    public ReleaseInfo getReleaseInfo() {
        return releaseInfo;
    }
}
