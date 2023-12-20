package org.moussaud.tanzu.tapoperator.controller.tanzusync;

import org.moussaud.tanzu.tapoperator.resource.TapResource;

import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.fabric8.kubernetes.api.model.rbac.*;

public class ClusterRoleBindingResource extends TanzuSyncResource<ClusterRoleBinding> {
    public static final String COMPONENT = "tanzu-sync-cluster-crb-admin";

    public ClusterRoleBindingResource() {
        super(ClusterRoleBinding.class, COMPONENT);
    }

    @Override
    protected ClusterRoleBinding desired(TapResource primary, Context<TapResource> context) {

        return new ClusterRoleBindingBuilder()
                .withMetadata(createMeta(primary).build())
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
