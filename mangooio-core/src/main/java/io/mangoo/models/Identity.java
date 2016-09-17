package io.mangoo.models;

import java.security.Principal;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

import io.mangoo.utils.CodecUtils;
import io.undertow.security.idm.Account;
import io.undertow.security.idm.Credential;
import io.undertow.security.idm.IdentityManager;
import io.undertow.security.idm.PasswordCredential;

/**
 * A simple {@link IdentityManager} implementation
 *
 *
* @author svenkubiak
*/
public class Identity implements IdentityManager {
    private String username;
    private String password;
    
    public Identity(String username, String password) {
        this.username = Objects.requireNonNull(username, "username can not be null");
        this.password = Objects.requireNonNull(password, "password can not be null");
    }

    @Override
    public Account verify(Account account) {
        return null;
    }

    @Override
    public Account verify(String username, Credential credential) {
        Account account = getAccount(username);
        if (account != null && verifyCredential(account, credential)) {
            return account;
        }

        return null;
    }

    @Override
    public Account verify(Credential credential) {
        return null;
    }

    private boolean verifyCredential(Account account, Credential credential) {
        if (credential instanceof PasswordCredential) {
            String password = new String(((PasswordCredential) credential).getPassword());
            return CodecUtils.checkJBCrypt(password, this.password);
        }
        
        return false;
    }

    private Account getAccount(final String username) {
        if (this.username.equals(username)) {
            return new Account() {
                private static final long serialVersionUID = -1097117887398334569L;
                private final Principal principal = new Principal() {
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
        
        return null;
    }
}