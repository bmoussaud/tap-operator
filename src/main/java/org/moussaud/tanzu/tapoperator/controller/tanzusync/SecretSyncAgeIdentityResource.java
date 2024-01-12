package org.moussaud.tanzu.tapoperator.controller.tanzusync;

import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import org.moussaud.tanzu.tapoperator.controller.Utils;
import org.moussaud.tanzu.tapoperator.resource.TapResource;

public class SecretSyncAgeIdentityResource extends TanzuSyncResource<Secret> {

    public static final String COMPONENT = "sync-age-identity";

    public SecretSyncAgeIdentityResource() {
        super(Secret.class, COMPONENT);
    }

    @Override
    protected Secret desired(TapResource primary, Context<TapResource> context) {
        var secret = getSecret(primary, context);
        return new SecretBuilder()
                .withMetadata(createMeta(primary).build())
                .withData(Utils.getAgeSecretKey(secret.getData()))
                .build();
    }
}
