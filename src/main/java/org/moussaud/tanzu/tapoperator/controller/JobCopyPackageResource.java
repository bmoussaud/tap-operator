package org.moussaud.tanzu.tapoperator.controller;

import java.util.Arrays;
import java.util.List;

import org.moussaud.tanzu.tapoperator.resource.TapResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.EnvFromSource;
import io.fabric8.kubernetes.api.model.EnvFromSourceBuilder;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.EnvVarBuilder;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.PodSpecBuilder;
import io.fabric8.kubernetes.api.model.PodTemplateSpecBuilder;
import io.fabric8.kubernetes.api.model.SecurityContextBuilder;
import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.fabric8.kubernetes.api.model.batch.v1.JobBuilder;
import io.fabric8.kubernetes.api.model.batch.v1.JobSpecBuilder;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.CRUDKubernetesDependentResource;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependent;

@KubernetesDependent(labelSelector = "app.kubernetes.io/managed-by=tap-operator")
public class JobCopyPackageResource extends CRUDKubernetesDependentResource<Job, TapResource> {

        private static final Logger log = LoggerFactory.getLogger(JobCopyPackageResource.class);
        public static final String NAME = "copy-package-job";

        public static String getJobName(String resourceName) {
                return resourceName + NAME;
        }

        public JobCopyPackageResource() {
                super(Job.class);
        }

        @Override
        protected Job desired(TapResource primary, Context<TapResource> context) {
                log.debug("Desired {} ", getJobName(primary.getMetadata().getName()));

                // String packagePath = "tanzu-application-platform/tap-packages";
                // packagePath = "tanzu-cluster-essentials/cluster-essentials-bundle";
                String image = "ghcr.io/bmoussaud/tap-operator-copy-packages:v0.0.3";

                Container copy_essentials = new ContainerBuilder()
                                .withName("copy-cluster-essentials-bundle")
                                .withImage(image)
                                .withSecurityContext(new SecurityContextBuilder().withRunAsUser(1000L).build())
                                .withEnv(Arrays.asList(
                                                new EnvVar("PACKAGE",
                                                                "tanzu-cluster-essentials/cluster-essentials-bundle",
                                                                null),
                                                new EnvVar("VERSION", primary.getSpec().getVersion(), null)))
                                .withEnvFrom(new EnvFromSourceBuilder()
                                                .withNewSecretRef(SecretCopyPackageResource.NAME, false)
                                                .build())
                                .build();

                Container deploy_essential = new ContainerBuilder()
                                .withName("deploy-essential")
                                .withImage("ghcr.io/alexandreroman/tanzu-cluster-essentials-bootstrap:kbld-rand-1699444742098385476-8669215173182")
                                .withSecurityContext(new SecurityContextBuilder().withRunAsUser(1000L).build())
                                // .withEnv(Arrays.asList(
                                // new EnvVar("INSTALL_BUNDLE", "tanzu-application-platform/tap-packages",
                                // null)))
                                .withEnvFrom(new EnvFromSourceBuilder()
                                                .withNewSecretRef(SecretCopyPackageResource.NAME, false)
                                                .build())
                                .build();

                Container copy_tap_packages = new ContainerBuilder()
                                .withName("copy-tap-packages")
                                .withImage(image)
                                .withSecurityContext(new SecurityContextBuilder().withRunAsUser(1000L).build())
                                .withEnv(Arrays.asList(
                                                new EnvVar("PACKAGE", "tanzu-application-platform/tap-packages", null),
                                                new EnvVar("VERSION", primary.getSpec().getVersion(), null)))
                                .withEnvFrom(new EnvFromSourceBuilder()
                                                .withNewSecretRef(SecretCopyPackageResource.NAME, false)
                                                .build())
                                .build();

                return new JobBuilder()
                                .withMetadata(new ObjectMetaBuilder()
                                                .withName(getJobName(primary.getMetadata().getName()))
                                                .withNamespace(primary.getMetadata().getNamespace())
                                                .build())
                                .withSpec(new JobSpecBuilder()
                                                .withBackoffLimit(1)
                                                .withActiveDeadlineSeconds(1800L)
                                                .withTtlSecondsAfterFinished(100)
                                                .withTemplate(new PodTemplateSpecBuilder()
                                                                .withSpec(new PodSpecBuilder()
                                                                                .withRestartPolicy("Never")
                                                                                .withServiceAccount(
                                                                                                "tap-operator")
                                                                                .withContainers(copy_essentials,
                                                                                                deploy_essential)
                                                                                .build())
                                                                .build())
                                                .build())
                                .build();

        }

}
