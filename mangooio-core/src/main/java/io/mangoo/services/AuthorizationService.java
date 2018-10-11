package io.mangoo.services;

import org.casbin.jcasbin.main.Enforcer;
import org.casbin.jcasbin.util.Util;

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
        Util.enableLog = false;
        if (MangooUtils.resourceExists(Default.MODEL_CONF.toString()) && MangooUtils.resourceExists(Default.POLICY_CSV.toString())) {
            this.enforcer = new Enforcer(Resources.getResource(Default.MODEL_CONF.toString()).getPath(), Resources.getResource(Default.POLICY_CSV.toString()).getPath(), false);            
        } else {
            this.enforcer = new Enforcer();
            this.enforcer.enableLog(false);
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