package mangoo.io.authentication;

import java.util.Date;

import mangoo.io.configuration.Config;
import mangoo.io.enums.Default;
import mangoo.io.enums.Key;

import org.apache.commons.lang3.StringUtils;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

/**
 *
 * @author svenkubiak
 *
 */
public class Authentication {
    private static final Logger LOG = LoggerFactory.getLogger(Authentication.class);
    private Config config;
    private String expires;
    private String authenticatedUser;
    private boolean remember;
    private boolean loggedOut;

    public Authentication(Config config, String authenticatedUser, String expires) {
        this.config = config;
        this.expires = expires;
        this.authenticatedUser = authenticatedUser;
    }

    public Authentication(Config config) {
        this.config = config;
        this.expires = String.valueOf(new Date().getTime() + this.config.getInt(Key.AUTH_COOKIE_EXPIRES, Default.COOKIE_EXPIRES.toInt()));
    }
    
    public String getAuthenticatedUser() {
        return this.authenticatedUser;
    }
    
    public String getExpires() {
        return expires;
    }

    public void setExpires(String expires) {
        this.expires = expires;
    }

    public boolean isLogout() {
        return loggedOut;
    }

    public boolean isRemember() {
        return remember;
    }

    /**
     * Hashes a given clear text password using BCrypt
     * 
     * @param password The clear text password
     * @return The hashed password
     */
    public String getHashedPassword(String password) {
        Preconditions.checkNotNull(password, "Password is required for getHashedPassword");

        return BCrypt.hashpw(password, BCrypt.gensalt(Default.JBCRYPT_ROUNDS.toInt()));
    }

    /**
     * Creates a hashed value of a given clear text password and checks if the
     * value matches a given already hashed password
     * 
     * @param password The clear text password
     * @param hash The previously hashed password to check
     * @return True if the hashed password matches the hash, false otherwise
     */
    public boolean authenticate(String password, String hash) {
        Preconditions.checkNotNull(password, "Password is required for authenticate");
        Preconditions.checkNotNull(password, "Hashed password is required for authenticate");

        boolean authenticated = false;
        try {
            authenticated = BCrypt.checkpw(password, hash);
        } catch (IllegalArgumentException e) {
            LOG.error("Failed to check password against hash", e);
        }

        return authenticated;
    }

    /**
     * Performs a logout of the currently authenticated user
     */
    public void logout() {
        this.loggedOut = true;
    }

    /**
     * Performs a login for a given username
     * 
     * @param username The username to login
     * @param remember If true, the user will stay logged in for (default) 2 weeks
     */
    public void login(String username, boolean remember) {
        Preconditions.checkNotNull(username, "Username is required for login");

        if (StringUtils.isNotBlank(StringUtils.trimToNull(username))) {
            this.authenticatedUser = username;
            this.remember = remember;
        }
    }

    /**
     * Checks if the authentication contains an authenticated user
     * 
     * @return True if authentication contains an authenticated user, false otherwise
     */
    public boolean hasAuthenticatedUser() {
        return StringUtils.isNotBlank(this.authenticatedUser);
    }

    /**
     * Checks if the given username is authenticated
     * 
     * @param username The username to check
     * @return True if the given username is authenticates
     */
    public boolean isAuthenticated(String username) {
        Preconditions.checkNotNull(username, "Username is required for isAuthenticated");

        return username.equals(this.authenticatedUser);
    }
}