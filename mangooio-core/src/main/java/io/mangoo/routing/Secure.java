package io.mangoo.routing;

import java.util.Objects;

import io.mangoo.enums.Required;
import io.mangoo.enums.SecureType;

/**
 * 
 * @author svenkubiak
 *
 */
public class Secure {
    private Class<?> controllerClass;
    private SecureType secureType;
    private String url;
    private String controllerMethod;
    private String[] urls;

    public static Secure url(String url) {
        Objects.requireNonNull(url, Required.URL.toString());
        
        Secure secure = new Secure();
        secure.secureType = SecureType.URL;
        secure.url = url;
        
        return secure;
    }
    
    public static Secure controller(Class<?> clazz) {
        Objects.requireNonNull(clazz, Required.CONTROLLER_CLASS.toString());
        
        Secure secure = new Secure();
        secure.secureType = SecureType.CONTROLLER;
        secure.controllerClass = clazz;
        
        return secure;
    }
    
    public static Secure method(Class<?> clazz, String method) {
        Objects.requireNonNull(clazz, Required.CONTROLLER_CLASS.toString());
        Objects.requireNonNull(clazz, Required.CONTROLLER_METHOD.toString());
        
        Secure secure = new Secure();
        secure.secureType = SecureType.METHOD;
        secure.controllerClass = clazz;
        secure.controllerMethod = method;
        
        return secure;
    }

    public static Secure urls(String... urls) {
        Objects.requireNonNull(urls, Required.URL.toString());
        
        Secure secure = new Secure();
        secure.secureType = SecureType.URLS;
        secure.urls = urls;
        
        return secure;
    }

    public void withBasicAuthentication(String username, String password) {
        Objects.requireNonNull(username, Required.USERNAME.toString());
        Objects.requireNonNull(password, Required.PASSWORD.toString());
        
        if (SecureType.URL == this.secureType) {
            
        } else if (SecureType.CONTROLLER == this.secureType) {
            Router.getRoutes().forEach(route -> {
                
            });
        } else if (SecureType.METHOD == this.secureType) {
            
        } else if (SecureType.URLS == this.secureType) {
            
        } else {
            // Ignore anything else
        }
    }

    public void withAuthentication() {
        // TODO Auto-generated method stub
    }

    public void withAuthorization(String role) {
        // TODO Auto-generated method stub
    }
}
