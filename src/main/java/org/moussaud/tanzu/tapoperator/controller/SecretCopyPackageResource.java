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

    public static final String NAME = "copy-package-credentials";
    private static final Logger log = LoggerFactory.getLogger(SecretCopyPackageResource.class);

    public SecretCopyPackageResource() {
        super(Secret.class);
    }

    @Override
    protected Secret desired(TapResource primary, Context<TapResource> context) {
        log.debug("Desired {} Secret", NAME);

        return new SecretBuilder()
                .withMetadata(new ObjectMetaBuilder()
                        .withName(NAME)
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
        Map<String, String> data = secret.getData();
        String install_bundle_value = decode(data.get("TO_REGISTRY_HOSTNAME"))
                + "/tanzu-cluster-essentials/cluster-essentials-bundle:" + resource.getSpec().getVersion();
        final Map<String, String> newData = Map.of(
                "IMGPKG_REGISTRY_HOSTNAME_0", data.get("FROM_REGISTRY_HOSTNAME"),
                "IMGPKG_REGISTRY_USERNAME_0", data.get("FROM_REGISTRY_USERNAME"),
                "IMGPKG_REGISTRY_PASSWORD_0", data.get("FROM_REGISTRY_PASSWORD"),
                "IMGPKG_REGISTRY_HOSTNAME_1", data.get("TO_REGISTRY_HOSTNAME"),
                "IMGPKG_REGISTRY_USERNAME_1", data.get("TO_REGISTRY_USERNAME"),
                "IMGPKG_REGISTRY_PASSWORD_1", data.get("TO_REGISTRY_PASSWORD"),
                "INSTALL_BUNDLE", encode(install_bundle_value),
                "INSTALL_REGISTRY_HOSTNAME", data.get("TO_REGISTRY_HOSTNAME"),
                "INSTALL_REGISTRY_USERNAME", data.get("TO_REGISTRY_USERNAME"),
                "INSTALL_REGISTRY_PASSWORD", data.get("TO_REGISTRY_PASSWORD"));
        log.debug("secret data {}", newData);
        return newData;
    }

    private String decode(String encoded) {
        byte[] decoded = Base64.getDecoder().decode(encoded);
        String decodedStr = new String(decoded, StandardCharsets.UTF_8);
        return decodedStr;
    }

    private String encode(String s) {
        try {
            String x = Base64.getEncoder().encodeToString(s.getBytes("UTF-8"));
            return x;
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return "X";
        }
    }
}
