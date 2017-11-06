package io.mangoo.core.yaml;

/**
 * 
 * @author svenkubiak
 *
 */
public class YamlRoute {
    private String method;
    private String url;
    private String mapping;
    private String username;
    private String password;
    private int limit;
    private boolean blocking;
    private boolean authentication;
    private boolean timer;
    
    public String getMethod() {
        return method;
    }
    
    public void setMethod(String method) {
        this.method = method;
    }
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    } 
    
    public void setLimit(int limit) {
        this.limit = limit;
    } 
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }         
    
    public String getMapping() {
        return mapping;
    }
    
    public int getLimit() {
        return limit;
    }
    
    public void setMapping(String mapping) {
        this.mapping = mapping;
    }
    
    public boolean isBlocking() {
        return blocking;
    }
    
    public void setBlocking(boolean blocking) {
        this.blocking = blocking;
    }
    
    public boolean isAuthentication() {
        return authentication;
    }
    
    public void setAuthentication(boolean authentication) {
        this.authentication = authentication;
    }
    
    public boolean isTimer() {
        return timer;
    }
    
    public void setTimer(boolean timer) {
        this.timer = timer;
    }
}