package org.moussaud.tanzu.tapoperator.controller;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.moussaud.tanzu.tapoperator.resource.TapResource;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.web.ServerProperties.Tomcat;
import org.slf4j.Logger;

import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.fabric8.kubernetes.api.model.batch.v1.JobStatus;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.javaoperatorsdk.operator.api.reconciler.Cleaner;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.ControllerConfiguration;
import io.javaoperatorsdk.operator.api.reconciler.DeleteControl;
import io.javaoperatorsdk.operator.api.reconciler.Reconciler;
import io.javaoperatorsdk.operator.api.reconciler.UpdateControl;
import io.javaoperatorsdk.operator.api.reconciler.dependent.Dependent;
import io.javaoperatorsdk.operator.processing.dependent.Updater;
import io.fabric8.kubernetes.api.model.apps.DeploymentStatus;

//, readyPostcondition = JobCopyPackageReadyCondition.class
@ControllerConfiguration(dependents = {
        @Dependent(name = "copy-package-job", type = JobCopyPackageResource.class)
})
public class TapReconciler implements Reconciler<TapResource>, Cleaner<TapResource>
// , Updater<Job, TapResource>
{

    private static final String DONE = "DONE";
    private static final String INPROGESS = "IN_PROGESS";
    private static final Logger log = LoggerFactory.getLogger(TapReconciler.class);

    private final KubernetesClient kubernetesClient;

    public TapReconciler(KubernetesClient kubernetesClient) {
        this.kubernetesClient = kubernetesClient;
    }

    @Override
    public UpdateControl<TapResource> reconcile(TapResource resource, Context<TapResource> context) throws Exception {
        log.info("Reconciling: {}/{}", resource.getMetadata().getName(), resource.getSpec().getVersion());

        // UpdateControl<TapResource> noUpdate = UpdateControl.noUpdate();
        // return noUpdate.rescheduleAfter(1, TimeUnit.SECONDS);

        // .rescheduleAfter(1, TimeUnit.SECONDS);

        TapResource updated = updateTapResourceStatus(resource, context);
        log.info(
                "Updating status of TapResource {} in namespace {} to {} status",
                updated.getMetadata().getName(),
                updated.getMetadata().getNamespace(),
                updated.getStatus().getCopyPackageStatus());
        if (updated.getStatus().getCopyPackageStatus().equalsIgnoreCase(INPROGESS)) {
            return UpdateControl.patchStatus(updated).rescheduleAfter(1, TimeUnit.SECONDS);
        } else {
            return UpdateControl.patchStatus(updated);
        }
    }

    private TapResource updateTapResourceStatus(TapResource resource, Context<TapResource> context) {
        log.info("Query {} running job in thr {} namespace", resource.getMetadata().getName(),
                resource.getMetadata().getNamespace());
        Job runningJob = context.getClient()
                .batch()
                .v1()
                .jobs()
                .inNamespace(resource.getMetadata().getNamespace())
                .withName(JobCopyPackageResource.getJobName(resource.getMetadata().getName())).get();
        // log.info("Running Job {}", runningJob);
        JobStatus status = Objects.requireNonNullElse(runningJob.getStatus(), new JobStatus());
        log.info("status {}", status);
        int succeeded = Objects.requireNonNullElse(status.getSucceeded(), 0);
        // log.info("is succeeded {} ?", succeeded);
        if (succeeded > 0) {
            resource.getStatus().setCopyPackageStatus(DONE);
        } else {
            resource.getStatus().setCopyPackageStatus(INPROGESS);
        }
        return resource;
    }

    @Override
    public DeleteControl cleanup(TapResource resource, Context<TapResource> context) {
        log.info("Clean up: {}", resource.getMetadata().getName());
        return DeleteControl.defaultDelete();
    }

}
