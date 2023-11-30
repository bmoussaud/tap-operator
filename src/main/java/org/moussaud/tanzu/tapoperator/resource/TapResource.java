package org.moussaud.tanzu.tapoperator.resource;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.ShortNames;
import io.fabric8.kubernetes.model.annotation.Version;

@Group("org.moussaud.tanzu")
@Version("v1")
@ShortNames("tap")
public class TapResource extends CustomResource<TapSpec, TapStatus> implements Namespaced {

    public String toSXxxxxtring() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }

    @Override
    protected TapStatus initStatus() {
        return new TapStatus();
    }
}
