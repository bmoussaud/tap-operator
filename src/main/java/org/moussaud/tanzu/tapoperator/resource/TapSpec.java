package org.moussaud.tanzu.tapoperator.resource;

import io.fabric8.crd.generator.annotation.PrinterColumn;
import io.fabric8.generator.annotation.Default;
import io.fabric8.generator.annotation.Required;

public class TapSpec {
 
    @Required
    @PrinterColumn(name = "TAP_VERSION")
    private String version;

    @Default("tap-operator-copy-packages-credentials")
    private String secret;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    

}
