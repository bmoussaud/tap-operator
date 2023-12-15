package org.moussaud.tanzu.tapoperator.controller;

import java.util.Map;

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
        // TODO Fix this : why the CRD does not return the default value.
        var secretName = (resource.getSpec().getSecret() == null ? "tap-operator-registry-credentials"
                : resource.getSpec().getSecret());
        var secret = context.getClient().secrets().inNamespace(resource.getMetadata().getNamespace())
                .withName(secretName).get();
        if (secret == null) {
            throw new RuntimeException(String.format("{} secret not found in the {} namespace ",
                    secretName, resource.getMetadata().getNamespace()));
        }
        return Utils.computeEnvironmentVariables(resource, secret.getData());
    }
}
