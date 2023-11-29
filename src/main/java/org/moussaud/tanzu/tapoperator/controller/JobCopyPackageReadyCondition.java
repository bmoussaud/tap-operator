package org.moussaud.tanzu.tapoperator.controller;

import java.util.Optional;
import java.util.function.Function;

import org.moussaud.tanzu.tapoperator.resource.TapResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.dependent.DependentResource;
import io.javaoperatorsdk.operator.processing.dependent.workflow.Condition;

public class JobCopyPackageReadyCondition implements Condition<Job, TapResource> {

    private static final Logger log = LoggerFactory.getLogger(JobCopyPackageResource.class);

    @Override
    public boolean isMet(DependentResource<Job, TapResource> dependentResource, TapResource primary,
            Context<TapResource> context) {
        log.info("isMet {} ? " + primary.getFullResourceName());
        Optional<Job> job = dependentResource.getSecondaryResource(primary, context);
        return job.map(new Function<Job, Boolean>() {
            @Override
            public Boolean apply(Job t) {
                log.info("Query running job {}  in {}  ns", t.getMetadata().getName(),
                        primary.getMetadata().getNamespace());
                Job runningJob = context.getClient()
                        .batch()
                        .v1()
                        .jobs()
                        .inNamespace(primary.getMetadata().getNamespace())
                        .withName(t.getMetadata().getName()).get();
                log.info("Running Job {}", runningJob);
                log.info("Running Job Status {}", runningJob.getStatus());
                log.info("Running Job Status Succeeded {}", runningJob.getStatus().getSucceeded());
                if (runningJob.getStatus().getSucceeded() != null && runningJob.getStatus().getSucceeded() > 0) {
                    return Boolean.TRUE;
                } else {
                    return Boolean.FALSE;
                }
            }
        }).get();

    }

}
