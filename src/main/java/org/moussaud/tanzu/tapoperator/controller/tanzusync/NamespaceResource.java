package org.moussaud.tanzu.tapoperator.controller.tanzusync;

import org.moussaud.tanzu.tapoperator.resource.TapResource;

import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceBuilder;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.dependent.Deleter;
import io.javaoperatorsdk.operator.processing.dependent.Creator;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependentResource;

public class NamespaceResource extends KubernetesDependentResource<Namespace, TapResource>
        implements Creator<Namespace, TapResource>, Deleter<TapResource> {
    public static final String COMPONENT = "tanzu-sync";

    public NamespaceResource() {
        super(Namespace.class);
    }

    @Override
    protected Namespace desired(TapResource primary, Context<TapResource> context) {
        return new NamespaceBuilder()
                .withMetadata(new ObjectMetaBuilder()
                        .withName(COMPONENT)                       
                        .build())
                .build();
    }

}
