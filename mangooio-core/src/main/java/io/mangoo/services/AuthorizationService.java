package io.mangoo.services;

import java.util.List;

import org.casbin.jcasbin.main.Enforcer;

import com.google.common.io.Resources;
import com.google.inject.Singleton;

/**
 * 
 * @author svenkubiak
 *
 */
@Singleton
public class AuthorizationService {
    private Enforcer enforcer;

    public AuthorizationService () {
        this.enforcer = new Enforcer(Resources.getResource("model.conf").getPath(), Resources.getResource("policy.csv").getPath());
    }
    
    /**
     * Returns a list of a roles for a given user
     * @param username The user to check
     * @return List of roles as string
     */
    public List<String> getRoles(String username) {
        return this.enforcer.getRolesForUser(username);
    }
    
    /**
     * Validates the authorization for a give subject on a given resource with a given operation
     * 
     * @param subject The subject, e.g. username, to check
     * @param resource The resource, e.g. /foo, to check
     * @param operation The operation, e.g. read, to check
     * 
     * @return true if authorization is valid, false otherwise
     */
    public boolean validAuthorization(String subject, String resource, String operation) {
        return enforcer.enforce(subject, resource, operation);
    }
}