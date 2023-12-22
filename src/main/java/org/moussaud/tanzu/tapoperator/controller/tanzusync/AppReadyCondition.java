package org.moussaud.tanzu.tapoperator.controller.tanzusync;

import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.dependent.DependentResource;
import io.javaoperatorsdk.operator.processing.dependent.workflow.Condition;
import io.k14s.kappctrl.v1alpha1.App;
import io.k14s.kappctrl.v1alpha1.AppStatus;
import org.moussaud.tanzu.tapoperator.resource.TapResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppReadyCondition implements Condition<App, TapResource> {
    private static final Logger log = LoggerFactory.getLogger(AppReadyCondition.class);

    @Override
    public boolean isMet(DependentResource<App, TapResource> dependentResource, TapResource primary, Context<TapResource> context) {
        return dependentResource.getSecondaryResource(primary, context)
                .map(app -> {
                    var name = primary.getMetadata().getName();
                    log.trace("is Met {} with app  {} ? ", name, app.getMetadata().getName());
                    var runningapp = context.getClient().resource(app).get();
                    if (runningapp == null) {
                        log.debug("running app {} not found", app.getMetadata().getName());
                        return false;
                    }
                    AppStatus status = runningapp.getStatus();
                    return true;
                }).orElse(false);
    }
}
