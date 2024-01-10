package org.moussaud.tanzu.tapoperator.controller;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.Secret;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.ResourceIDMatcherDiscriminator;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.CRUDKubernetesDependentResource;
import io.javaoperatorsdk.operator.processing.event.ResourceID;
import org.jetbrains.annotations.NotNull;
import org.moussaud.tanzu.tapoperator.controller.tanzusync.TanzuSyncResource;
import org.moussaud.tanzu.tapoperator.resource.TapResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseResource<R extends HasMetadata> extends CRUDKubernetesDependentResource<R, TapResource>
        implements TapOperatorManagedResource {

    private static final Logger log = LoggerFactory.getLogger(BaseResource.class);
    private static final String K8S_NAME = "app.kubernetes.io/name";
    private static final String K8S_COMPONENT = "app.kubernetes.io/component";
    private static final String K8S_MANAGED_BY = "app.kubernetes.io/managed-by";
    private static final String K8S_OWNER = "tap-operator";

    protected final String component;

    public BaseResource(Class<R> resourceType, String component) {
        super(resourceType);
        this.component = component;
        log.trace("set Discriminator on {}/{}", resourceType.getSimpleName(), this.component);
        setResourceDiscriminator(new Discriminator(component));
    }

    class Discriminator
            extends ResourceIDMatcherDiscriminator<R, TapResource> {
        public Discriminator(String component) {
            super(p -> new ResourceID(name(p), namespace(p)));
        }
    }

    public String name(TapResource primary) {
        return "%s-%s".formatted(primary.getMetadata().getName(), component);
    }

    protected ObjectMetaBuilder createMeta(TapResource primary) {
        return new ObjectMetaBuilder()
                .withName(name(primary))
                .withNamespace(namespace(primary))
                .addToLabels(K8S_NAME, primary.getMetadata().getName())
                .addToLabels(K8S_COMPONENT, component)
                .addToLabels(K8S_MANAGED_BY, K8S_OWNER);
    }

    protected String namespace(TapResource primary) {
        return primary.getMetadata().getNamespace();
    }

    @NotNull
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
