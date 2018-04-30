package io.mangoo.models;

/**
 * @author svenkubiak
 *
 */
public class Subject {
    private String username;
    private boolean authentication;
    
    public Subject(String username, boolean authenticated) {
        this.username = username;
        this.authentication = authenticated;
    }
    
    public String getUsername() {
        return this.username;
    }
    
    public boolean isAuthenticated() {
        return this.authentication;
    }
}