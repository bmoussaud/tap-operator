package org.moussaud.tanzu.tapoperator.controller;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import org.moussaud.tanzu.tapoperator.resource.TapResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static java.lang.String.format;

public class ConfigMapTapGuiResource extends BaseResource<ConfigMap> {

    public static final String COMPONENT = "tap-gui-values";

    private static final Logger log = LoggerFactory.getLogger(ConfigMapTapGuiResource.class);

    public ConfigMapTapGuiResource() {
        super(ConfigMap.class, COMPONENT);
    }

    @Override
    protected ConfigMap desired(TapResource primary, Context<TapResource> context) {
        log.debug("Desired {} {}", name(primary), resourceType());
        var data = Map.of(format("%s.yaml", name(primary)), buildData(primary, context));
        return new ConfigMapBuilder()
                .withMetadata(createMeta(primary).build())
                .withData(data)
                .build();
    }

    private String buildData(TapResource resource, Context<TapResource> context) {
        //Use this kind of ugly YAML template to guarantee the items are always in the same order (Yaml -> String comparison)
        String template = """
                ---
                tap_install:
                  values:
                    tap_gui:
                      app_config:
                        customize:
                          custom_name: Tanzu Application Platform Version %s              
                """;
        return format(template, resource.getSpec().getVersion()).trim();
    }
}
