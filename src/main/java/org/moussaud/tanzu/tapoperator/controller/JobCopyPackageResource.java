package org.moussaud.tanzu.tapoperator.controller;

import io.fabric8.kubernetes.api.model.batch.v1.Job;
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

        return super.desired(primary, context);
    }

}
