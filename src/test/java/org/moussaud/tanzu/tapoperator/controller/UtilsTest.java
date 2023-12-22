package org.moussaud.tanzu.tapoperator.controller;

import org.junit.jupiter.api.Test;
import org.moussaud.tanzu.tapoperator.resource.TapResource;
import org.moussaud.tanzu.tapoperator.resource.TapSpec;

import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class UtilsTest {

    @Test
    void testKnownVersionGetClusterEssentialsBundleVersion() {
        TapResource resource = new TapResource();
        TapSpec spec = new TapSpec();
        spec.setVersion("1.7.0");
        resource.setSpec(spec);
        assertEquals("1.7.0", Utils.getClusterEssentialsBundleVersion(resource));
    }

    @Test
    void testUnknownVersionGetClusterEssentialsBundleVersion() {
        TapResource resource = new TapResource();
        TapSpec spec = new TapSpec();
        spec.setVersion("1.9.x");
        resource.setSpec(spec);
        assertEquals("1.7.1", Utils.getClusterEssentialsBundleVersion(resource));
    }

    @Test
    void testUnknownRCVersionGetClusterEssentialsBundleVersion() {
        TapResource resource = new TapResource();
        TapSpec spec = new TapSpec();
        spec.setVersion("1.9.4-rc.3"); // 1.7.2-rc.6
        resource.setSpec(spec);
        assertEquals("1.7.1", Utils.getClusterEssentialsBundleVersion(resource));
    }

    @Test
    void testRCVersionGetClusterEssentialsBundleVersion() {
        TapResource resource = new TapResource();
        TapSpec spec = new TapSpec();
        spec.setVersion("1.7.0-rc.8");
        resource.setSpec(spec);
        assertEquals("1.7.0", Utils.getClusterEssentialsBundleVersion(resource));
    }

    @Test
    void testRoundedLowerRCVersionGetClusterEssentialsBundleVersion() {
        TapResource resource = new TapResource();
        TapSpec spec = new TapSpec();
        spec.setVersion("1.4.12-rc.8 ");
        resource.setSpec(spec);
        assertEquals("1.4.7", Utils.getClusterEssentialsBundleVersion(resource));
    }

    @Test
    void testBuildVersionGetClusterEssentialsBundleVersion() {
        TapResource resource = new TapResource();
        TapSpec spec = new TapSpec();
        spec.setVersion("1.8.0-build.38");
        resource.setSpec(spec);
        assertEquals("1.7.1", Utils.getClusterEssentialsBundleVersion(resource));
    }

    @Test
    void testgetDockerConfigJsonTarget() {
        Map<String, String> input = getEncodedSecret();
        String encodedjson = Utils.getDockerConfigJsonTarget(input).get(".dockerconfigjson");
        assertEquals("eyJhdXRocyI6eyJha3NldXRhcDdyZWdpc3RyeS5henVyZWNyLmlvIjp7ImF1dGgiOiJPVFZpTXpnd1lXWXROR0l6WmkwME5XUTJMV0ptWmpndE1qWm1NalZrTUdJeFpHSXlPa1JwTlRoUmZsOVhkV2R4V1ZFMUxsaEljelV5UTBSMVZsWkhNVlV5VTJoaVQyTnpNMGhoT1RBPSIsInBhc3N3b3JkIjoiRGk1OFF+X1d1Z3FZUTUuWEhzNTJDRHVWVkcxVTJTaGJPY3MzSGE5MCIsInVzZXJuYW1lIjoiOTViMzgwYWYtNGIzZi00NWQ2LWJmZjgtMjZmMjVkMGIxZGIyIn19fQ==", encodedjson);
    }

    @Test
    void testgetAgeSecretKey() {
        Map<String, String> input = getEncodedSecret();
        String encoded = Utils.getAgeSecretKey(input).get("key.txt");
        assertEquals("QUdFLVNFQ1JFVC1LRVktMTIzYXplcnR5dWlvcGtxamRsbWtmamxta3NkamZtbGtxc2ptbGZramJvbmpvdXI=", encoded);
    }


    @Test
    void testSyncGitKey() {
        Map<String, String> input = getEncodedSecret();
        Map<String, String> result = Utils.getSyncGit(input);
        assertEquals(4, result.size());
        assertTrue(result.containsKey("ssh-privatekey"));
        assertTrue(result.containsKey("ssh-knownhosts"));
        assertTrue(result.containsKey("username"));
        assertTrue(result.containsKey("password"));
    }

    private Map<String, String> getEncodedSecret() {
        Map<String, String> data = new HashMap<>();
        data.put("TO_REGISTRY_HOSTNAME", encode("akseutap7registry.azurecr.io"));
        data.put("TO_REGISTRY_USERNAME", encode("95b380af-4b3f-45d6-bff8-26f25d0b1db2"));
        data.put("TO_REGISTRY_PASSWORD", encode("Di58Q~_WugqYQ5.XHs52CDuVVG1U2ShbOcs3Ha90"));
        data.put("AGE_SECRET_KEY", encode("AGE-SECRET-KEY-123azertyuiopkqjdlmkfjlmksdjfmlkqsjmlfkjbonjour"));
        data.put("GIT_SSH_PRIVATEKEY", encode("MYKEY"));
        data.put("GIT_SSH_KNOWNHOSTS", encode("MYHOSTS"));
        data.put("GIT_USERNAME", encode("SCOTT"));
        data.put("GIT_PASSWORD", encode("TIGER"));

        return data;
    }


    private static String encode(String s) {
        try {
            return Base64.getEncoder().encodeToString(s.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return "X";
        }
    }
}
