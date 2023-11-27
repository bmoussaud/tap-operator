package org.moussaud.tanzu.tapoperator.resource;

import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.ShortNames;
import io.fabric8.kubernetes.model.annotation.Version;

@Group("org.moussaud.tanzu")
@Version("v1")
@ShortNames("tap")
public class TapResource extends CustomResource<TapSpec, Void> implements Namespaced {

}
