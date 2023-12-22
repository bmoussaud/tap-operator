package org.moussaud.tanzu.tapoperator.controller.tanzusync;

import io.fabric8.kubernetes.api.model.ServiceAccount;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.k14s.kappctrl.v1alpha1.App;
import io.k14s.kappctrl.v1alpha1.AppSpec;
import io.k14s.kappctrl.v1alpha1.appspec.Deploy;
import io.k14s.kappctrl.v1alpha1.appspec.Fetch;
import io.k14s.kappctrl.v1alpha1.appspec.Template;
import io.k14s.kappctrl.v1alpha1.appspec.deploy.Kapp;
import io.k14s.kappctrl.v1alpha1.appspec.fetch.Git;
import io.k14s.kappctrl.v1alpha1.appspec.fetch.git.SecretRef;
import io.k14s.kappctrl.v1alpha1.appspec.template.Sops;
import io.k14s.kappctrl.v1alpha1.appspec.template.Ytt;
import io.k14s.kappctrl.v1alpha1.appspec.template.sops.Age;
import io.k14s.kappctrl.v1alpha1.appspec.template.sops.age.PrivateKeysSecretRef;
import io.k14s.kappctrl.v1alpha1.appspec.template.ytt.ValuesFrom;
import org.moussaud.tanzu.tapoperator.resource.TapResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

public class AppResource extends TanzuSyncResource<App> {
    public static final String COMPONENT = "sync";

    private static final Logger log = LoggerFactory.getLogger(TanzuSyncResource.class);

    public AppResource() {
        super(App.class, COMPONENT);
    }

    @Override
    protected App desired(TapResource primary, Context<TapResource> context) {
        App desired = new App();
        desired.setMetadata(createMeta(primary).build());
        AppSpec spec = new AppSpec();
        desired.setSpec(spec);
        spec.setServiceAccountName(ServiceAccountResource.COMPONENT);
        Git git = new Git();
        git.setRef("origin/main");
        git.setUrl(primary.getSpec().getUrl());
        git.setSubPath(primary.getSpec().getSubPath());
        var secret = new SecretRef();
        secret.setName(SecretSyncGitResource.COMPONENT);
        git.setSecretRef(secret);
        Fetch fetch = new Fetch();
        fetch.setGit(git);
        spec.setFetch(Collections.singletonList(fetch));

        var pkr = new PrivateKeysSecretRef();
        pkr.setName(SecretSyncAgeIdentityResource.COMPONENT);
        var age = new Age();
        age.setPrivateKeysSecretRef(pkr);
        var sops = new Sops();
        sops.setAge(age);
        var template = new Template();
        template.setSops(sops);
        spec.setTemplate(Collections.singletonList(template));

        var valuesFrom = new ValuesFrom();
        valuesFrom.setPath("values");
        var ytt = new Ytt();
        ytt.setPaths(Collections.singletonList("config"));
        ytt.setValuesFrom(Collections.singletonList(valuesFrom));
        template.setYtt(ytt);

        var deploy = new Deploy();
        deploy.setKapp(new Kapp());
        spec.setDeploy(Collections.singletonList(deploy));

        return desired;
    }

    @Override
    protected void handleDelete(TapResource primary, App secondary, Context<TapResource> context) {
        super.handleDelete(primary, secondary, context);
    }
}
