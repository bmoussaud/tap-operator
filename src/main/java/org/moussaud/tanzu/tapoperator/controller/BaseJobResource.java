package org.moussaud.tanzu.tapoperator.controller;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.PodSpecBuilder;
import io.fabric8.kubernetes.api.model.PodTemplateSpecBuilder;
import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.fabric8.kubernetes.api.model.batch.v1.JobBuilder;
import io.fabric8.kubernetes.api.model.batch.v1.JobSpecBuilder;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import org.moussaud.tanzu.tapoperator.resource.TapResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public abstract class BaseJobResource extends BaseResource<Job> {

    private static final Logger log = LoggerFactory.getLogger(BaseJobResource.class);

    public BaseJobResource(Class<Job> resourceType, String component) {
        super(resourceType, component);
    }

    protected String getSecretName(TapResource primary) {
        return new SecretResource().name(primary);
    }

    protected abstract List<Container> getContainer(TapResource primary);

    @Override
    protected Job desired(TapResource primary, Context<TapResource> context) {
        log.debug("Desired {} {}", name(primary), resourceType());
        return new JobBuilder()
                .withMetadata(createMeta(primary).build())
                .withSpec(new JobSpecBuilder()
                        .withBackoffLimit(1)
                        .withActiveDeadlineSeconds(1800L)
                        .withTtlSecondsAfterFinished(10000)
                        .withTemplate(new PodTemplateSpecBuilder()
                                .withSpec(new PodSpecBuilder()
                                        .withRestartPolicy("Never")
                                        .withServiceAccount(new ServiceAccountResource().name(primary))
                                        .withContainers(getContainer(primary))
                                        .build())
                                .build())
                        .build())
                .build();
    }

    @Override
    public Job create(Job desired, TapResource primary, Context<TapResource> context) {
        log.debug("Create {} {}", name(primary), resourceType());
        deleteCurrentJob(desired, context);
        log.debug("proceed create with actual {}/{}", desired.getMetadata().getNamespace(),
                desired.getMetadata().getName());
        return super.create(desired, primary, context);
    }

    @Override
    public Job update(Job actual, Job desired, TapResource primary, Context<TapResource> context) {
        log.debug("Update {} {}", name(primary), resourceType());
        deleteCurrentJob(actual, context);
        log.debug("proceed update with actual {}/{}", actual.getMetadata().getNamespace(),
                actual.getMetadata().getName());
        return super.update(actual, desired, primary, context);
    }

    private void deleteCurrentJob(Job actual, Context<TapResource> context) {
        log.debug("delete actual version {}/{}", actual.getMetadata().getNamespace(),
                actual.getMetadata().getName());
        context.getClient().resource(actual).delete();
    }

}
