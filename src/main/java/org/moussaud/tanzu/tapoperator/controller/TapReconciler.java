package org.moussaud.tanzu.tapoperator.controller;

import io.javaoperatorsdk.operator.api.reconciler.*;
import io.javaoperatorsdk.operator.api.reconciler.dependent.Dependent;
import org.moussaud.tanzu.tapoperator.resource.TapResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

@ControllerConfiguration(dependents = {

        @Dependent(name = SecretResource.COMPONENT, type = SecretResource.class),
        @Dependent(type = ConfigMapInstallValuesResource.class),
        @Dependent(name = ServiceAccountResource.COMPONENT, type = ServiceAccountResource.class),
        @Dependent(name = ClusterRoleResourceCluster.COMPONENT, type = ClusterRoleResourceCluster.class),
        @Dependent(name = ClusterRoleBindingResourceCluster.COMPONENT, type = ClusterRoleBindingResourceCluster.class),

        @Dependent(name = JobEssentialBundleCopyResource.COMPONENT,
                dependsOn = {SecretResource.COMPONENT, ServiceAccountResource.COMPONENT},
                type = JobEssentialBundleCopyResource.class,
                readyPostcondition = JobReadyCondition.class),
        @Dependent(name = JobEssentialBundleDeployResource.COMPONENT,
                dependsOn = {JobEssentialBundleCopyResource.COMPONENT},
                type = JobEssentialBundleDeployResource.class,
                readyPostcondition = JobReadyCondition.class),
        @Dependent(name = JobTapCopyResource.COMPONENT,
                dependsOn = {SecretResource.COMPONENT, ServiceAccountResource.COMPONENT},
                type = JobTapCopyResource.class,
                readyPostcondition = JobReadyCondition.class),
        @Dependent(name = JobPostgresCopyResource.COMPONENT,
                dependsOn = {SecretResource.COMPONENT, ServiceAccountResource.COMPONENT},
                type = JobPostgresCopyResource.class,
                readyPostcondition = JobReadyCondition.class),

        @Dependent(name = SecretExportInstallRegistryResource.COMPONENT + "-se", type = SecretExportInstallRegistryResource.class, dependsOn = JobEssentialBundleDeployResource.COMPONENT),
        @Dependent(name = SecretExportAgeKeyResource.COMPONENT + "-se", type = SecretExportAgeKeyResource.class, dependsOn = JobEssentialBundleDeployResource.COMPONENT),
        @Dependent(name = SecretInstallRegistryDockerConfigResource.COMPONENT, type = SecretInstallRegistryDockerConfigResource.class),
        @Dependent(name = SecretSyncAgeIdentityResource.COMPONENT, type = SecretSyncAgeIdentityResource.class),
        @Dependent(name = SecretSyncGitResource.COMPONENT, type = SecretSyncGitResource.class),
        @Dependent(name = SecretTapSensitiveImageRegistryResource.COMPONENT, type = SecretTapSensitiveImageRegistryResource.class),
        @Dependent(name = AppResource.COMPONENT, type = AppResource.class,
                dependsOn = {
                        JobEssentialBundleCopyResource.COMPONENT,
                        JobEssentialBundleDeployResource.COMPONENT,
                        JobPostgresCopyResource.COMPONENT,
                        JobTapCopyResource.COMPONENT},
                readyPostcondition = AppReadyCondition.class,
                deletePostcondition = AppDeleteCondition.class)

})
public class TapReconciler implements Reconciler<TapResource>, Cleaner<TapResource> {

    private static final Logger log = LoggerFactory.getLogger(TapReconciler.class);


    @Override
    public UpdateControl<TapResource> reconcile(TapResource resource, Context<TapResource> context) throws Exception {
        log.debug("Reconciling: {}/{}", resource.getMetadata().getName(), resource.getSpec().getVersion());

        var ready = context
                .managedDependentResourceContext()
                .getWorkflowReconcileResult()
                .orElseThrow()
                .allDependentResourcesReady();
        resource.getStatus().setReady(ready);
        log.debug("{}", resource.getStatus());
        if (resource.getStatus().getReady()) {
            log.info("Ready !");
            return UpdateControl.noUpdate();
        }
        return UpdateControl.updateStatus(resource).rescheduleAfter(Duration.ofSeconds(10));
    }

    @Override
    public DeleteControl cleanup(TapResource resource, Context<TapResource> context) {
        log.debug("Clean up: {}", resource.getMetadata().getName());
        return DeleteControl.defaultDelete();
    }
}
