package org.moussaud.tanzu.tapoperator.controller.tanzusync;

import io.fabric8.kubernetes.api.model.GenericKubernetesResource;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.ServiceAccount;
import io.fabric8.kubernetes.api.model.ServiceAccountBuilder;
import io.fabric8.kubernetes.api.model.rbac.ClusterRoleBinding;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.javaoperatorsdk.operator.ReconcilerUtils;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.dependent.Deleter;
import io.javaoperatorsdk.operator.processing.dependent.Creator;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.CRUDKubernetesDependentResource;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependentResource;
import dev.carvel.secretgen.v1alpha1.*;

import java.util.Collections;

import org.moussaud.tanzu.tapoperator.resource.*;

public class SecretExportResource extends KubernetesDependentResource<SecretExport, TapResource>
        implements Creator<SecretExport, TapResource>, Deleter<TapResource> {
    public static final String COMPONENT = "install-registry-dockerconfig";

    public SecretExportResource() {
        super(SecretExport.class);
    }

    @Override
    protected SecretExport desired(TapResource primary, Context<TapResource> context) {
        SecretExport desired = new SecretExport();
        desired.setMetadata(new ObjectMetaBuilder()
                .withName(COMPONENT)
                .withNamespace(NamespaceResource.COMPONENT)
                .build());
        desired.setSpec(new SecretExportSpec());
        desired.getSpec().setToNamespaces(Collections.singletonList("*"));
        return desired;
    }

}
