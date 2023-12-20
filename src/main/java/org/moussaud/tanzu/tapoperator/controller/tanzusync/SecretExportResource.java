package org.moussaud.tanzu.tapoperator.controller.tanzusync;

import io.javaoperatorsdk.operator.api.reconciler.Context;
import dev.carvel.secretgen.v1alpha1.*;

import java.util.Collections;

import org.moussaud.tanzu.tapoperator.resource.*;

public class SecretExportResource extends TanzuSyncResource<SecretExport> {
    public static final String COMPONENT = "install-registry-dockerconfig";

    public SecretExportResource() {
        super(SecretExport.class, COMPONENT);
    }

    @Override
    protected SecretExport desired(TapResource primary, Context<TapResource> context) {
        SecretExport desired = new SecretExport();
        desired.setMetadata(createMeta(primary).build());
        desired.setSpec(new SecretExportSpec());
        desired.getSpec().setToNamespaces(Collections.singletonList("*"));
        return desired;
    }

}
