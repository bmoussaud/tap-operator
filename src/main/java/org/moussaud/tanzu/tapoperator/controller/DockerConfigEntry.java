package org.moussaud.tanzu.tapoperator.controller;

import java.io.UnsupportedEncodingException;
import java.util.Base64;

class DockerConfigEntry {
    private String auth;
    private String password;
    private String username;

    public DockerConfigEntry(String username, String password) {
        this.username = username;
        this.password = password;
        this.auth = encode(String.format("%s:%s", this.username, this.password));
    }

    public String getAuth() {
        return auth;
    }

    public void setAuth(String auth) {
        this.auth = auth;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    private static String encode(String s) {
        try {
            return Base64.getEncoder().encodeToString(s.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return "X";
        }
    }
}
