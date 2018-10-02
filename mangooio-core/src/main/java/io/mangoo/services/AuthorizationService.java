package io.mangoo.services;

import org.casbin.jcasbin.main.Enforcer;

import com.google.common.io.Resources;
import com.google.inject.Singleton;

import io.mangoo.enums.Default;
import io.mangoo.utils.MangooUtils;

/**
 * 
 * @author svenkubiak
 *
 */
@Singleton
public class AuthorizationService {
    private Enforcer enforcer;

    public AuthorizationService () {
        if (MangooUtils.resourceExists(Default.MODEL_CONF.toString()) && MangooUtils.resourceExists(Default.POLICY_CSV.toString())) {
            this.enforcer = new Enforcer(Resources.getResource(Default.MODEL_CONF.toString()).getPath(), Resources.getResource(Default.POLICY_CSV.toString()).getPath());            
        } else {
            this.enforcer = new Enforcer();
        }
    }
    
    /**
     * Validates the authorization for a give subject on a given resource with a given operation
     * 
     * @param subject The subject, e.g. username, to check
     * @param resource The resource, e.g. ApplicationController:write, to check
     * @param operation The operation, e.g. read, to check
     * 
     * @return true if authorization is valid, false otherwise
     */
    public boolean validAuthorization(String subject, String resource, String operation) {
        return enforcer.enforce(subject, resource, operation);
    }
}