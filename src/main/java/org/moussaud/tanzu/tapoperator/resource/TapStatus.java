package org.moussaud.tanzu.tapoperator.resource;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.javaoperatorsdk.operator.api.ObservedGenerationAwareStatus;

public class TapStatus extends ObservedGenerationAwareStatus {

    private Boolean ready;

    public Boolean getReady() {
        return ready;
    }

    public TapStatus setReady(Boolean ready) {
        this.ready = ready;
        return this;
    }

    @Override
    public String toString() {
        return "TapStatus [ready=" + ready + "]";
    }

}
