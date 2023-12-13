package org.moussaud.tanzu.tapoperator.controller;

import org.moussaud.tanzu.tapoperator.resource.TapResource;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.CRUDKubernetesDependentResource;

public abstract class BaseResource<R extends HasMetadata> extends CRUDKubernetesDependentResource<R , TapResource>
                implements TapOperatorManagedResource {

        public static final String K8S_NAME = "app.kubernetes.io/name";
        public static final String K8S_COMPONENT = "app.kubernetes.io/component";
        public static final String K8S_MANAGED_BY = "app.kubernetes.io/managed-by";
        public static final String K8S_OWNER = "tap-operator";

        protected final String component;

        public BaseResource(Class<R> resourceType, String component) {
                super(resourceType);
                this.component = component;
        }

        public String name(TapResource primary) {
                return "%s-%s".formatted(primary.getMetadata().getName(), component);
        }

        protected ObjectMetaBuilder createMeta(TapResource primary) {
                String name = name(primary);
                return new ObjectMetaBuilder()
                                .withName(name)
                                .withNamespace(primary.getMetadata().getNamespace())
                                .addToLabels(K8S_NAME, primary.getMetadata().getName())
                                .addToLabels(K8S_COMPONENT, component)
                                .addToLabels(K8S_MANAGED_BY, K8S_OWNER);
        }

}
