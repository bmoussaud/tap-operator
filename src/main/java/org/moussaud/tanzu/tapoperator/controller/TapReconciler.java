package org.moussaud.tanzu.tapoperator.controller;

import org.moussaud.tanzu.tapoperator.resource.TapResource;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.ControllerConfiguration;
import io.javaoperatorsdk.operator.api.reconciler.Reconciler;
import io.javaoperatorsdk.operator.api.reconciler.UpdateControl;
import io.javaoperatorsdk.operator.api.reconciler.dependent.Dependent;
import io.javaoperatorsdk.operator.springboot.starter.sample.CustomServiceReconciler;

@ControllerConfiguration(dependents = {
        @Dependent(name = "config", type = JobCopyPackageResource.class)
})
public class TapReconciler implements Reconciler<TapResource> {

    private static final Logger log = LoggerFactory.getLogger(CustomServiceReconciler.class);

    private final KubernetesClient kubernetesClient;

    public TapReconciler(KubernetesClient kubernetesClient) {
        this.kubernetesClient = kubernetesClient;
    }

    @Override
    public UpdateControl<TapResource> reconcile(TapResource resource, Context<TapResource> context) throws Exception {
        log.info("Reconciling: {}", resource.getMetadata().getName());
        return UpdateControl.updateResource(resource);
    }

}
