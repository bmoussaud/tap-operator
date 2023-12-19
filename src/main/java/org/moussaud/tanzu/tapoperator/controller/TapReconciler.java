package org.moussaud.tanzu.tapoperator.controller;

import java.time.Duration;

import org.moussaud.tanzu.tapoperator.controller.tanzusync.*;
import org.moussaud.tanzu.tapoperator.resource.TapResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.javaoperatorsdk.operator.api.reconciler.Cleaner;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.ControllerConfiguration;
import io.javaoperatorsdk.operator.api.reconciler.DeleteControl;
import io.javaoperatorsdk.operator.api.reconciler.Reconciler;
import io.javaoperatorsdk.operator.api.reconciler.UpdateControl;
import io.javaoperatorsdk.operator.api.reconciler.dependent.Dependent;

@ControllerConfiguration(dependents = {
        // @Dependent(name = SecretResource.COMPONENT, type = SecretResource.class),
        // @Dependent(name = JobEssentialBundleCopyResource.COMPONENT, dependsOn =
        // SecretResource.COMPONENT, type = JobEssentialBundleCopyResource.class,
        // readyPostcondition = JobReadyCondition.class),
        // @Dependent(name = JobEssentialBundleDeployResource.COMPONENT, dependsOn =
        // JobEssentialBundleCopyResource.COMPONENT, type =
        // JobEssentialBundleDeployResource.class, readyPostcondition =
        // JobReadyCondition.class),
        // @Dependent(name = JobTapCopyResource.COMPONENT, dependsOn =
        // SecretResource.COMPONENT, type = JobTapCopyResource.class, readyPostcondition
        // = JobReadyCondition.class),
        // @Dependent(name = JobPostgresCopyResource.COMPONENT, dependsOn =
        // SecretResource.COMPONENT, type = JobPostgresCopyResource.class,
        // readyPostcondition = JobReadyCondition.class),
        @Dependent(name = NamespaceResource.COMPONENT, type = NamespaceResource.class),
        @Dependent(name = ServiceAccountResource.COMPONENT, dependsOn = NamespaceResource.COMPONENT, type = ServiceAccountResource.class),
        @Dependent(name = ClusterRoleResource.COMPONENT, type = ClusterRoleResource.class),
        @Dependent(name = ClusterRoleBindingResource.COMPONENT, type = ClusterRoleBindingResource.class),
        @Dependent(name = SecretExportResource.COMPONENT, type = SecretExportResource.class, dependsOn = NamespaceResource.COMPONENT),
        @Dependent(name = InstallRegistryDockerConfigSecretResource.COMPONENT + "-sec", type = InstallRegistryDockerConfigSecretResource.class, dependsOn = NamespaceResource.COMPONENT),
})
public class TapReconciler implements Reconciler<TapResource>, Cleaner<TapResource> {

    private static final Logger log = LoggerFactory.getLogger(TapReconciler.class);

    @Override
    public UpdateControl<TapResource> reconcile(TapResource resource, Context<TapResource> context) throws Exception {
        log.info("Reconciling: {}/{}", resource.getMetadata().getName(), resource.getSpec().getVersion());
        var ready = context
                .managedDependentResourceContext()
                .getWorkflowReconcileResult()
                .orElseThrow()
                .allDependentResourcesReady();
        resource.getStatus().setReady(ready);
        log.info("{}", resource.getStatus());
        if (resource.getStatus().getReady()) {
            log.info("Ready !");
            return UpdateControl.updateStatus(resource);
        }
        return UpdateControl.updateStatus(resource).rescheduleAfter(Duration.ofSeconds(10));
    }

    @Override
    public DeleteControl cleanup(TapResource resource, Context<TapResource> context) {
        log.info("Clean up: {}", resource.getMetadata().getName());
        return DeleteControl.defaultDelete();
    }
}
