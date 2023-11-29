package org.moussaud.tanzu.tapoperator.resource;

import io.javaoperatorsdk.operator.api.ObservedGenerationAwareStatus;

public class TapStatus extends ObservedGenerationAwareStatus {

    private String copyPackageStatus;

    public String getCopyPackageStatus() {
        return copyPackageStatus;
    }

    public void setCopyPackageStatus(String copyPackageStatus) {
        this.copyPackageStatus = copyPackageStatus;
    }

}
