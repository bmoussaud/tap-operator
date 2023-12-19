package org.moussaud.tanzu.tapoperator.controller.tanzusync;

import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import org.moussaud.tanzu.tapoperator.controller.Utils;
import org.moussaud.tanzu.tapoperator.resource.TapResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InstallRegistryDockerConfigSecretResource extends BaseSecretResource {

    public static final String COMPONENT = "install-registry-dockerconfig";
    
    public InstallRegistryDockerConfigSecretResource() {
        super(COMPONENT);
    }

    @Override
    protected Secret desired(TapResource primary, Context<TapResource> context) {
        var secret = getSecret(primary, context);
        return new SecretBuilder()
                .withMetadata(createMeta(primary).build())
                .withType("kubernetes.io/dockerconfigjson")
                .withData(Utils.getDockerConfigJsonTarget(secret.getData()))
                .build();
    }
}
