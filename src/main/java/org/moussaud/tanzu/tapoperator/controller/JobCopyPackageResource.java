package org.moussaud.tanzu.tapoperator.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.EnvVarBuilder;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.PodSpecBuilder;
import io.fabric8.kubernetes.api.model.PodTemplateSpec;
import io.fabric8.kubernetes.api.model.PodTemplateSpecBuilder;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretEnvSourceBuilder;
import io.fabric8.kubernetes.api.model.SecurityContext;
import io.fabric8.kubernetes.api.model.SecurityContextBuilder;
import io.fabric8.kubernetes.api.model.ContainerFluent.EnvFromNested;
import io.fabric8.kubernetes.api.model.EnvFromSource;
import io.fabric8.kubernetes.api.model.EnvFromSourceBuilder;
import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.fabric8.kubernetes.api.model.batch.v1.JobBuilder;
import io.fabric8.kubernetes.api.model.batch.v1.JobSpec;
import io.fabric8.kubernetes.api.model.batch.v1.JobSpecBuilder;
import io.fabric8.openshift.api.model.hive.v1.SecretReference;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.CRUDKubernetesDependentResource;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependent;
import io.javaoperatorsdk.operator.springboot.starter.sample.CustomService;

@KubernetesDependent(labelSelector = "app.kubernetes.io/managed-by=custom-service-operator")
public class JobCopyPackageResource extends CRUDKubernetesDependentResource<Job, CustomService> {

    public JobCopyPackageResource() {
        super(Job.class);
    }

    @Override
    protected Job desired(CustomService primary, Context<CustomService> context) {
        List<EnvVar> vars = Arrays.asList(
                new EnvVar("PACKAGE", "tanzu-application-platform/tap-packages", null),
                new EnvVar("VERSION", "1.7.1-rc.7", null));
        EnvFromSource secret = new EnvFromSourceBuilder()
                .withNewSecretRef("tap-operator-copy-packages-credentials", false)
                .build();

        Container container = new ContainerBuilder()
                .withName("tap-operator")
                .withImage("tap-operator-copy-packages")
                .withSecurityContext(new SecurityContextBuilder().withRunAsUser(1000L).build())
                .withEnv(vars)
                .withEnvFrom(secret)
                .build();

        return new JobBuilder()
                .withMetadata(new ObjectMetaBuilder()
                        .withName(primary.getMetadata().getName() + "-job")
                        .withNamespace(primary.getMetadata().getNamespace())
                        .build())
                .withSpec(new JobSpecBuilder()
                        .withBackoffLimit(1)
                        .withActiveDeadlineSeconds(1800L)
                        .withTtlSecondsAfterFinished(120)
                        .withTemplate(new PodTemplateSpecBuilder()
                                .withSpec(new PodSpecBuilder()
                                        .withRestartPolicy("Never")
                                        .withServiceAccount("tap-operator-copy-packages-sa")
                                        .withContainers(container)
                                        .build())
                                .build())
                        .build())
                .build();

        // return super.desired(primary, context);
    }

}
