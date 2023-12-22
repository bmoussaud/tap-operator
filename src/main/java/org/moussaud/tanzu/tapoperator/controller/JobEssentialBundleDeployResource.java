package org.moussaud.tanzu.tapoperator.controller;

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.batch.v1.Job;
import org.moussaud.tanzu.tapoperator.resource.TapResource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class JobEssentialBundleDeployResource extends BaseJobResource {

    public static final String COMPONENT = "essential-bundle-deploy";

    public JobEssentialBundleDeployResource() {
        super(Job.class, COMPONENT);
    }

    @Override
    protected List<Container> getContainer(TapResource primary) {
        var image = "ghcr.io/alexandreroman/tanzu-cluster-essentials-bootstrap:kbld-rand-1699444742098385476-8669215173182";
        return Collections.singletonList(new ContainerBuilder()
                .withName(COMPONENT)
                .withImage(image)
                .withSecurityContext(new SecurityContextBuilder().withRunAsUser(1000L).build())
                .withEnv(Arrays.asList(
                        new EnvVar("PACKAGE",
                                "tanzu-cluster-essentials/cluster-essentials-bundle",
                                null),
                        new EnvVar("VERSION", Utils.getClusterEssentialsBundleVersion(primary),
                                null)))
                .withEnvFrom(new EnvFromSourceBuilder()
                        .withNewSecretRef(getSecretName(primary), false)
                        .build())
                .build());
    }


}
