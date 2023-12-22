package org.moussaud.tanzu.tapoperator.controller.tanzusync;

import io.k14s.kappctrl.v1alpha1.App;
import org.moussaud.tanzu.tapoperator.resource.TapResource;

import io.fabric8.kubernetes.api.model.ServiceAccount;
import io.fabric8.kubernetes.api.model.ServiceAccountBuilder;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceAccountResource extends TanzuSyncResource<ServiceAccount> {
    public static final String COMPONENT = "sync-sa";

    private static final Logger log = LoggerFactory.getLogger(ServiceAccountResource.class);

    public ServiceAccountResource() {
        super(ServiceAccount.class, COMPONENT);
    }

    @Override
    protected ServiceAccount desired(TapResource primary, Context<TapResource> context) {
        return new ServiceAccountBuilder()
                .withMetadata(createMeta(primary).build())
                .build();
    }

    @Override
    protected void handleDelete(TapResource primary, ServiceAccount secondary, Context<TapResource> context) {
        var desired = new AppResource().desired(primary, context);
        log.warn("Pre-Delete {} {}/{}", primary.getMetadata().getName(), desired.getKind(), desired.getMetadata().getName());
        context.getClient().resource(desired).delete();
        super.handleDelete(primary, secondary, context);
    }

}
