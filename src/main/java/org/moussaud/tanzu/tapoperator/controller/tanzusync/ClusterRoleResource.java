package org.moussaud.tanzu.tapoperator.controller.tanzusync;

import org.moussaud.tanzu.tapoperator.resource.TapResource;

import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.rbac.ClusterRole;
import io.fabric8.kubernetes.api.model.rbac.ClusterRoleBuilder;
import io.fabric8.kubernetes.api.model.rbac.PolicyRuleBuilder;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.dependent.Deleter;
import io.javaoperatorsdk.operator.processing.dependent.Creator;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependentResource;

public class ClusterRoleResource extends KubernetesDependentResource<ClusterRole, TapResource>
        implements Creator<ClusterRole, TapResource>, Deleter<TapResource> {
    public static final String COMPONENT = "tanzu-sync-cluster-admin";

    public ClusterRoleResource() {
        super(ClusterRole.class);
    }

    @Override
    protected ClusterRole desired(TapResource primary, Context<TapResource> context) {
        return new ClusterRoleBuilder()
                .withMetadata(new ObjectMetaBuilder()
                        .withName(COMPONENT)
                        .withNamespace(NamespaceResource.COMPONENT)
                        .build())
                .withRules(new PolicyRuleBuilder()
                        .withApiGroups("*")
                        .withResources("*")
                        .withVerbs("*")
                        .build())
                .build();
    }

}
