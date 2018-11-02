package io.mangoo.services;

import java.io.IOException;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.casbin.jcasbin.main.Enforcer;
import org.casbin.jcasbin.model.Model;
import org.casbin.jcasbin.persist.Adapter;
import org.casbin.jcasbin.persist.Helper;
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
    private static final Logger LOG = LogManager.getLogger(AuthorizationService.class);
    private Enforcer enforcer;
    private boolean logging = false;
    
    public AuthorizationService () {
        Util.enableLog = logging;
        
        if (MangooUtils.resourceExists(Default.MODEL_CONF.toString()) && MangooUtils.resourceExists(Default.POLICY_CSV.toString())) {
            Model model = new Model();
            try {
                model.loadModelFromText(IOUtils.toString(Resources.getResource(Default.MODEL_CONF.toString()).openStream(), Default.ENCODING.toString()));
                this.enforcer = new Enforcer(model, new AuthorizationAdapter());
                this.enforcer.enableLog(logging);
            } catch (IOException e) {
                LOG.error("Failed to load model configuration for authorization handling", e);
            }
        } else {
            this.enforcer = new Enforcer();
            this.enforcer.enableLog(logging);   
        }
    }
    
    @Override
    public boolean validAuthorization(String subject, String resource, String operation) {
        return enforcer.enforce(subject, resource, operation);
    }
    
    private class AuthorizationAdapter implements Adapter {
        @Override
        public void loadPolicy(Model model) {
            loadPolicyFile(model, Helper::loadPolicyLine);
        }

        private void loadPolicyFile(Model model, Helper.loadPolicyLineHandler<String, Model> handler) {
            try {
                List<String> lines = IOUtils.readLines(Resources.getResource(Default.POLICY_CSV.toString()).openStream(), Default.ENCODING.toString());
                lines.forEach(line -> handler.accept(line, model));
            } catch (IOException e) {
                LOG.error("Failed to load policy configuration authorization handling", e);
            }        
        }

        @Override
        public void savePolicy(Model model) {
            // not implemented
        }

        @Override
        public void addPolicy(String sec, String ptype, List<String> rule) {
            // not implemented
        }

        @Override
        public void removePolicy(String sec, String ptype, List<String> rule) {
            // not implemented
        }

        @Override
        public void removeFilteredPolicy(String sec, String ptype, int fieldIndex, String... fieldValues) {
            // not implemented
        }
    }
}