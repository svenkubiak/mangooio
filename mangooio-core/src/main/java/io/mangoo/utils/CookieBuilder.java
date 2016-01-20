package io.mangoo.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import io.undertow.server.handlers.Cookie;
import io.undertow.server.handlers.CookieImpl;

import io.mangoo.configuration.Config;
import io.mangoo.core.Application;

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
    private Integer cookieMaxAge;
    private LocalDateTime cookieExpires = LocalDateTime.now().plusDays(1);
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
     * Sets up the cookie for locale settings
     * @author WilliamDunne
     */
    public CookieBuilder createLocale() {
        return new CookieBuilder().name(Application.getInstance(Config.class).getLocaleCookieName());
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

        if (this.cookieDomain != null) {
            cookie.setDomain(this.cookieDomain);
        }

        if (this.cookieMaxAge != null) {
            cookie.setMaxAge(this.cookieMaxAge);
        }

        return cookie;
    }
}