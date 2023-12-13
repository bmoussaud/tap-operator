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

@KubernetesDependent(labelSelector = JobDeployEssentialBundleResource.SELECTOR, resourceDiscriminator = JobDeployEssentialBundleResource.Discriminator.class)
public class JobDeployEssentialBundleResource extends JobResource {

        public static final String COMPONENT = "essential-bundle-deploy";
        public static final String SELECTOR = K8S_MANAGED_BY + "=" + K8S_OWNER + "," + K8S_COMPONENT + "=" + COMPONENT;

        public JobDeployEssentialBundleResource() {
                super(Job.class, COMPONENT);
        }

        @Override
        protected List<Container> getContainer(TapResource primary) {
                var image = "ghcr.io/alexandreroman/tanzu-cluster-essentials-bootstrap:kbld-rand-1699444742098385476-8669215173182";
                return Collections.singletonList(new ContainerBuilder()
                                .withName(COMPONENT)
                                .withImage(image)
                                .withSecurityContext(new SecurityContextBuilder().withRunAsUser(1000L).build())
                                .withEnv(Arrays.asList(
                                                new EnvVar("PACKAGE",
                                                                "tanzu-cluster-essentials/cluster-essentials-bundle",
                                                                null),
                                                new EnvVar("VERSION", Utils.getClusterEssentialsBundleVersion(primary),
                                                                null)))
                                .withEnvFrom(new EnvFromSourceBuilder()
                                                .withNewSecretRef(getSecretName(primary), false)
                                                .build())
                                .build());
        }

        public static class Discriminator
                        extends ResourceIDMatcherDiscriminator<Job, TapResource> {
                public Discriminator() {
                        super(p -> new ResourceID(p.getMetadata().getName() + "-" + COMPONENT,
                                        p.getMetadata().getNamespace()));
                }
        }
}
