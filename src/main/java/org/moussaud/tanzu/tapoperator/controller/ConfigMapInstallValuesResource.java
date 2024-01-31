package org.moussaud.tanzu.tapoperator.controller;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import org.moussaud.tanzu.tapoperator.resource.TapResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class ConfigMapInstallValuesResource extends BaseResource<ConfigMap> {

    public static final String COMPONENT = "tap-install-values";

    private static final Logger log = LoggerFactory.getLogger(ConfigMapInstallValuesResource.class);

    public ConfigMapInstallValuesResource() {
        super(ConfigMap.class, COMPONENT);
    }

    @Override
    protected ConfigMap desired(TapResource primary, Context<TapResource> context) {
        log.debug("Desired {} {}", name(primary), resourceType());
        var data = Map.of("tap-operator-tap-install-values.yaml", buildData(primary, context));
        return new ConfigMapBuilder()
                .withMetadata(createMeta(primary).build())
                .withData(data)
                .build();
    }

    private String buildData(TapResource resource, Context<TapResource> context) {
        var secret = getSecret(resource, context);
        var to = Utils.getTargetRegistry(secret.getData());
        return toYAML(to.get("TO_REGISTRY_HOSTNAME"), resource.getSpec().getVersion(), resource.getSpec().getVersion()).trim();
    }

    private String toYAML(String package_repo_bundle_tag, String package_version, String repository) {

        //Use this kind of ugly YAML template to guarantee the items are always in the same order (Yaml -> String comparison)
        String template = """
                ---
                tap_install:
                  package_repository:
                    oci_repository: "%s/tanzu-application-platform/tap-packages"
                  version:
                    package_repo_bundle_tag: "%s"
                    package_version: "%s"
                """;

        return String.format(template, package_repo_bundle_tag, package_version, repository).trim();
    }


}
