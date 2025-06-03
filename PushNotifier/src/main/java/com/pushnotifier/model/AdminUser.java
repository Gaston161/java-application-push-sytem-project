package com.pushnotifier.model;

public class AdminUser {
    private int id;
    private String username;
    private String passwordHash;

    public AdminUser(int id, String username, String passwordHash) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
    }
    // getters/setters si nécessaire
}
