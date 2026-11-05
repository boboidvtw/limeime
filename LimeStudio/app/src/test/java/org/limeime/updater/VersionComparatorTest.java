package org.limeime.updater;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class VersionComparatorTest {

    @Test
    public void testExtractVersionCodeFromFileName() {
        assertEquals(202662000L, VersionComparator.extractVersionCodeFromFileName("LIMEHD202662000-v6.2.0-DarkAccent.apk"));
        assertEquals(501L, VersionComparator.extractVersionCodeFromFileName("LIMEHD501-5.0.1.apk"));
        assertEquals(0L, VersionComparator.extractVersionCodeFromFileName("custom-build.apk"));
        assertEquals(0L, VersionComparator.extractVersionCodeFromFileName(null));
    }

    @Test
    public void testCompareSemanticVersions() {
        // v1 > v2
        assertTrue(VersionComparator.compareSemanticVersions("v6.2.1", "v6.2.0-DarkAccent") > 0);
        assertTrue(VersionComparator.compareSemanticVersions("v7.0.0", "6.2.0") > 0);
        assertTrue(VersionComparator.compareSemanticVersions("6.3.0", "6.2.9") > 0);

        // v1 == v2
        assertEquals(0, VersionComparator.compareSemanticVersions("v6.2.0", "v6.2.0-DarkAccent"));
        assertEquals(0, VersionComparator.compareSemanticVersions("6.2.0", "6.2.0"));

        // v1 < v2
        assertTrue(VersionComparator.compareSemanticVersions("v6.1.38", "v6.2.0") < 0);
        assertTrue(VersionComparator.compareSemanticVersions("5.0.6", "6.0.0") < 0);
    }

    @Test
    public void testIsRemoteNewerWithVersionCode() {
        // Remote code > Local code
        assertTrue(VersionComparator.isRemoteNewer(202662000L, "v6.2.0-DarkAccent", 202662001L, "v6.2.1"));
        // Remote code <= Local code
        assertFalse(VersionComparator.isRemoteNewer(202662000L, "v6.2.0-DarkAccent", 202662000L, "v6.2.0-DarkAccent"));
        assertFalse(VersionComparator.isRemoteNewer(202662000L, "v6.2.0-DarkAccent", 202661999L, "v6.1.38"));
    }

    @Test
    public void testIsRemoteNewerFallbackSemVer() {
        // Code is 0 (fallback to semver)
        assertTrue(VersionComparator.isRemoteNewer(0L, "v6.2.0-DarkAccent", 0L, "v6.2.1"));
        assertFalse(VersionComparator.isRemoteNewer(0L, "v6.2.0-DarkAccent", 0L, "v6.2.0"));
        assertFalse(VersionComparator.isRemoteNewer(0L, "v6.2.0-DarkAccent", 0L, "v6.1.38"));
    }
}
