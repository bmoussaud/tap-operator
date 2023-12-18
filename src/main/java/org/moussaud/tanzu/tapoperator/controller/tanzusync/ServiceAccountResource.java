package org.moussaud.tanzu.tapoperator.controller.tanzusync;

import org.moussaud.tanzu.tapoperator.resource.TapResource;

import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.ServiceAccount;
import io.fabric8.kubernetes.api.model.ServiceAccountBuilder;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.dependent.Deleter;
import io.javaoperatorsdk.operator.processing.dependent.Creator;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependentResource;

public class ServiceAccountResource extends KubernetesDependentResource<ServiceAccount, TapResource>
        implements Creator<ServiceAccount, TapResource>, Deleter<TapResource> {
    public static final String COMPONENT = "sync-sa";

    public ServiceAccountResource() {
        super(ServiceAccount.class);
    }

    @Override
    protected ServiceAccount desired(TapResource primary, Context<TapResource> context) {
        return new ServiceAccountBuilder()
                .withMetadata(new ObjectMetaBuilder()
                        .withName(COMPONENT)
                        .withNamespace(NamespaceResource.COMPONENT)
                        .build())
                .build();
    }

}
