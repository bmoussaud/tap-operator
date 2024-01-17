package org.moussaud.tanzu.tapoperator.controller;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import org.moussaud.tanzu.tapoperator.resource.TapResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class ConfigMapVersionResource extends BaseResource<ConfigMap> {

    public static final String COMPONENT = "tap-version";

    private static final Logger log = LoggerFactory.getLogger(SecretResource.class);

    public ConfigMapVersionResource() {
        super(ConfigMap.class, COMPONENT);
    }

    @Override
    protected ConfigMap desired(TapResource primary, Context<TapResource> context) {
        log.debug("Desired {} {}", name(primary), resourceType());
        var data = new HashMap<String, String>();
        var tap_install_values = Map.of("tap_operator", Map.of("tap_version", primary.getSpec().getVersion()));
        var tap_install_schema = Map.of("tap_operator", Map.of("tap_version", "x"));
        try {
            var schemaYamlMapper = createYamlMapper();
            //data.put("schema.yaml", "#@data/values-schema\n" + schemaYamlMapper.writeValueAsString(tap_install_schema));
            data.put("values.yaml", schemaYamlMapper.writeValueAsString(tap_install_values));
        } catch (JsonProcessingException e) {
            log.error("JsonProcessingException ", e);
        }

        return new ConfigMapBuilder()
                .withMetadata(createMeta(primary).build())
                .withData(data)
                .build();
    }

    public static ObjectMapper createYamlMapper() {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory()
               // .configure(YAMLGenerator.Feature.MINIMIZE_QUOTES, true)
                //.configure(YAMLGenerator.Feature.ALWAYS_QUOTE_NUMBERS_AS_STRINGS, true)
                .configure(YAMLGenerator.Feature.USE_NATIVE_OBJECT_ID, false)
                .configure(YAMLGenerator.Feature.USE_NATIVE_TYPE_ID, false)
        );
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY).
                enable(SerializationFeature.INDENT_OUTPUT);
        return objectMapper;

    }
}
