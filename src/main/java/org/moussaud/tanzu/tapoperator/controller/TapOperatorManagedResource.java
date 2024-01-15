package org.moussaud.tanzu.tapoperator.controller;

import org.moussaud.tanzu.tapoperator.resource.TapResource;

public interface TapOperatorManagedResource {

    public String getComponent();

    default public String name(TapResource primary) {
        return "%s-%s".formatted(primary.getMetadata().getName(), getComponent());
    }
}
