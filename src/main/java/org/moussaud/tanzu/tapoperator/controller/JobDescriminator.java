package org.moussaud.tanzu.tapoperator.controller;

import java.util.Optional;

import org.moussaud.tanzu.tapoperator.resource.TapResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.javaoperatorsdk.operator.api.reconciler.Context;

import io.javaoperatorsdk.operator.api.reconciler.ResourceIDMatcherDiscriminator;
import io.javaoperatorsdk.operator.processing.event.ResourceID;

public class JobDescriminator
        extends ResourceIDMatcherDiscriminator<Job, TapResource> {

    private static final Logger log = LoggerFactory.getLogger(JobDescriminator.class);

    public JobDescriminator() {
        super(p -> new ResourceID(p.getMetadata().getName() + "_", p.getMetadata().getNamespace()));
    }

    @Override
    public Optional<Job> distinguish(Class<Job> resource, TapResource primary, Context<TapResource> context) {

        Optional<Job> result = super.distinguish(resource, primary, context);
        String x = "empty";
        if (!result.isEmpty()) {
            x = result.get().getMetadata().getName();
        }
        log.debug("* distinguish ({},{})=>{}", resource.getCanonicalName(), primary.getMetadata().getName(), x);
        return result;
    }

}
