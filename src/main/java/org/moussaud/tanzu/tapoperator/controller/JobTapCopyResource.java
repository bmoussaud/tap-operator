package org.moussaud.tanzu.tapoperator.controller;

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.batch.v1.Job;
import org.moussaud.tanzu.tapoperator.resource.TapResource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class JobTapCopyResource extends BaseJobResource {

    public static final String COMPONENT = "tap-copy";
    public static final String TAP_PACKAGES = "tanzu-application-platform/tap-packages";

    public JobTapCopyResource() {
        super(Job.class, COMPONENT);
    }

    @Override
    protected List<Container> getContainer(TapResource primary) {
        var image = "ghcr.io/bmoussaud/tap-operator-copy-packages:v0.0.3";

        final Container copy_essentials = new ContainerBuilder()
                .withName(COMPONENT)
                .withImage(image)
                .withSecurityContext(new SecurityContextBuilder().withRunAsUser(1000L).build())
                .withEnv(Arrays.asList(
                        new EnvVar("PACKAGE",
                                TAP_PACKAGES,
                                null),
                        new EnvVar("VERSION", primary.getSpec().getVersion(),
                                null)))
                .withEnvFrom(new EnvFromSourceBuilder()
                        .withNewSecretRef(getSecretName(primary), false)
                        .build())
                .build();
        return Collections.singletonList(copy_essentials);
    }


}
