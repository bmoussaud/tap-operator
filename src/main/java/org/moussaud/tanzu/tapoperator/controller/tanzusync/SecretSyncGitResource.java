package org.moussaud.tanzu.tapoperator.controller.tanzusync;

import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import org.moussaud.tanzu.tapoperator.controller.Utils;
import org.moussaud.tanzu.tapoperator.resource.TapResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecretSyncGitResource extends TanzuSyncResource<Secret> {

    public static final String COMPONENT = "sync-git";

    private static final Logger log = LoggerFactory.getLogger(SecretSyncGitResource.class);

    public SecretSyncGitResource() {
        super(Secret.class, COMPONENT);
    }

    @Override
    protected Secret desired(TapResource primary, Context<TapResource> context) {
        var secret = getSecret(primary, context);
        var result = Utils.getSyncGit(secret.getData());
        if (result.containsKey("ssh-privatekey") && !result.containsKey("ssh-knownhosts")) {
            if (primary.getSpec().getUrl().contains(KnownHosts.GITHUB.getName())) {
                result.put("ssh-knownhosts", KnownHosts.GITHUB.getEncodedContent());
            } else {
                log.warn("ssh-knownhosts empty and unmanaged & unknown provider {}.", primary.getSpec().getUrl());
            }
        }
        return new SecretBuilder()
                .withMetadata(createMeta(primary).build())
                .withData(result)
                .build();
    }

}
