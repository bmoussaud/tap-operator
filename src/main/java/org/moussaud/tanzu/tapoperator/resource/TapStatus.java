package org.moussaud.tanzu.tapoperator.resource;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.javaoperatorsdk.operator.api.ObservedGenerationAwareStatus;

public class TapStatus extends ObservedGenerationAwareStatus {

    private String copyPackageStatus;

    public String getCopyPackageStatus() {
        return copyPackageStatus;
    }

    public void setCopyPackageStatus(String copyPackageStatus) {
        this.copyPackageStatus = copyPackageStatus;
    }

    @JsonIgnore
    public void setCopyPackageStatus(TapReconcilerStatus copyPackageStatus) {
        this.copyPackageStatus = copyPackageStatus.toString();
    }

    @JsonIgnore
    public boolean  isCopyPackageStatusInProgress() {
        return getCopyPackageStatus().equalsIgnoreCase(TapReconcilerStatus.INPROGESS.toString());
    }
}
