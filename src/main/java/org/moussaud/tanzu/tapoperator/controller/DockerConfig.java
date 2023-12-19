package org.moussaud.tanzu.tapoperator.controller;

import java.util.HashMap;
import java.util.Map;

class DockerConfig {
    private Map<String, DockerConfigEntry> auths = new HashMap<>();

    public Map<String, DockerConfigEntry> getAuths() {
        return auths;
    }

    public void setAuths(Map<String, DockerConfigEntry> auths) {
        this.auths = auths;
    }

    public void add(String hostname, DockerConfigEntry entry) {
        auths.put(hostname, entry);
    }
}
