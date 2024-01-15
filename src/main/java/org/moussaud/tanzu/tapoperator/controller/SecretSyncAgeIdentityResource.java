package org.moussaud.tanzu.tapoperator.controller;

import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import org.moussaud.tanzu.tapoperator.resource.TapResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecretSyncAgeIdentityResource extends BaseResource<Secret> {

    private static final Logger log = LoggerFactory.getLogger(SecretSyncAgeIdentityResource.class);

    public static final String COMPONENT = "sync-age-identity";

    public SecretSyncAgeIdentityResource() {
        super(Secret.class, COMPONENT);
    }

    @Override
    protected Secret desired(TapResource primary, Context<TapResource> context) {
        log.info("Desired {} {}", name(primary), resourceType());
        var secret = getSecret(primary, context);
        return new SecretBuilder()
                .withMetadata(createMeta(primary).build())
                .withData(Utils.getAgeSecretKey(secret.getData()))
                .build();
    }
}
