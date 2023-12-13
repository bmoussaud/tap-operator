package org.moussaud.tanzu.tapoperator.controller;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.moussaud.tanzu.tapoperator.resource.TapResource;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.EnvFromSourceBuilder;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.SecurityContextBuilder;
import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.javaoperatorsdk.operator.api.reconciler.ResourceIDMatcherDiscriminator;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependent;
import io.javaoperatorsdk.operator.processing.event.ResourceID;

@KubernetesDependent(labelSelector = JobPostgresCopyResource.SELECTOR, resourceDiscriminator = JobPostgresCopyResource.Discriminator.class)
public class JobPostgresCopyResource extends JobResource {

        public static final String COMPONENT = "postgres-copy";
        public static final String SELECTOR = K8S_MANAGED_BY + "=" + K8S_OWNER + "," + K8S_COMPONENT + "=" + COMPONENT;

        public JobPostgresCopyResource() {
                super(Job.class, COMPONENT);
        }

        // TODO imgpkg tag list -i
        // registry.tanzu.vmware.com/tanzu-sql-postgres/vmware-sql-postgres-operator
        @Override
        protected List<Container> getContainer(TapResource primary) {
                var image = "ghcr.io/bmoussaud/tap-operator-copy-packages:v0.0.3";
                final Container copy_essentials = new ContainerBuilder()
                                .withName(COMPONENT)
                                .withImage(image)
                                .withSecurityContext(new SecurityContextBuilder().withRunAsUser(1000L).build())
                                .withEnv(Arrays.asList(
                                                new EnvVar("PACKAGE",
                                                                "packages-for-vmware-tanzu-data-services/tds-packages",
                                                                null),
                                                new EnvVar("VERSION", Utils.getPostgresVersion(primary),
                                                                null)))
                                .withEnvFrom(new EnvFromSourceBuilder()
                                                .withNewSecretRef(getSecretName(primary), false)
                                                .build())
                                .build();
                return Collections.singletonList(copy_essentials);
        }

        public static class Discriminator
                        extends ResourceIDMatcherDiscriminator<Job, TapResource> {
                public Discriminator() {
                        super(p -> new ResourceID(p.getMetadata().getName() + "-" + COMPONENT,
                                        p.getMetadata().getNamespace()));
                }
        }

}
