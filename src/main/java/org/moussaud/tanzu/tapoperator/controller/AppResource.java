package org.moussaud.tanzu.tapoperator.controller;

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
import io.k14s.kappctrl.v1alpha1.appspec.template.ytt.valuesfrom.ConfigMapRef;
import org.moussaud.tanzu.tapoperator.resource.TapResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static java.util.List.of;


public class AppResource extends BaseResource<App> {
    public static final String COMPONENT = "sync";

    private static final Logger log = LoggerFactory.getLogger(AppResource.class);

    public AppResource() {
        super(App.class, COMPONENT);
    }

    @Override
    protected App desired(TapResource primary, Context<TapResource> context) {

        log.debug("Desired {} {}", name(primary), resourceType());

        App desired = new App();
        desired.setMetadata(createMeta(primary).build());

        Map<String, String> annotations = new HashMap<>();
        annotations.put("kapp.k14s.io/change-group", "tanzu-sync");
        desired.getMetadata().setAnnotations(annotations);

        AppSpec spec = new AppSpec();
        desired.setSpec(spec);
        spec.setServiceAccountName(new ServiceAccountResource().name(primary));
        Git git = new Git();
        git.setRef("origin/main");
        git.setUrl(primary.getSpec().getUrl());
        git.setSubPath(primary.getSpec().getSubPath());
        var secret = new SecretRef();
        secret.setName(new SecretSyncGitResource().name(primary));
        git.setSecretRef(secret);
        Fetch fetch = new Fetch();
        fetch.setGit(git);
        spec.setFetch(Collections.singletonList(fetch));

        var pkr = new PrivateKeysSecretRef();
        pkr.setName(new SecretSyncAgeIdentityResource().name(primary));
        var age = new Age();
        age.setPrivateKeysSecretRef(pkr);
        var sops = new Sops();
        sops.setAge(age);

        var template_sops = new Template();
        template_sops.setSops(sops);

        var valuesFromValues = new ValuesFrom();
        valuesFromValues.setPath("values");

        var configMapRefToInstallValues = new ConfigMapRef();
        configMapRefToInstallValues.setName(new ConfigMapInstallValuesResource().name(primary));
        var valuesFromInstallValuesCM = new ValuesFrom();
        valuesFromInstallValuesCM.setConfigMapRef(configMapRefToInstallValues);

        var configMapRefToTapGui = new ConfigMapRef();
        configMapRefToTapGui.setName(new ConfigMapTapGuiResource().name(primary));
        var valuesFromInstallValuesTapGui = new ValuesFrom();
        valuesFromInstallValuesTapGui.setConfigMapRef(configMapRefToTapGui);

        var secretRef = new io.k14s.kappctrl.v1alpha1.appspec.template.ytt.valuesfrom.SecretRef();
        secretRef.setName(new SecretTapSensitiveImageRegistryResource().name(primary));
        var valuesFromSecret = new ValuesFrom();
        valuesFromSecret.setSecretRef(secretRef);

        var ytt = new Ytt();
        ytt.setPaths(Collections.singletonList("config"));
        ytt.setValuesFrom(of(valuesFromValues, valuesFromInstallValuesCM, valuesFromInstallValuesTapGui, valuesFromSecret));
        var template_ytt = new Template();
        template_ytt.setYtt(ytt);

        spec.setTemplate(of(template_sops, template_ytt));

        var deploy = new Deploy();
        deploy.setKapp(new Kapp());
        spec.setDeploy(Collections.singletonList(deploy));

        return desired;
    }

    public String name(TapResource primary) {
        return getComponent();
    }


    public boolean isDeletable() {
        log.trace("isDeletable  {} but True...to trigger the deletion of the App", super.isDeletable());
        return true;
    }

}

