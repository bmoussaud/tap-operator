package org.moussaud.tanzu.tapoperator.controller.tanzusync;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.ResourceIDMatcherDiscriminator;
import io.javaoperatorsdk.operator.api.reconciler.dependent.Deleter;
import io.javaoperatorsdk.operator.processing.dependent.Creator;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependentResource;
import io.javaoperatorsdk.operator.processing.event.ResourceID;
import org.moussaud.tanzu.tapoperator.controller.BaseJobResource;
import org.moussaud.tanzu.tapoperator.resource.TapResource;

public class TanzuSyncResource<R extends HasMetadata> extends KubernetesDependentResource<R, TapResource>
        implements Creator<R, TapResource>, Deleter<TapResource> {
    private final String component;

    public TanzuSyncResource(Class<R> resourceType, String component) {
        super(resourceType);
        this.component = component;
        //setResourceDiscriminator(new TanzuSyncResource.Discriminator(component));
    }

    static class SecretDiscriminator
            extends ResourceIDMatcherDiscriminator<Secret, TapResource> {
        public SecretDiscriminator(String component) {
            super(p -> new ResourceID(component,
                    NamespaceResource.COMPONENT));
        }
    }


    private static final String K8S_NAME = "app.kubernetes.io/name";
    private static final String K8S_COMPONENT = "app.kubernetes.io/component";
    private static final String K8S_MANAGED_BY = "app.kubernetes.io/managed-by";
    private static final String K8S_OWNER = "tap-operator";

    protected ObjectMetaBuilder createMeta(TapResource primary) {
        return new ObjectMetaBuilder()
                .withName(this.component)
                .withNamespace(NamespaceResource.COMPONENT)
                .addToLabels(K8S_NAME, component)
                .addToLabels(K8S_COMPONENT, component)
                .addToLabels(K8S_MANAGED_BY, K8S_OWNER);
    }

    protected Secret getSecret(TapResource resource, Context<TapResource> context) {
        // TODO Fix this : why the CRD does not return the default value.
        var secretName = (resource.getSpec().getSecret() == null ? "tap-operator-registry-credentials"
                : resource.getSpec().getSecret());
        var secret = context.getClient().secrets().inNamespace(resource.getMetadata().getNamespace())
                .withName(secretName).get();
        if (secret == null) {
            throw new RuntimeException(String.format("%s secret not found in the %s namespace ",
                    secretName, resource.getMetadata().getNamespace()));
        }
        return secret;
    }
}
