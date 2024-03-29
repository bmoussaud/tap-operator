package org.moussaud.tanzu.tapoperator.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import org.moussaud.tanzu.tapoperator.resource.TapResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static org.apache.logging.log4j.util.Base64Util.encode;


public class SecretTapSensitiveImageRegistryResource extends BaseResource<Secret> {

    private static final Logger log = LoggerFactory.getLogger(SecretTapSensitiveImageRegistryResource.class);

    public static final String COMPONENT = "sensitive-image-registry";

    public SecretTapSensitiveImageRegistryResource() {
        super(Secret.class, COMPONENT);
    }

    @Override
    protected Secret desired(TapResource primary, Context<TapResource> context) {
        log.debug("Desired {} {}", name(primary), resourceType());
        var data = new HashMap<String, String>();
        var schemaYamlMapper = new ObjectMapper(new YAMLFactory());
        try {
            data.put("tap-operator-tap-sensitive-values.yaml", encode(schemaYamlMapper.writeValueAsString(buildData(primary, context))));
        } catch (JsonProcessingException e) {
            log.error("JsonProcessing Error", e);
            throw new RuntimeException(e);
        }
        return new SecretBuilder()
                .withMetadata(createMeta(primary).build())
                .withData(data)
                .build();
    }

    private Map<String, Object> buildData(TapResource resource, Context<TapResource> context) {
        var secret = getSecret(resource, context);
        var to = Utils.getTargetRegistry(secret.getData());
        var data = new HashMap<String, String>();
        data.put("project_path", String.format("%s/library/tanzu-build-service", to.get("TO_REGISTRY_HOSTNAME")));
        data.put("username", to.get("TO_REGISTRY_USERNAME"));
        data.put("password", to.get("TO_REGISTRY_PASSWORD"));
        return Map.of("tap_install", Map.of("sensitive_values", Map.of("shared", Map.of("image_registry", data))));
    }

    @Override
    protected Secret handleUpdate(Secret actual, Secret desired, TapResource primary, Context<TapResource> context) {
        return super.handleUpdate(actual, desired, primary, context);
    }
}
