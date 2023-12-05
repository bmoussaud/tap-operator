package org.moussaud.tanzu.tapoperator.controller;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.moussaud.tanzu.tapoperator.resource.TapReconcilerStatus;
import org.moussaud.tanzu.tapoperator.resource.TapResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

//, readyPostcondition = JobCopyPackageReadyCondition.class
@ControllerConfiguration(dependents = {
        @Dependent(name = JobCopyPackageResource.NAME, type = JobCopyPackageResource.class),
        @Dependent(name = SecretCopyPackageResource.NAME, type = SecretCopyPackageResource.class)
})
public class TapReconciler implements Reconciler<TapResource>, Cleaner<TapResource> {

    private static final Logger log = LoggerFactory.getLogger(TapReconciler.class);

    private final KubernetesClient kubernetesClient;

    public TapReconciler(KubernetesClient kubernetesClient) {
        this.kubernetesClient = kubernetesClient;
    }

    @Override
    public UpdateControl<TapResource> reconcile(TapResource resource, Context<TapResource> context) throws Exception {
        log.info("Reconciling: {}/{}", resource.getMetadata().getName(), resource.getSpec().getVersion());
        TapResource updated = updateTapResourceStatus(resource, context);
        log.info(
                "Updating status of TapResource {} in namespace {} to {} status",
                updated.getMetadata().getName(),
                updated.getMetadata().getNamespace(),
                updated.getStatus().getCopyPackageStatus());

        if (updated.getStatus().isCopyPackageStatusInProgress()) {
            return UpdateControl.updateStatus(updated).rescheduleAfter(2, TimeUnit.SECONDS);
        } else {
            return UpdateControl.updateStatus(updated);
        }
    }

    private TapResource updateTapResourceStatus(TapResource resource, Context<TapResource> context) {
        log.trace("Query the {} jobs in the {} namespace", resource.getMetadata().getName(),
                resource.getMetadata().getNamespace());
        Job runningJob = context.getClient()
                .batch()
                .v1()
                .jobs()
                .inNamespace(resource.getMetadata().getNamespace())
                .withName(Utils.getJobName(resource)).get();
        if (runningJob == null) {
            log.trace("Running {} job not found, job's gone", resource.getMetadata().getName());
            resource.getStatus().setCopyPackageStatus(TapReconcilerStatus.DONE);
            return resource;
        }

        log.trace("Running Job {}", runningJob);
        JobStatus status = Objects.requireNonNullElse(runningJob.getStatus(), new JobStatus());
        log.trace("Status {}", status);
        int succeeded = Objects.requireNonNullElse(status.getSucceeded(), 0);
        int failed = Objects.requireNonNullElse(status.getFailed(), 0);
        log.trace("is job succeeded {} / failed {} ?", succeeded, failed);
        if (failed > 0) {
            resource.getStatus().setCopyPackageStatus(TapReconcilerStatus.FAILED);
            deleteJob(resource, context);
        } else {
            if (succeeded > 0) {
                resource.getStatus().setCopyPackageStatus(TapReconcilerStatus.DONE);
                deleteJob(resource, context);
            } else {
                resource.getStatus().setCopyPackageStatus(TapReconcilerStatus.INPROGESS);
            }
        }
        return resource;
    }

    private void deleteJob(TapResource resource, Context<TapResource> context) {
        String jobName = Utils.getJobName(resource);
        log.trace("Delete job: {}", jobName);
        if (1 == 0) {
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
