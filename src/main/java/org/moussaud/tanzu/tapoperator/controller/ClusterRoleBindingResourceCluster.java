package org.moussaud.tanzu.tapoperator.controller;

import io.fabric8.kubernetes.api.model.rbac.ClusterRoleBinding;
import io.fabric8.kubernetes.api.model.rbac.ClusterRoleBindingBuilder;
import io.fabric8.kubernetes.api.model.rbac.SubjectBuilder;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import org.moussaud.tanzu.tapoperator.resource.TapResource;

public class ClusterRoleBindingResourceCluster extends ClusterBaseResource<ClusterRoleBinding> {
    public static final String COMPONENT = "tap-operator-cluster-crb-admin";

    public ClusterRoleBindingResourceCluster() {
        super(ClusterRoleBinding.class, COMPONENT);
    }

    @Override
    protected ClusterRoleBinding desired(TapResource primary, Context<TapResource> context) {

        return new ClusterRoleBindingBuilder()
                .withMetadata(createMeta(primary).build())
                .withNewRoleRef("rbac.authorization.k8s.io", "ClusterRole",
                        ClusterRoleResourceCluster.COMPONENT)
                .withSubjects(
                        new SubjectBuilder()
                                .withName(new ServiceAccountResource().name(primary))
                                .withNamespace(new ServiceAccountResource().namespace(primary))
                                .withKind("ServiceAccount")
                                .build())
                .build();
    }

}
