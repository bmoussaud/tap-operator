package org.moussaud.tanzu.tapoperator.controller;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.moussaud.tanzu.tapoperator.resource.TapResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Utils {

    private static final Logger log = LoggerFactory.getLogger(Utils.class);

    public static List<String> availableBundleVersions = Arrays.asList(
            "1.0.0", "1.1.0", "1.1.0-rc.1", "1.1.0-rc.2", "1.2.0", "1.2.0-rc.1", "1.3.0", "1.3.1", "1.3.2", "1.3.3",
            "1.3.4", "1.3.5", "1.4.0", "1.4.1", "1.4.2", "1.4.3", "1.4.4", "1.4.5", "1.4.6", "1.4.7", "1.5.0", "1.5.1",
            "1.5.2", "1.5.3", "1.5.4", "1.5.5", "1.5.6", "1.5.7", "1.6.0", "1.6.1", "1.6.2", "1.6.3", "1.6.4", "1.7.0",
            "1.7.1");

    public static Map<String, String> computeEnvironmentVariables(TapResource resource, Map<String, String> data) {
        String install_bundle_value = decode(data.get("TO_REGISTRY_HOSTNAME"))
                + "/tanzu-cluster-essentials/cluster-essentials-bundle:" + getClusterEssentialsBundleVersion(resource);
        final Map<String, String> newData = Map.of(
                "IMGPKG_REGISTRY_HOSTNAME_0", data.get("FROM_REGISTRY_HOSTNAME"),
                "IMGPKG_REGISTRY_USERNAME_0", data.get("FROM_REGISTRY_USERNAME"),
                "IMGPKG_REGISTRY_PASSWORD_0", data.get("FROM_REGISTRY_PASSWORD"),
                "IMGPKG_REGISTRY_HOSTNAME_1", data.get("TO_REGISTRY_HOSTNAME"),
                "IMGPKG_REGISTRY_USERNAME_1", data.get("TO_REGISTRY_USERNAME"),
                "IMGPKG_REGISTRY_PASSWORD_1", data.get("TO_REGISTRY_PASSWORD"),
                "INSTALL_BUNDLE", encode(install_bundle_value),
                "INSTALL_REGISTRY_HOSTNAME", data.get("TO_REGISTRY_HOSTNAME"),
                "INSTALL_REGISTRY_USERNAME", data.get("TO_REGISTRY_USERNAME"),
                "INSTALL_REGISTRY_PASSWORD", data.get("TO_REGISTRY_PASSWORD"));
        log.debug("secret data {}", newData);
        return newData;

    }

    public static String getJobName(TapResource resource) {
        return resource.getMetadata().getName() + "-copy-package-job";
    }

    public static String getSecretName(TapResource resource) {
        return resource.getMetadata().getName() + "-tap-operator-credentials";
    }

    public static String getClusterEssentialsBundleVersion(TapResource resource) {
        return resource.getSpec().getVersion();
    }

    private static String decode(String encoded) {
        byte[] decoded = Base64.getDecoder().decode(encoded);
        String decodedStr = new String(decoded, StandardCharsets.UTF_8);
        return decodedStr;
    }

    private static String encode(String s) {
        try {
            String x = Base64.getEncoder().encodeToString(s.getBytes("UTF-8"));
            return x;
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return "X";
        }
    }
}
