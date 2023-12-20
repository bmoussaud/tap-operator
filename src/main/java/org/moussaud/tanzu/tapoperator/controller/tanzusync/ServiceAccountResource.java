package org.moussaud.tanzu.tapoperator.controller.tanzusync;

import org.moussaud.tanzu.tapoperator.resource.TapResource;

import io.fabric8.kubernetes.api.model.ServiceAccount;
import io.fabric8.kubernetes.api.model.ServiceAccountBuilder;
import io.javaoperatorsdk.operator.api.reconciler.Context;

public class ServiceAccountResource extends TanzuSyncResource<ServiceAccount> {
    public static final String COMPONENT = "sync-sa";

    public ServiceAccountResource() {
        super(ServiceAccount.class, COMPONENT);
    }

    @Override
    protected ServiceAccount desired(TapResource primary, Context<TapResource> context) {
        return new ServiceAccountBuilder()
                .withMetadata(createMeta(primary).build())
                .build();
    }

}
