package org.moussaud.tanzu.tapoperator.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.ObjectUtils;
import org.moussaud.tanzu.tapoperator.resource.TapResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Utils {

    private static final Logger log = LoggerFactory.getLogger(Utils.class);

    private static final List<String> availableBundleVersions = Arrays.asList(
            "1.0.0", "1.1.0", "1.1.0-rc.1", "1.1.0-rc.2", "1.2.0", "1.2.0-rc.1", "1.3.0", "1.3.1", "1.3.2", "1.3.3",
            "1.3.4", "1.3.5", "1.4.0", "1.4.1", "1.4.2", "1.4.3", "1.4.4", "1.4.5", "1.4.6", "1.4.7", "1.5.0", "1.5.1",
            "1.5.2", "1.5.3", "1.5.4", "1.5.5", "1.5.6", "1.5.7", "1.6.0", "1.6.1", "1.6.2", "1.6.3", "1.6.4", "1.7.0",
            "1.7.1", "1.7.2");

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

    public static Map<String, String> getDockerConfigJsonTarget(Map<String, String> data) {
        DockerConfigEntry entry = new DockerConfigEntry(decode(data.get("TO_REGISTRY_USERNAME")), decode(data.get("TO_REGISTRY_PASSWORD")));
        DockerConfig target = new DockerConfig();
        target.add(decode(data.get("TO_REGISTRY_HOSTNAME")), entry);

        ObjectMapper mapper = new ObjectMapper();
        StringWriter stringEmp = new StringWriter();
        try {
            mapper.writeValue(stringEmp, target);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        data = new HashMap<>();
        data.put(".dockerconfigjson", encode(stringEmp.toString()));
        return data;
    }

    public static Map<String, String> getAgeSecretKey(Map<String, String> data) {
        return Collections.singletonMap("key.txt", data.get("AGE_SECRET_KEY"));
    }

    public static Map<String, String> getSyncGit(Map<String, String> data) {
        return data.entrySet().stream()
                .filter(e -> e.getKey().startsWith("GIT_"))
                .map(entry -> {
                    var newKey = entry.getKey().toLowerCase().substring(4).replace('_', '-');
                    return Map.entry(newKey, entry.getValue());
                })
                .collect(Collectors.toMap(Map.Entry::getKey,
                        Map.Entry::getValue));
    }


    public static String getClusterEssentialsBundleVersion(TapResource resource) {

        var clusterEssentialsBundleVersion = resource.getSpec().getClusterEssentialsBundleVersion();
        if (ObjectUtils.isNotEmpty(clusterEssentialsBundleVersion)) {
            return clusterEssentialsBundleVersion;
        } else {
            var computed = _getClusterEssentialsBundleVersion(resource.getSpec().getVersion().trim());
            resource.getSpec().setClusterEssentialsBundleVersion(computed);
            return computed;
        }
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

        String last = availableBundleVersions.getLast();
        log.trace("by default return the latest known version return {}", last);
        return last;
    }

    private static String decode(String encoded) {
        byte[] decoded = Base64.getDecoder().decode(encoded);
        return new String(decoded, StandardCharsets.UTF_8);
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


