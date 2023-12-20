package org.moussaud.tanzu.tapoperator.controller.tanzusync;

import org.moussaud.tanzu.tapoperator.resource.TapResource;

import io.fabric8.kubernetes.api.model.rbac.ClusterRole;
import io.fabric8.kubernetes.api.model.rbac.ClusterRoleBuilder;
import io.fabric8.kubernetes.api.model.rbac.PolicyRuleBuilder;
import io.javaoperatorsdk.operator.api.reconciler.Context;

public class ClusterRoleResource extends TanzuSyncResource<ClusterRole> {
    public static final String COMPONENT = "tanzu-sync-cluster-admin";

    public ClusterRoleResource() {
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
