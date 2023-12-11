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

//, readyPostcondition = JobCopyPackageReadyCondition.class
//  @Dependent(name = SecretCopyPackageResource.NAME, type = SecretCopyPackageResource.class)
@ControllerConfiguration(dependents = {
        @Dependent(name = JobCopyEssentialBundleResource.COMPONENT, type = JobCopyEssentialBundleResource.class, readyPostcondition = JobCopyEssentialBundleResourceReadyCondition.class),
        @Dependent(name = JobDeployEssentialBundleResource.COMPONENT, type = JobDeployEssentialBundleResource.class, readyPostcondition = JobCopyEssentialBundleResourceReadyCondition.class, dependsOn = JobCopyEssentialBundleResource.COMPONENT),
})
public class TapReconciler implements Reconciler<TapResource>, Cleaner<TapResource> {

    private static final Logger log = LoggerFactory.getLogger(TapReconciler.class);

    @Override
    public UpdateControl<TapResource> reconcile(TapResource resource, Context<TapResource> context) throws Exception {
        log.info("Reconciling: {}/{}", resource.getMetadata().getName(), resource.getSpec().getVersion());

        var ready = context.managedDependentResourceContext().getWorkflowReconcileResult().orElseThrow()
                .allDependentResourcesReady();
        log.info("ready ? {}", ready);
        resource.getStatus().setReady(ready);

        log.info("get Status:" + resource.getStatus());
        if (resource.getStatus().getReady()) {
            log.info("Ready !");
            // return UpdateControl.updateStatus(resource);
        }
        return UpdateControl.updateStatus(resource).rescheduleAfter(Duration.ofSeconds(10));
    }

    /*
     * private TapResource updateTapResourceStatus(TapResource resource,
     * Context<TapResource> context) {
     * log.trace("Query the {} jobs in the {} namespace",
     * resource.getMetadata().getName(),
     * resource.getMetadata().getNamespace());
     * Job runningJob = context.getClient()
     * .batch()
     * .v1()
     * .jobs()
     * .inNamespace(resource.getMetadata().getNamespace())
     * .withName(Utils.getJobName(resource)).get();
     * if (runningJob == null) {
     * log.trace("Running {} job not found, job's gone",
     * resource.getMetadata().getName());
     * resource.getStatus().setCopyPackageStatus(TapReconcilerStatus.DONE);
     * return resource;
     * }
     * 
     * log.trace("Running Job {}", runningJob);
     * JobStatus status = Objects.requireNonNullElse(runningJob.getStatus(), new
     * JobStatus());
     * log.trace("Status {}", status);
     * int succeeded = Objects.requireNonNullElse(status.getSucceeded(), 0);
     * int failed = Objects.requireNonNullElse(status.getFailed(), 0);
     * log.trace("is job succeeded {} / failed {} ?", succeeded, failed);
     * if (failed > 0) {
     * resource.getStatus().setCopyPackageStatus(TapReconcilerStatus.FAILED);
     * deleteJob(resource, context);
     * } else {
     * if (succeeded > 0) {
     * resource.getStatus().setCopyPackageStatus(TapReconcilerStatus.DONE);
     * deleteJob(resource, context);
     * } else {
     * resource.getStatus().setCopyPackageStatus(TapReconcilerStatus.INPROGESS);
     * }
     * }
     * return resource;
     * }
     */

    private void deleteJob(TapResource resource, Context<TapResource> context) {
        if (1 == 0) {
            String jobName = Utils.getJobName(resource);
            log.trace("Delete job: {}", jobName);
            context.getClient()
                    .batch()
                    .v1()
                    .jobs()
                    .inNamespace(resource.getMetadata().getNamespace())
                    .withName(jobName).delete();
        }

    }

    @Override
    public DeleteControl cleanup(TapResource resource, Context<TapResource> context) {
        log.info("Clean up: {}", resource.getMetadata().getName());
        return DeleteControl.defaultDelete();
    }

}
