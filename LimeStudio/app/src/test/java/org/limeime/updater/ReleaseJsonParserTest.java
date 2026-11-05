package org.limeime.updater;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ReleaseJsonParserTest {

    private static final String SAMPLE_RELEASE_JSON = "{\n" +
            "  \"tag_name\": \"v6.2.1\",\n" +
            "  \"name\": \"萊姆輸入法 v6.2.1\",\n" +
            "  \"body\": \"修復輸入法候選字問題\",\n" +
            "  \"assets\": [\n" +
            "    {\n" +
            "      \"name\": \"LIMEHD202662001-v6.2.1.apk\",\n" +
            "      \"browser_download_url\": \"https://github.com/boboidvtw/limeime/releases/download/v6.2.1/LIMEHD202662001-v6.2.1.apk\",\n" +
            "      \"size\": 16785200\n" +
            "    }\n" +
            "  ]\n" +
            "}";

    private static final String NO_APK_RELEASE_JSON = "{\n" +
            "  \"tag_name\": \"v6.2.1\",\n" +
            "  \"name\": \"iOS Release\",\n" +
            "  \"body\": \"Only iOS assets\",\n" +
            "  \"assets\": [\n" +
            "    {\n" +
            "      \"name\": \"source.zip\",\n" +
            "      \"browser_download_url\": \"https://github.com/.../source.zip\",\n" +
            "      \"size\": 1024\n" +
            "    }\n" +
            "  ]\n" +
            "}";

    private static final String RATE_LIMIT_JSON = "{\n" +
            "  \"message\": \"API rate limit exceeded for IP\",\n" +
            "  \"documentation_url\": \"https://docs.github.com/rest/overview/resources-in-the-rest-api#rate-limiting\"\n" +
            "}";

    @Test
    public void testParseUpdateAvailable() {
        // Local is 202662000, Remote is 202662001 -> update available
        UpdateCheckResult result = ReleaseJsonParser.parse(SAMPLE_RELEASE_JSON, 202662000L, "v6.2.0-DarkAccent");
        assertTrue(result.isSuccess());
        assertTrue(result.hasUpdate());
        assertEquals(UpdateCheckResult.CODE_SUCCESS_UPDATE_AVAILABLE, result.getErrorCode());

        ReleaseInfo info = result.getReleaseInfo();
        assertNotNull(info);
        assertEquals("v6.2.1", info.getTagName());
        assertEquals("萊姆輸入法 v6.2.1", info.getTitle());
        assertEquals("LIMEHD202662001-v6.2.1.apk", info.getApkFileName());
        assertEquals(16785200L, info.getApkSize());
        assertEquals(202662001L, info.getRemoteVersionCode());
        assertEquals("https://github.com/boboidvtw/limeime/releases/download/v6.2.1/LIMEHD202662001-v6.2.1.apk", info.getApkDownloadUrl());
    }

    @Test
    public void testParseAlreadyUpToDate() {
        // Local is 202662001, Remote is 202662001 -> up to date
        UpdateCheckResult result = ReleaseJsonParser.parse(SAMPLE_RELEASE_JSON, 202662001L, "v6.2.1");
        assertTrue(result.isSuccess());
        assertFalse(result.hasUpdate());
        assertEquals(UpdateCheckResult.CODE_SUCCESS_UP_TO_DATE, result.getErrorCode());
        assertNotNull(result.getReleaseInfo());
    }

    @Test
    public void testParseNoApkAsset() {
        UpdateCheckResult result = ReleaseJsonParser.parse(NO_APK_RELEASE_JSON, 202662000L, "v6.2.0-DarkAccent");
        assertFalse(result.isSuccess());
        assertEquals(UpdateCheckResult.CODE_ERROR_NO_ASSET, result.getErrorCode());
    }

    @Test
    public void testParseRateLimit() {
        UpdateCheckResult result = ReleaseJsonParser.parse(RATE_LIMIT_JSON, 202662000L, "v6.2.0-DarkAccent");
        assertFalse(result.isSuccess());
        assertEquals(UpdateCheckResult.CODE_ERROR_RATE_LIMIT, result.getErrorCode());
    }

    @Test
    public void testParseMalformedJson() {
        UpdateCheckResult result = ReleaseJsonParser.parse("{ invalid json", 202662000L, "v6.2.0-DarkAccent");
        assertFalse(result.isSuccess());
        assertEquals(UpdateCheckResult.CODE_ERROR_PARSE, result.getErrorCode());
    }

    @Test
    public void testParseEmptyJson() {
        UpdateCheckResult result = ReleaseJsonParser.parse("", 202662000L, "v6.2.0-DarkAccent");
        assertFalse(result.isSuccess());
        assertEquals(UpdateCheckResult.CODE_ERROR_PARSE, result.getErrorCode());
    }
}
