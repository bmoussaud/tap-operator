package org.moussaud.tanzu.tapoperator.resource;

public enum TapReconcilerStatus {

    DONE("DONE"),
    INPROGESS("IN_PROGRESS"),
    FAILED("FAILED");

    private TapReconcilerStatus(String name) {
        this.name = name;
    }

    private String name;

    @Override
    public String toString() {
        return name;
    }
}