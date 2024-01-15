package org.moussaud.tanzu.tapoperator.controller;

import io.fabric8.kubernetes.api.model.ServiceAccount;
import io.fabric8.kubernetes.api.model.ServiceAccountBuilder;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import org.moussaud.tanzu.tapoperator.resource.TapResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceAccountResource extends BaseResource<ServiceAccount> {
    public static final String COMPONENT = "sync-sa";

    private static final Logger log = LoggerFactory.getLogger(ServiceAccountResource.class);

    public ServiceAccountResource() {
        super(ServiceAccount.class, COMPONENT);
    }

    @Override
    protected ServiceAccount desired(TapResource primary, Context<TapResource> context) {
        log.info("Desired {} {}", name(primary), resourceType());
        return new ServiceAccountBuilder()
                .withMetadata(createMeta(primary).build())
                .build();
    }

}
