package org.moussaud.tanzu.tapoperator.controller;

import org.moussaud.tanzu.tapoperator.resource.TapResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.dependent.DependentResource;
import io.javaoperatorsdk.operator.processing.dependent.workflow.Condition;

public class JobReadyCondition implements Condition<Job, TapResource> {

    private static final Logger log = LoggerFactory.getLogger(JobReadyCondition.class);

    @Override
    public boolean isMet(DependentResource<Job, TapResource> dependentResource, TapResource primary,
                         Context<TapResource> context) {
        return dependentResource.getSecondaryResource(primary, context)
                .map(job -> {
                    TapOperatorManagedResource tomr = (TapOperatorManagedResource) dependentResource;
                    log.trace("is Met {} with job {} ? ", primary.getMetadata().getName(), tomr.name(primary));
                    var runningJob = context.getClient().batch().v1().jobs()
                            .inNamespace(primary.getMetadata().getNamespace())
                            .withName(tomr.name(primary)).get();
                    if (runningJob == null) {
                        log.info("Running {} job not found, job's gone or wrong search !",
                                primary.getMetadata().getName());
                        return false;
                    }
                    log.info("isMet:{} runningJob: {} Status: {} ? ", primary.getMetadata().getName(), tomr.name(primary), runningJob.getStatus());
                    var succeeed = runningJob.getStatus().getSucceeded();
                    return succeeed != null && succeeed > 0;
                })
                .orElse(false);
    }

}
