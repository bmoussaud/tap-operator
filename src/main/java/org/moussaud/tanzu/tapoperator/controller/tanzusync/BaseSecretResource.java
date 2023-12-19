package org.moussaud.tanzu.tapoperator.controller.tanzusync;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.Secret;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.dependent.Deleter;
import io.javaoperatorsdk.operator.processing.dependent.Creator;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.CRUDKubernetesDependentResource;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependentResource;
import org.jetbrains.annotations.NotNull;
import org.moussaud.tanzu.tapoperator.controller.BaseResource;
import org.moussaud.tanzu.tapoperator.resource.TapResource;

public abstract class BaseSecretResource extends KubernetesDependentResource<Secret, TapResource>
        implements Creator<Secret, TapResource>, Deleter<TapResource> {


    private static final String K8S_NAME = "app.kubernetes.io/name";
    private static final String K8S_COMPONENT = "app.kubernetes.io/component";
    private static final String K8S_MANAGED_BY = "app.kubernetes.io/managed-by";
    private static final String K8S_OWNER = "tap-operator";

    private final String component;

    public BaseSecretResource(String component) {
        super(Secret.class);
        this.component = component;
    }

    public String name(TapResource primary) {
        return component;
    }

    protected String getNamespace(TapResource primary) {
        return "tanzu-sync";
    }

    protected ObjectMetaBuilder createMeta(TapResource primary) {
        String name = name(primary);
        return new ObjectMetaBuilder()
                .withName(name)
                .withNamespace(getNamespace(primary))
                .addToLabels(K8S_NAME, primary.getMetadata().getName())
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
