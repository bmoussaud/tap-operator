package org.moussaud.tanzu.tapoperator.controller;

import io.fabric8.kubernetes.api.model.rbac.ClusterRole;
import io.fabric8.kubernetes.api.model.rbac.ClusterRoleBuilder;
import io.fabric8.kubernetes.api.model.rbac.PolicyRuleBuilder;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import org.moussaud.tanzu.tapoperator.resource.TapResource;

public class ClusterRoleResourceCluster extends ClusterBaseResource<ClusterRole> {
    public static final String COMPONENT = "tap-operator-cluster-admin";

    public ClusterRoleResourceCluster() {
        super(ClusterRole.class, COMPONENT);
    }

    @Override
    protected ClusterRole desired(TapResource primary, Context<TapResource> context) {
        return new ClusterRoleBuilder()
                .withMetadata(createMeta(primary).build())
                .withRules(new PolicyRuleBuilder()
                        .withApiGroups("*")
                        .withResources("*")
                        .withVerbs("*")
                        .build())
                .build();
    }

}
