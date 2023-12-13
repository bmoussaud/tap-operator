package org.moussaud.tanzu.tapoperator.controller;

import java.time.Duration;

import org.moussaud.tanzu.tapoperator.resource.TapResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.javaoperatorsdk.operator.api.reconciler.Cleaner;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.ControllerConfiguration;
import io.javaoperatorsdk.operator.api.reconciler.DeleteControl;
import io.javaoperatorsdk.operator.api.reconciler.Reconciler;
import io.javaoperatorsdk.operator.api.reconciler.UpdateControl;
import io.javaoperatorsdk.operator.api.reconciler.dependent.Dependent;

@ControllerConfiguration(dependents = {
        @Dependent(name = SecretCopyPackageResource.COMPONENT, type = SecretCopyPackageResource.class),
        @Dependent(name = JobCopyEssentialBundleResource.COMPONENT, dependsOn = SecretCopyPackageResource.COMPONENT, type = JobCopyEssentialBundleResource.class, readyPostcondition = JobCopyEssentialBundleResourceReadyCondition.class),
        @Dependent(name = JobDeployEssentialBundleResource.COMPONENT, dependsOn = JobCopyEssentialBundleResource.COMPONENT, type = JobDeployEssentialBundleResource.class, readyPostcondition = JobCopyEssentialBundleResourceReadyCondition.class),
        @Dependent(name = JobCopyTapResource.COMPONENT, dependsOn = SecretCopyPackageResource.COMPONENT, type = JobCopyTapResource.class, readyPostcondition = JobCopyEssentialBundleResourceReadyCondition.class),
})
public class TapReconciler implements Reconciler<TapResource>, Cleaner<TapResource> {

    private static final Logger log = LoggerFactory.getLogger(TapReconciler.class);

    @Override
    public UpdateControl<TapResource> reconcile(TapResource resource, Context<TapResource> context) throws Exception {
        log.info("Reconciling: {}/{}", resource.getMetadata().getName(), resource.getSpec().getVersion());
        var ready = context.managedDependentResourceContext().getWorkflowReconcileResult().orElseThrow()
                .allDependentResourcesReady();
        resource.getStatus().setReady(ready);
        log.info("{}", resource.getStatus());
        if (resource.getStatus().getReady()) {
            log.info("Ready !");
            return UpdateControl.updateStatus(resource);
        }
        return UpdateControl.updateStatus(resource).rescheduleAfter(Duration.ofSeconds(10));
    }

    @Override
    public DeleteControl cleanup(TapResource resource, Context<TapResource> context) {
        log.info("Clean up: {}", resource.getMetadata().getName());
        return DeleteControl.defaultDelete();
    }

}
