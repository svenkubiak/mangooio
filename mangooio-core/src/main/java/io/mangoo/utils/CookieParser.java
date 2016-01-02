package io.mangoo.utils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Splitter;

import io.mangoo.core.Application;
import io.mangoo.crypto.Crypto;
import io.mangoo.enums.Default;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.Cookie;

/**
 *
 * @author svenkubiak
 *
 */
public class CookieParser {
    private static final int AUTH_PREFIX_LENGTH = 3;
    private static final int SESSION_PREFIX_LENGTH = 4;
    private static final int INDEX_0 = 0;
    private static final int INDEX_1 = 1;
    private static final int INDEX_2 = 2;
    private static final int INDEX_3 = 3;
    private final Map<String, String> sessionValues = new HashMap<>();
    private final boolean encrypted;
    private final String secret;
    private String value;
    private String sign;
    private String expires;
    private String authenticityToken;
    private String authenticatedUser;
    private String version;
    private LocalDateTime expiresDate;

    public CookieParser(String value, String secret, boolean encrypted) {
        this.value = value;
        this.encrypted = encrypted;
        this.secret = secret;
    }

    public static CookieParser create(HttpServerExchange exchange, String cookieName, String secret, boolean encrypted) {
        Objects.requireNonNull(exchange, "exchange can not be null");
        Objects.requireNonNull(cookieName, "cookieName can not be null");
        Objects.requireNonNull(secret, "application secret can not be null");

        return new CookieParser(getCookieValue(exchange, cookieName), secret, encrypted);
    }

    public boolean hasValidSessionCookie() {
        decrypt();
        extractSession();

        boolean valid = false;
        if (StringUtils.isNotBlank(this.sign) && StringUtils.isNotBlank(this.expires) && StringUtils.isNotBlank(this.authenticityToken)) {
            final String data = this.value.substring(this.value.indexOf(Default.DATA_DELIMITER.toString()) + 1, this.value.length());
            final LocalDateTime expiresDate = LocalDateTime.parse(expires);

            if (LocalDateTime.now().isBefore(expiresDate) && DigestUtils.sha512Hex(data + this.authenticityToken + expires + version + this.secret).equals(sign)) {
                this.expiresDate = expiresDate;
                valid = true;

                if (StringUtils.isNotEmpty(data)) {
                    Splitter.on(Default.SPLITTER.toString())
                        .withKeyValueSeparator(Default.SEPERATOR.toString())
                        .split(data)
                        .entrySet()
                        .forEach(entry -> this.sessionValues.put(entry.getKey(), entry.getValue()));
                }
            }
        }

        return valid;
    }

    public boolean hasValidAuthenticationCookie() {
        decrypt();
        extractAuthentication();

        boolean valid = false;
        if (StringUtils.isNotBlank(this.sign) && StringUtils.isNotBlank(this.expires)) {
            final String username = this.value.substring(this.value.indexOf(Default.DATA_DELIMITER.toString()) + 1, this.value.length());
            final LocalDateTime expires = LocalDateTime.parse(this.expires);

            if (LocalDateTime.now().isBefore(expires) && DigestUtils.sha512Hex(username + this.expires + this.version + this.secret).equals(sign)) {
                this.expiresDate = expires;
                this.authenticatedUser = username;
                valid = true;
            }
        }

        return valid;
    }

    public Map<String, String> getSessionValues() {
        return this.sessionValues;
    }

    public String getAuthenticityToken() {
        return this.authenticityToken;
    }

    public LocalDateTime getExpiresDate() {
        return this.expiresDate;
    }

    public String getAuthenticatedUser() {
        return this.authenticatedUser;
    }

    private void decrypt() {
        if (this.encrypted && !this.value.contains("\\|")) {
            this.value = Application.getInstance(Crypto.class).decrypt(this.value);
        }
    }

    private void extractSession() {
        final String prefix = StringUtils.substringBefore(this.value, Default.DATA_DELIMITER.toString());
        if (StringUtils.isNotBlank(prefix)) {
            final String [] prefixes = prefix.split("\\" + Default.DELIMITER.toString());
            if (prefixes != null && prefixes.length == SESSION_PREFIX_LENGTH) {
                this.sign = prefixes [INDEX_0];
                this.authenticityToken = prefixes [INDEX_1];
                this.expires = prefixes [INDEX_2];
                this.version = prefixes [INDEX_3];
            }
        }
    }

    private void extractAuthentication() {
        final String prefix = StringUtils.substringBefore(this.value, Default.DATA_DELIMITER.toString());
        if (StringUtils.isNotBlank(prefix)) {
            final String [] prefixes = prefix.split("\\" + Default.DELIMITER.toString());

            if (prefixes != null && prefixes.length == AUTH_PREFIX_LENGTH) {
                this.sign = prefixes [INDEX_0];
                this.expires = prefixes [INDEX_1];
                this.version = prefixes [INDEX_2];
            }
        }
    }

    private static String getCookieValue(HttpServerExchange exchange, String cookieName) {
        String value = null;
        final Cookie cookie = exchange.getRequestCookies().get(cookieName);
        if (cookie != null) {
            value = cookie.getValue();
        }

        return value;
    }
}