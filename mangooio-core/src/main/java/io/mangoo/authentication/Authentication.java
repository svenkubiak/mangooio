package io.mangoo.authentication;

import java.time.LocalDateTime;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mindrot.jbcrypt.BCrypt;

import io.mangoo.enums.Default;
import io.mangoo.models.OAuthUser;

/**
 * Convenient class for handling authentication
 *
 * @author svenkubiak
 *
 */
public class Authentication {
    private static final Logger LOG = LogManager.getLogger(Authentication.class);
    private LocalDateTime expires;
    private OAuthUser oAuthUser;
    private String authenticatedUser;
    private boolean remember;
    private boolean loggedOut;

    public Authentication() {
    }

    public Authentication(LocalDateTime expires, String authenticatedUser) {
        this.expires = expires;
        this.authenticatedUser = authenticatedUser;
    }

    /**
     * Retrieves the current authenticated user
     *
     * @return The username of the current authenticated user or null
     */
    public String getAuthenticatedUser() {
        return this.authenticatedUser;
    }

    /**
     * Returns the LocalDateTime when the authentication expires
     *
     * @return A LocalDateTime object or null if unset
     */
    public LocalDateTime getExpires() {
        return expires;
    }

    /**
     * @return True if the user wants to logout, false otherwise
     */
    public boolean isLogout() {
        return loggedOut;
    }

    /**
     *
     * @return True if the user wants to stay logged in, false otherwise
     */
    public boolean isRemember() {
        return remember;
    }

    /**
     * Sets an OAuthUser to the current authentication.
     * Can only be set once!
     *
     * @param oAuthUser An OAuthUser
     */
    public void setOAuthUser(OAuthUser oAuthUser) {
        if (this.oAuthUser == null) {
            this.oAuthUser = oAuthUser;
        }
    }

    /**
     * Retrieves the current OAuthUser from the authentication
     * Note: This is only available during a OAuth authentication in a
     * method that is annotated with @OAuthCallbackFilter
     *
     * @return The current OAuthUser instance or null if undefined
     */
    public OAuthUser getOAuthUser() {
        return this.oAuthUser;
    }

    /**
     * Hashes a given clear text password using JBCrypt
     *
     * @param password The clear text password
     * @return The hashed password
     */
    public String getHashedPassword(String password) {
        Objects.requireNonNull(password, "password is required for getHashedPassword");

        return BCrypt.hashpw(password, BCrypt.gensalt(Default.JBCRYPT_ROUNDS.toInt()));
    }

    /**
     * Creates a hashed value of a given clear text password and checks if the
     * value matches a given, already hashed password
     *
     * @param password The clear text password
     * @param hash The previously hashed password to check
     * @return True if the new hashed password matches the hash, false otherwise
     */
    public boolean authenticate(String password, String hash) {
        Objects.requireNonNull(password, "password is required for authenticate");
        Objects.requireNonNull(hash, "Hashed password is required for authenticate");

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
     * Performs a login for a given user name
     *
     * @param username The user name to login
     * @param remember If true, the user will stay logged in for (default) 2 weeks
     */
    public void login(String username, boolean remember) {
        Objects.requireNonNull(username, "Username is required for login");

        if (StringUtils.isNotBlank(StringUtils.trimToNull(username))) {
            this.authenticatedUser = username;
            this.remember = remember;
        }
    }

    /**
     * Convenient method that calls {@link #login(String, boolean) login} with
     * remember set to false
     *
     * @param username The user name to login
     */
    public void login(String username) {
        login(username, false);
    }

    /**
     * Checks if the authentication contains an authenticated user
     *
     * @return True if authentication contains an authenticated user, false otherwise
     */
    public boolean hasAuthenticatedUser() {
        return StringUtils.isNotBlank(this.authenticatedUser) || this.oAuthUser != null;
    }

    /**
     * Checks if the given user name is authenticated
     *
     * @param username The user name to check
     * @return True if the given user name is authenticates
     */
    public boolean isAuthenticated(String username) {
        Objects.requireNonNull(username, "username is required for isAuthenticated");

        return username.equals(this.authenticatedUser);
    }
}