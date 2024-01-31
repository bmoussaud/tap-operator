package org.moussaud.tanzu.tapoperator.controller;

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.fabric8.kubernetes.api.model.batch.v1.JobBuilder;
import io.fabric8.kubernetes.api.model.batch.v1.JobSpecBuilder;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import org.moussaud.tanzu.tapoperator.resource.TapResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
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
        List<Container> containers = getContainer(primary);
        //log.warn("{} mode dev use sleep ", this.getClass());
        containers = getDefaultContainer(primary);
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
                                        .withContainers(containers)
                                        .build())
                                .build())
                        .build())
                .build();
    }

    protected List<Container> getDefaultContainer(TapResource primary) {
        var image = "ghcr.io/bmoussaud/tap-operator@sha256:8b93667ca276be679d55482fc7138aa049be56ee44460d920bca6bb04b9b1352";
        final Container copy_essentials = new ContainerBuilder()
                .withName("dummy")
                .withImage(image)
                .withSecurityContext(new SecurityContextBuilder().withRunAsUser(1000L).build())
                .withEnv(Arrays.asList(
                        new EnvVar("PACKAGE", "tanzu-cluster-essentials/cluster-essentials-bundle", null),
                        new EnvVar("VERSION_TAP", primary.getSpec().getVersion(), null),
                        new EnvVar("VERSION_ESS", Utils.getClusterEssentialsBundleVersion(primary), null)))
                .build();
        return Collections.singletonList(copy_essentials);
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
