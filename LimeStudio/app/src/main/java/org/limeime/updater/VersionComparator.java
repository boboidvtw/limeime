package org.limeime.updater;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Pure utility class for comparing local and remote application versions.
 * Supports both numeric Android versionCode and semantic versioning strings.
 */
public class VersionComparator {

    private static final Pattern CODE_PATTERN = Pattern.compile("LIMEHD(\\d+)");
    private static final Pattern SEMVER_PATTERN = Pattern.compile("(\\d+)(?:\\.(\\d+))?(?:\\.(\\d+))?");

    /**
     * Extracts numeric versionCode from APK filename (e.g. "LIMEHD202662000-v6.2.0-DarkAccent.apk" -> 202662000).
     */
    public static long extractVersionCodeFromFileName(String fileName) {
        if (fileName == null) return 0;
        Matcher matcher = CODE_PATTERN.matcher(fileName);
        if (matcher.find()) {
            try {
                return Long.parseLong(matcher.group(1));
            } catch (NumberFormatException ignored) {
            }
        }
        return 0;
    }

    /**
     * Determines whether the remote release is newer than the local installation.
     */
    public static boolean isRemoteNewer(long localVersionCode, String localVersionName,
                                        long remoteVersionCode, String remoteTagName) {
        // Priority 1: Compare numeric versionCode if both are positive integers
        if (remoteVersionCode > 0 && localVersionCode > 0) {
            return remoteVersionCode > localVersionCode;
        }

        // Priority 2: Semantic version comparison as fallback
        return compareSemanticVersions(remoteTagName, localVersionName) > 0;
    }

    /**
     * Compares two semantic version strings (e.g. "v6.2.1" vs "v6.2.0-DarkAccent").
     * Returns 1 if v1 > v2, -1 if v1 < v2, and 0 if equal.
     */
    public static int compareSemanticVersions(String v1, String v2) {
        int[] parts1 = parseSemVer(v1);
        int[] parts2 = parseSemVer(v2);

        for (int i = 0; i < 3; i++) {
            if (parts1[i] > parts2[i]) return 1;
            if (parts1[i] < parts2[i]) return -1;
        }
        return 0;
    }

    private static int[] parseSemVer(String raw) {
        int[] result = new int[]{0, 0, 0};
        if (raw == null) return result;

        String cleaned = raw.trim();
        if (cleaned.startsWith("v") || cleaned.startsWith("V")) {
            cleaned = cleaned.substring(1);
        }

        Matcher matcher = SEMVER_PATTERN.matcher(cleaned);
        if (matcher.find()) {
            if (matcher.group(1) != null) result[0] = parseIntSafe(matcher.group(1));
            if (matcher.group(2) != null) result[1] = parseIntSafe(matcher.group(2));
            if (matcher.group(3) != null) result[2] = parseIntSafe(matcher.group(3));
        }
        return result;
    }

    private static int parseIntSafe(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
