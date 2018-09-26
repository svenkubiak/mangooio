package io.mangoo.models;

import java.security.Principal;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

import io.mangoo.enums.Required;
import io.undertow.security.idm.Account;
import io.undertow.security.idm.Credential;
import io.undertow.security.idm.IdentityManager;
import io.undertow.security.idm.PasswordCredential;

/**
 * 
 * A simple IdentityManager implementation
 * 
 * @author svenkubiak
 *
 */
public class Identity implements IdentityManager {
    private String username;
    private String password;
    
    public Identity(String username, String password) {
        this.username = Objects.requireNonNull(username, Required.USERNAME.toString());
        this.password = Objects.requireNonNull(password, Required.PASSWORD.toString());
    }

    @Override
    public Account verify(Account account) {
        return null;
    }

    @Override
    public Account verify(Credential credential) {
        return null;
    }

    @Override
    public Account verify(String username, Credential credential) {
        Account account = null;
        if (this.username.equals(username) && verifyCredential(credential)) {
            account = getAccount(username);
        }

        return account;
    }

    @SuppressWarnings("all")
    private Account getAccount(String username) {
        return new Account() {
            private static final long serialVersionUID = 5311970975103831035L;
            private transient Principal principal = new Principal() {
                @Override
                public String getName() {
                    return username;
                }
            };

            @Override
            public Principal getPrincipal() {
                return principal;
            }

            @Override
            public Set<String> getRoles() {
                return Collections.emptySet();
            }
        };
    }

    private boolean verifyCredential(Credential credential) {
        if (credential instanceof PasswordCredential) {
            return new String (((PasswordCredential) credential).getPassword()).equals(this.password);
        }
        
        return false;
    }
}