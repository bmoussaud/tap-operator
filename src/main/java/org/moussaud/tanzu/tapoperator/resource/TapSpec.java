package org.moussaud.tanzu.tapoperator.resource;

import io.fabric8.crd.generator.annotation.PrinterColumn;
import io.fabric8.generator.annotation.Default;
import io.fabric8.generator.annotation.Required;
import org.apache.commons.lang3.ObjectUtils;
import org.moussaud.tanzu.tapoperator.controller.Utils;

public class TapSpec {

    @Required
    @PrinterColumn(name = "TAP_VERSION")
    private String version;

    private String clusterEssentialsBundleVersion;

    @Default("1.12.1")
    private String postgresVersion;
    //imgpkg tag list -i registry.tanzu.vmware.com/packages-for-vmware-tanzu-data-services/tds-packages

    @Default("tap-operator-registry-credentials")
    private String secret;
    private String url;

    @Default(".")
    private String subPath;

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


    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSubPath() {
        return subPath;
    }

    public void setSubPath(String subPath) {
        this.subPath = subPath;
    }

    public String getClusterEssentialsBundleVersion() {
        if (ObjectUtils.isNotEmpty(this.clusterEssentialsBundleVersion)) {
            return this.clusterEssentialsBundleVersion;
        } else {
            var computed = Utils._getClusterEssentialsBundleVersion(getVersion().trim());
            setClusterEssentialsBundleVersion(computed);
            return computed;
        }
    }

    public void setClusterEssentialsBundleVersion(String clusterEssentialsBundleVersion) {
        this.clusterEssentialsBundleVersion = clusterEssentialsBundleVersion;
    }

    public String getPostgresVersion() {
        return postgresVersion;
    }

    public void setPostgresVersion(String postgresVersion) {
        this.postgresVersion = postgresVersion;
    }
}
