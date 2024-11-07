package io.mangoo.utils.paseto;

import io.mangoo.constants.NotNull;
import io.mangoo.exceptions.MangooTokenException;
import io.mangoo.utils.JsonUtils;
import org.apache.logging.log4j.util.Strings;
import org.paseto4j.commons.SecretKey;
import org.paseto4j.commons.Version;
import org.paseto4j.version4.PasetoLocal;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class PasetoParser {
    private String secret;
    private String cookieValue;

    private PasetoParser() {}

    public static PasetoParser create() {
        return new PasetoParser();
    }

    /**
     * @param Secret The shared secret the Token was created with
     * @return TokenParser
     */
    public PasetoParser withSecret(String Secret) {
        Objects.requireNonNull(Secret, NotNull.SECRET);
        
        this.secret = Secret;
        return this;
    }
    
    /**
     * @param cookieValue The cookie value to parse the token of
     * @return TokenParser
     */
    public PasetoParser withCookieValue(String cookieValue) {
        Objects.requireNonNull(cookieValue, NotNull.COOKIE_VALUE);
        
        this.cookieValue = cookieValue;
        return this;
    }

    /**
     * Parses the cookie value to a token
     * @return A Token
     * @throws MangooTokenException if parsing fails
     */
    public Token parse() throws MangooTokenException {
        try {
            String jwt = PasetoLocal.decrypt(
                    new SecretKey(secret.substring(0, 32).getBytes(StandardCharsets.UTF_8), Version.V4),
                    cookieValue,
                    Strings.EMPTY);

            return JsonUtils.toObject(jwt, Token.class);
        } catch (Exception e) {
            throw new MangooTokenException(e);
        }
    }

    public String getSecret() {
        return secret;
    }

    public String getCookieValue() {
        return cookieValue;
    }
}