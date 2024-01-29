package org.moussaud.tanzu.tapoperator.controller;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.javaoperatorsdk.operator.api.reconciler.dependent.Deleter;
import io.javaoperatorsdk.operator.processing.dependent.Creator;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependentResource;
import org.moussaud.tanzu.tapoperator.resource.TapResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClusterBaseResource<R extends HasMetadata> extends KubernetesDependentResource<R, TapResource>
        implements Creator<R, TapResource>, Deleter<TapResource>, TapOperatorManagedResource {

    private static final Logger log = LoggerFactory.getLogger(ClusterBaseResource.class);
    private final String component;

    public ClusterBaseResource(Class<R> resourceType, String component) {
        super(resourceType);
        this.component = component;
    }

    @Override
    public String name(TapResource primary) {
        return component;
    }

    public String namespace(TapResource primary) {
        return primary.getMetadata().getNamespace();
    }


    @Override
    public String getComponent() {
        return component;
    }

    private static final String K8S_NAME = "app.kubernetes.io/name";
    private static final String K8S_COMPONENT = "app.kubernetes.io/component";
    private static final String K8S_MANAGED_BY = "app.kubernetes.io/managed-by";
    private static final String K8S_OWNER = "tap-operator";

    protected ObjectMetaBuilder createMeta(TapResource primary) {
        return new ObjectMetaBuilder()
                .withName(name(primary))
                .addToLabels(K8S_NAME, component)
                .addToLabels(K8S_COMPONENT, component)
                .addToLabels(K8S_MANAGED_BY, K8S_OWNER);
    }


}
