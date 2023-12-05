package org.moussaud.tanzu.tapoperator.controller;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import org.moussaud.tanzu.tapoperator.resource.TapResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.CRUDKubernetesDependentResource;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependent;

@KubernetesDependent(labelSelector = "app.kubernetes.io/managed-by=tap-operator")
public class SecretCopyPackageResource extends CRUDKubernetesDependentResource<Secret, TapResource> {

    public static final String NAME = "tap-operator-credentials";
    private static final Logger log = LoggerFactory.getLogger(SecretCopyPackageResource.class);

    public SecretCopyPackageResource() {
        super(Secret.class);
    }

    @Override
    protected Secret desired(TapResource primary, Context<TapResource> context) {
        log.debug("Desired {} Secret", Utils.getSecretName(primary));

        return new SecretBuilder()
                .withMetadata(new ObjectMetaBuilder()
                        .withName(Utils.getSecretName(primary))
                        .withNamespace(primary.getMetadata().getNamespace())
                        .build())

                .withData(buildDataFromSecret(primary, context))
                .build();
    }

    Map<String, String> buildDataFromSecret(TapResource resource, Context<TapResource> context) {
        Secret secret = context.getClient().secrets().inNamespace(resource.getMetadata().getNamespace())
                .withName(resource.getSpec().getSecret()).get();
        if (secret == null) {
            throw new RuntimeException(String.format("{} secret not found in the {} namespace ",
                    resource.getSpec().getSecret(), resource.getMetadata().getNamespace()));
        }

        return Utils.computeEnvironmentVariables(resource, secret.getData());
    }

}
