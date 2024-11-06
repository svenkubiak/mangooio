package io.mangoo.utils.jwt;

import io.mangoo.constants.NotNull;
import io.mangoo.exceptions.MangooTokenException;
import io.mangoo.utils.JsonUtils;
import org.apache.logging.log4j.util.Strings;
import org.paseto4j.commons.SecretKey;
import org.paseto4j.commons.Version;
import org.paseto4j.version4.PasetoLocal;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class JwtParser {
    private String sharedSecret;
    private String cookieValue;
    
    public static JwtParser create() {
        return new JwtParser();
    }

    /**
     * @param sharedSecret The shared secret the Token was created with
     * @return TokenParser
     */
    public JwtParser withSharedSecret(String sharedSecret) {
        Objects.requireNonNull(sharedSecret, NotNull.SHARED_SECRET);
        
        this.sharedSecret = sharedSecret;
        return this;
    }
    
    /**
     * @param cookieValue The cookie value to parse the token of
     * @return TokenParser
     */
    public JwtParser withCookieValue(String cookieValue) {
        Objects.requireNonNull(cookieValue, NotNull.COOKIE_VALUE);
        
        this.cookieValue = cookieValue;
        return this;
    }

    /**
     * Parses the cookie value to a token
     * @return A Token
     * @throws MangooTokenException if parsing fails
     */
    public Jwt parse() throws MangooTokenException {
        try {
            String jwt = PasetoLocal.decrypt(
                    new SecretKey(sharedSecret.substring(0, 32).getBytes(StandardCharsets.UTF_8), Version.V4),
                    cookieValue,
                    Strings.EMPTY);

            return JsonUtils.toObject(jwt, Jwt.class);
        } catch (Exception e) {
            throw new MangooTokenException(e);
        }
    }

    public String getSharedSecret() {
        return sharedSecret;
    }

    public void setSharedSecret(String sharedSecret) {
        this.sharedSecret = sharedSecret;
    }

    public String getCookieValue() {
        return cookieValue;
    }

    public void setCookieValue(String cookieValue) {
        this.cookieValue = cookieValue;
    }
}