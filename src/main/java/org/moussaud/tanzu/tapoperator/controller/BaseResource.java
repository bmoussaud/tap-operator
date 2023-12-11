package org.moussaud.tanzu.tapoperator.controller;

import java.util.List;

import org.moussaud.tanzu.tapoperator.resource.TapResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.PodSpecBuilder;
import io.fabric8.kubernetes.api.model.PodTemplateSpecBuilder;
import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.fabric8.kubernetes.api.model.batch.v1.JobBuilder;
import io.fabric8.kubernetes.api.model.batch.v1.JobSpecBuilder;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.processing.dependent.Creator;
import io.javaoperatorsdk.operator.processing.dependent.Updater;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.CRUDKubernetesDependentResource;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependentResource;
import io.javaoperatorsdk.operator.processing.event.source.filter.OnUpdateFilter;

public abstract class BaseResource extends CRUDKubernetesDependentResource<Job, TapResource>
                implements TapOperatorManagedResource {

        public static final String K8S_NAME = "app.kubernetes.io/name";
        public static final String K8S_COMPONENT = "app.kubernetes.io/component";
        public static final String K8S_MANAGED_BY = "app.kubernetes.io/managed-by";
        public static final String K8S_OWNER = "tap-operator";

        private final String component;

        public BaseResource(Class<Job> resourceType, String component) {
                super(resourceType);
                this.component = component;
        }

        private static final Logger log = LoggerFactory.getLogger(JobDeployEssentialBundleResource.class);

        public String name(TapResource primary) {
                return "%s-%s".formatted(primary.getMetadata().getName(), component);
        }

        protected ObjectMetaBuilder createMeta(TapResource primary) {
                String name = name(primary);
                return new ObjectMetaBuilder()
                                .withName(name)
                                .withNamespace(primary.getMetadata().getNamespace())
                                .addToLabels(K8S_NAME, primary.getMetadata().getName())
                                .addToLabels(K8S_COMPONENT, component)
                                .addToLabels(K8S_MANAGED_BY, K8S_OWNER);
        }

        protected abstract List<Container> getContainer(TapResource primary);

        @Override
        protected Job desired(TapResource primary, Context<TapResource> context) {
                log.debug("Desired {} ", name(primary));
                // var actual = getSecondaryResource(primary, context);
                // log.debug("-> actual Job {}", actual);
                return new JobBuilder()
                                .withMetadata(createMeta(primary).build())
                                .withSpec(new JobSpecBuilder()
                                                .withBackoffLimit(1)
                                                .withActiveDeadlineSeconds(1800L)
                                                .withTtlSecondsAfterFinished(10000)
                                                .withTemplate(new PodTemplateSpecBuilder()
                                                                .withSpec(new PodSpecBuilder()
                                                                                .withRestartPolicy("Never")
                                                                                .withServiceAccount(
                                                                                                "tap-operator")
                                                                                .withContainers(getContainer(primary))
                                                                                .build())
                                                                .build())
                                                .build())
                                .build();

        }

        public static class UpdateFiter implements OnUpdateFilter<Job> {

                public UpdateFiter() {
                }

                @Override
                public boolean accept(Job newResource, Job oldResource) {
                        log.error("UPDATE !!!! ");
                        return true;
                }

        }

        @Override
        public Job create(Job desired, TapResource primary, Context<TapResource> context) {
                deleteCurrentJob(desired, context);
                log.debug("proceed create with actual {}/{}", desired.getMetadata().getNamespace(),
                                desired.getMetadata().getName());
                return super.create(desired, primary, context);
        }

        public Job update(Job actual, Job desired, TapResource primary, Context<TapResource> context) {
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
