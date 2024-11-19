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
    private String value;

    private PasetoParser() {}

    public static PasetoParser create() {
        return new PasetoParser();
    }

    /**
     * @param secret The shared secret the Token was created with
     * @return TokenParser
     */
    public PasetoParser withSecret(String secret) {
        Objects.requireNonNull(secret, NotNull.SECRET);
        
        this.secret = secret.substring(0, 32);
        return this;
    }
    
    /**
     * @param value The paseto value to parse the actual token
     * @return TokenParser
     */
    public PasetoParser withValue(String value) {
        Objects.requireNonNull(value, NotNull.VALUE);
        
        this.value = value;
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
                    new SecretKey(secret.getBytes(StandardCharsets.UTF_8), Version.V4),
                    value,
                    Strings.EMPTY);

            return JsonUtils.toObject(jwt, Token.class);
        } catch (Exception e) {
            throw new MangooTokenException(e);
        }
    }

    public String getSecret() {
        return secret;
    }

    public String getValue() {
        return value;
    }
}