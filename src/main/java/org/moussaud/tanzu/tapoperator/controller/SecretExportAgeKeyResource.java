package org.moussaud.tanzu.tapoperator.controller;

import dev.carvel.secretgen.v1alpha1.SecretExport;
import dev.carvel.secretgen.v1alpha1.SecretExportSpec;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import org.moussaud.tanzu.tapoperator.resource.TapResource;

import java.util.Collections;

public class SecretExportAgeKeyResource extends BaseResource<SecretExport> {
    public static final String COMPONENT = "sync-age-identity";

    public SecretExportAgeKeyResource() {
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
