package io.mangoo.models;

import io.mangoo.enums.Required;
import io.undertow.security.idm.Account;
import io.undertow.security.idm.Credential;
import io.undertow.security.idm.IdentityManager;
import io.undertow.security.idm.PasswordCredential;

import java.io.Serial;
import java.io.Serializable;
import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

public class Identity implements IdentityManager, Serializable {
    private static final long serialVersionUID = -412633269312361644L;
    private final String username;
    private final char[] password;
    
    public Identity(String username, String password) {
        this.username = Objects.requireNonNull(username, Required.USERNAME.toString());
        this.password = Objects.requireNonNull(password.toCharArray(), Required.PASSWORD.toString());
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

    private static Account getAccount(String username) {
        return new Account() {
            @Serial
            private static final long serialVersionUID = 5311970975103831035L;
            private transient Principal principal = () -> username;

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
        if (credential instanceof PasswordCredential passwordCredential) {
            return Arrays.equals(passwordCredential.getPassword(), this.password); 
        }
        
        return false;
    }
}