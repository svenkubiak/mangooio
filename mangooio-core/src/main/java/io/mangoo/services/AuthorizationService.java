package io.mangoo.services;

import org.casbin.jcasbin.main.Enforcer;
import org.casbin.jcasbin.util.Util;

import com.google.common.io.Resources;
import com.google.inject.Singleton;

import io.mangoo.enums.Default;
import io.mangoo.interfaces.MangooAuthorizationService;
import io.mangoo.utils.MangooUtils;

/**
 * 
 * @author svenkubiak
 *
 */
@Singleton
public class AuthorizationService implements MangooAuthorizationService {
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
    
    @Override
    public boolean validAuthorization(String subject, String resource, String operation) {
        return enforcer.enforce(subject, resource, operation);
    }
}