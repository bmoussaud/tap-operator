package org.moussaud.tanzu.tapoperator.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import org.moussaud.tanzu.tapoperator.resource.TapResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class ConfigMapVersionResource extends BaseResource<ConfigMap> {

    public static final String COMPONENT = "tap-install-values";

    private static final Logger log = LoggerFactory.getLogger(SecretResource.class);

    public ConfigMapVersionResource() {
        super(ConfigMap.class, COMPONENT);
    }

    @Override
    protected ConfigMap desired(TapResource primary, Context<TapResource> context) {
        log.debug("Desired {} {}", name(primary), resourceType());
        var data = new HashMap<String, String>();

        try {
            data.put("tap-operator-tap-install-values.yaml", createYamlMapper().writeValueAsString(buildData(primary, context)));
        } catch (JsonProcessingException e) {
            log.error("JsonProcessingException ", e);
        }
        return new ConfigMapBuilder()
                .withMetadata(createMeta(primary).build())
                .withData(data)
                .build();
    }

    private Map<String, Object> buildData(TapResource resource, Context<TapResource> context) {
        var secret = getSecret(resource, context);
        var to = Utils.getTargetRegistry(secret.getData());
        var ociRepository = Map.of("oci_repository", String.format("%s/%s", to.get("TO_REGISTRY_HOSTNAME"), JobTapCopyResource.TAP_PACKAGES));
        return Map.of("tap_install", Map.of("version", Map.of(
                        "package_repo_bundle_tag", resource.getSpec().getVersion(),
                        "package_version", resource.getSpec().getVersion()),
                "package_repository", ociRepository)
        );
    }


}
