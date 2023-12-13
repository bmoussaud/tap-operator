package org.moussaud.tanzu.tapoperator.controller;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.moussaud.tanzu.tapoperator.resource.TapResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Utils {

    private static final Logger log = LoggerFactory.getLogger(Utils.class);

    private static final List<String> availableBundleVersions = Arrays.asList(
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
        log.trace("secret data {}", newData);
        return newData;

    }

    public static String getPostgresVersion(TapResource resource) {
        return "1.12.1";
    }

    public static String getClusterEssentialsBundleVersion(TapResource resource) {
        return _getClusterEssentialsBundleVersion(resource.getSpec().getVersion().trim());
    }

    private static final Pattern REG_EXP_RC = Pattern.compile("^(\\d+\\.\\d+\\.\\d+)(?:-([a-zA-Z]+)\\.(\\d+))?$");

    private static final Pattern REG_EXP_MINOR = Pattern.compile("^(\\d+\\.\\d+)\\.(\\d+)(?:-([a-zA-Z]+)\\.(\\d+))?$");

    public static String _getClusterEssentialsBundleVersion(String tapVersion) {
        log.trace("input tapVersion {}", tapVersion);
        if (availableBundleVersions.contains(tapVersion)) {
            log.trace("tapVersion found in availableBundleVersions return {}", tapVersion);
            return tapVersion;
        }

        final Matcher matcherRc = REG_EXP_RC.matcher(tapVersion);
        if (matcherRc.matches()) {
            String mainVersion = matcherRc.group(1);
            if (matcherRc.group(2) != null) {
                return _getClusterEssentialsBundleVersion(mainVersion);
            }
            final Matcher matcherMinor = REG_EXP_MINOR.matcher(tapVersion);
            if (matcherMinor.matches()) {
                String majorMinorVersion = matcherMinor.group(1);
                Optional<String> foundVersion = availableBundleVersions
                        .stream()
                        .filter(v -> v.startsWith(majorMinorVersion))
                        .sorted(Comparator.reverseOrder())
                        .findFirst();
                if (foundVersion.isPresent()) {
                    log.trace("reduced tapVersion found return {}", foundVersion.get());
                    return foundVersion.get();
                }
            }
        }

        String last = availableBundleVersions.get(availableBundleVersions.size() - 1);
        log.trace("by default return the latest known version return {}", last);
        return last;
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
