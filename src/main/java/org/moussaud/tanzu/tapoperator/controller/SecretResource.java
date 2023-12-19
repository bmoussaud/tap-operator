package org.moussaud.tanzu.tapoperator.controller;

import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.moussaud.tanzu.tapoperator.resource.TapResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.javaoperatorsdk.operator.api.reconciler.Context;

public class SecretResource extends BaseResource<Secret> {
    public static final String COMPONENT = "tap-operator-credentials";

    private static final Logger log = LoggerFactory.getLogger(SecretResource.class);

    public SecretResource() {
        super(Secret.class, COMPONENT);
    }

    @Override
    protected Secret desired(TapResource primary, Context<TapResource> context) {
        log.debug("Desired {} {}", name(primary), resourceType());
        return new SecretBuilder()
                .withMetadata(createMeta(primary).build())
                .withData(buildDataFromSecret(primary, context))
                .build();
    }

    private Map<String, String> buildDataFromSecret(TapResource resource, Context<TapResource> context) {
        var secret = getSecret(resource, context);
        return Utils.computeEnvironmentVariables(resource, secret.getData());
    }


}
