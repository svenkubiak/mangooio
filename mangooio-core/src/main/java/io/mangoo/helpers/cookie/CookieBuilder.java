package io.mangoo.helpers.cookie;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;

import com.google.common.base.Preconditions;

import io.mangoo.enums.Required;
import io.undertow.server.handlers.Cookie;
import io.undertow.server.handlers.CookieImpl;

/**
 *
 * @author svenkubiak
 * @author WilliamDunne
 *
 */
public class CookieBuilder {
    private String cookieName = "";
    private String cookieValue = "";
    private String cookiePath = "/";
    private String cookieDomain;
    private String cookieSameSiteMode;
    private Integer cookieMaxAge;
    private LocalDateTime cookieExpires = LocalDateTime.now().plusDays(1);
    private boolean cookieSameSite;
    private boolean cookieDiscard;
    private boolean cookieSecure;
    private boolean cookieHttpOnly;

    /**
     * @return A new CookieBuilder instance
     */
    public static CookieBuilder create() {
        return new CookieBuilder();
    }

    /**
     * Sets the name of the cookie
     *
     * Default is ""
     *
     * @param name The name of the cookie
     * @return CookieBuilder instance
     */
    public CookieBuilder name(String name) {
        Objects.requireNonNull(name, Required.COOKIE_NAME.toString());
        
        this.cookieName = name;
        return this;
    }

    /**
     * Sets the value of the cookie
     *
     * Default is ""
     *
     * @param value The value of the cookie
     * @return CookieBuilder instance
     */
    public CookieBuilder value(String value) {
        Objects.requireNonNull(value, Required.COOKIE_VALUE.toString());
        
        this.cookieValue = value;
        return this;
    }

    /**
     * Sets the path of the cookie
     *
     * Default is /
     *
     * @param path The path of the cookie
     * @return CookieBuilder instance
     */
    public CookieBuilder path(String path) {
        Objects.requireNonNull(path, Required.PATH.toString());
        
        this.cookiePath = path;
        return this;
    }

    /**
     * Sets the date when the cookie expires
     *
     * Default is now plus 1 day
     *
     * @param expires The expires LocalDateTime
     * @return CookieBuilder instance
     */
    public CookieBuilder expires(LocalDateTime expires) {
        Objects.requireNonNull(expires, Required.EXPIRES.toString());
        
        this.cookieExpires = expires;
        return this;
    }

    /**
     * Sets the max age of the cookie
     *
     * Default value is null
     *
     * @param maxAge The max age of the cookie
     * @return CookieBuilder instance
     */
    public CookieBuilder maxAge(Integer maxAge) {
        this.cookieMaxAge = maxAge;
        return this;
    }

    /**
     * Sets the domain of the cookie
     *
     * Default is null
     *
     * @param domain The domain of the cookie
     * @return CookieBuilder instance
     */
    public CookieBuilder domain(String domain) {
        Objects.requireNonNull(domain, Required.DOMAIN.toString());
        
        this.cookieDomain = domain;
        return this;
    }

    /**
     * Sets discarding of the cookie
     *
     * Default is false
     *
     * @param discard True if cookie should be discard, false otherwise
     * @return CookieBuilder instance
     */
    public CookieBuilder discard(boolean discard) {
        this.cookieDiscard = discard;
        return this;
    }

    /**
     * Sets if the cookie can only be used of HTTPS
     * connections
     *
     * Default is false
     *
     * @param secure True if the cookie can only be used via HTTPS, false otherwise
     * @return CookieBuilder instance
     */
    public CookieBuilder secure(boolean secure) {
        this.cookieSecure = secure;
        return this;
    }

    /**
     * Sets if the cookie can only be used of HTTP
     * connections
     *
     * Default is false
     *
     * @param httpOnly True if the cookie can only be send via HTTP, false otherwise
     * @return CookieBuilder instance
     */
    public CookieBuilder httpOnly(boolean httpOnly) {
        this.cookieHttpOnly = httpOnly;
        return this;
    }
    
    /**
     * Sets if the cookie should have the sameSite attribute
     *
     * Default is strict
     *
     * @param sameSiteMode which is either lax or strict
     * @return CookieBuilder instance
     */
    public CookieBuilder sameSiteMode(String sameSiteMode) {
        Objects.requireNonNull(sameSiteMode, Required.SAME_SIZE_MODE.toString());
        Preconditions.checkArgument(("lax").equalsIgnoreCase(sameSiteMode) || ("strict").equalsIgnoreCase(sameSiteMode), "sameSiteMode can either be 'lax' or 'strict'");
        
        this.cookieSameSite = true;
        this.cookieSameSiteMode = sameSiteMode.toLowerCase();
        return this;
    }

    public Cookie build() {
        Cookie cookie = new CookieImpl(this.cookieName)
                .setValue(this.cookieValue)
                .setDiscard(this.cookieDiscard)
                .setSecure(this.cookieSecure)
                .setHttpOnly(this.cookieHttpOnly)
                .setPath(this.cookiePath)
                .setExpires((this.cookieExpires == null) ?
                        Date.from(LocalDateTime.now().plusDays(1).atZone(ZoneId.systemDefault()).toInstant()) :
                        Date.from(this.cookieExpires.atZone(ZoneId.systemDefault()).toInstant()));

        if (this.cookieSameSite) {
            cookie.setSameSite(true);
            cookie.setSameSiteMode(this.cookieSameSiteMode);
        }
        
        if (this.cookieDomain != null) {
            cookie.setDomain(this.cookieDomain);
        }

        if (this.cookieMaxAge != null) {
            cookie.setMaxAge(this.cookieMaxAge);
        }

        return cookie;
    }
}