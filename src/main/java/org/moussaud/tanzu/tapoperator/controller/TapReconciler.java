package org.moussaud.tanzu.tapoperator.controller;

import io.javaoperatorsdk.operator.api.reconciler.*;
import io.javaoperatorsdk.operator.api.reconciler.dependent.Dependent;
import org.moussaud.tanzu.tapoperator.controller.tanzusync.*;
import org.moussaud.tanzu.tapoperator.resource.TapResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

@ControllerConfiguration(dependents = {
        @Dependent(name = SecretResource.COMPONENT, type = SecretResource.class),

        @Dependent(name = JobEssentialBundleCopyResource.COMPONENT,
                dependsOn = SecretResource.COMPONENT,
                type = JobEssentialBundleCopyResource.class,
                readyPostcondition = JobReadyCondition.class),
        @Dependent(name = JobEssentialBundleDeployResource.COMPONENT,
                dependsOn = JobEssentialBundleCopyResource.COMPONENT,
                type = JobEssentialBundleDeployResource.class,
                readyPostcondition = JobReadyCondition.class),
        @Dependent(name = JobTapCopyResource.COMPONENT,
                dependsOn = SecretResource.COMPONENT,
                type = JobTapCopyResource.class,
                readyPostcondition = JobReadyCondition.class),
        @Dependent(name = JobPostgresCopyResource.COMPONENT,
                dependsOn = SecretResource.COMPONENT,
                type = JobPostgresCopyResource.class,
                readyPostcondition = JobReadyCondition.class),
        @Dependent(name = NamespaceResource.COMPONENT, type = NamespaceResource.class,
                dependsOn = {JobEssentialBundleDeployResource.COMPONENT, JobTapCopyResource.COMPONENT, JobPostgresCopyResource.COMPONENT}
        ),
        @Dependent(name = ServiceAccountResource.COMPONENT, dependsOn = NamespaceResource.COMPONENT, type = ServiceAccountResource.class),
        @Dependent(name = ClusterRoleResource.COMPONENT, type = ClusterRoleResource.class),
        @Dependent(name = ClusterRoleBindingResource.COMPONENT, type = ClusterRoleBindingResource.class),
        @Dependent(name = SecretExportResource.COMPONENT, type = SecretExportResource.class, dependsOn = NamespaceResource.COMPONENT),
        @Dependent(name = SecretInstallRegistryDockerConfigResource.COMPONENT + "-sec", type = SecretInstallRegistryDockerConfigResource.class, dependsOn = NamespaceResource.COMPONENT),
        @Dependent(name = SecretSyncAgeIdentityResource.COMPONENT, type = SecretSyncAgeIdentityResource.class, dependsOn = NamespaceResource.COMPONENT),
        @Dependent(name = SecretSyncGitResource.COMPONENT, type = SecretSyncGitResource.class, dependsOn = NamespaceResource.COMPONENT),
        @Dependent(name = AppResource.COMPONENT, type = AppResource.class,
                dependsOn = {ServiceAccountResource.COMPONENT, SecretSyncGitResource.COMPONENT, SecretSyncAgeIdentityResource.COMPONENT, SecretInstallRegistryDockerConfigResource.COMPONENT + "-sec", SecretExportResource.COMPONENT, ClusterRoleBindingResource.COMPONENT, ClusterRoleResource.COMPONENT},
                readyPostcondition = AppReadyCondition.class, deletePostcondition = AppDeleteCondition.class)
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
