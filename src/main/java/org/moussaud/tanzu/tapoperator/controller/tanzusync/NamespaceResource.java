package org.moussaud.tanzu.tapoperator.controller.tanzusync;

import org.moussaud.tanzu.tapoperator.resource.TapResource;

import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceBuilder;
import io.javaoperatorsdk.operator.api.reconciler.Context;

public class NamespaceResource extends TanzuSyncResource<Namespace> {
    public static final String COMPONENT = "tanzu-sync";

    public NamespaceResource() {
        super(Namespace.class, COMPONENT);
    }

    @Override
    protected Namespace desired(TapResource primary, Context<TapResource> context) {
        return new NamespaceBuilder()
                .withMetadata(createMeta(primary).build())
                .build();
    }

}
