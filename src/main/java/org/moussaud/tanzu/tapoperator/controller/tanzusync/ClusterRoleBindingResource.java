package org.moussaud.tanzu.tapoperator.controller.tanzusync;

import org.moussaud.tanzu.tapoperator.resource.TapResource;

import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.dependent.Deleter;
import io.javaoperatorsdk.operator.processing.dependent.Creator;
import io.fabric8.kubernetes.api.model.rbac.*;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependentResource;

public class ClusterRoleBindingResource extends KubernetesDependentResource<ClusterRoleBinding, TapResource>
                implements Creator<ClusterRoleBinding, TapResource>, Deleter<TapResource> {
        public static final String COMPONENT = "tanzu-sync-cluster-crb-admin";

        public ClusterRoleBindingResource() {
                super(ClusterRoleBinding.class);
        }

        @Override
        protected ClusterRoleBinding desired(TapResource primary, Context<TapResource> context) {

                return new ClusterRoleBindingBuilder()
                                .withMetadata(new ObjectMetaBuilder()
                                                .withName(COMPONENT)
                                                .withNamespace(NamespaceResource.COMPONENT)
                                                .build())
                                .withNewRoleRef("rbac.authorization.k8s.io", "ClusterRole",
                                                ClusterRoleResource.COMPONENT)
                                .withSubjects(
                                                new SubjectBuilder()
                                                                .withName(ServiceAccountResource.COMPONENT)
                                                                .withNamespace(NamespaceResource.COMPONENT)
                                                                .withKind("ServiceAccount")
                                                                .build())

                                .build();
        }

}
