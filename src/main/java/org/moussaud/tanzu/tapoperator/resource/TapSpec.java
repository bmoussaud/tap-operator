package org.moussaud.tanzu.tapoperator.resource;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import io.fabric8.crd.generator.annotation.PrinterColumn;
import io.fabric8.generator.annotation.Required;

public class TapSpec {
 
    @Required
    @PrinterColumn(name = "TAP_VERSION")
    private String version;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

}
