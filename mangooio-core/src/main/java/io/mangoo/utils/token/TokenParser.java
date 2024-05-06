package io.mangoo.utils.token;

import dev.paseto.jpaseto.PasetoException;
import dev.paseto.jpaseto.Pasetos;
import io.mangoo.constants.NotNull;
import io.mangoo.exceptions.MangooTokenException;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class TokenParser {
    private String sharedSecret;
    private String cookieValue;
    
    public static TokenParser create() {
        return new TokenParser();
    }

    /**
     * @param sharedSecret The shared secret the Token was created with
     * @return TokenParser
     */
    public TokenParser withSharedSecret(String sharedSecret) {
        Objects.requireNonNull(sharedSecret, NotNull.SHARED_SECRET);
        
        this.sharedSecret = sharedSecret;
        return this;
    }
    
    /**
     * @param cookieValue The cookie value to parse the token of
     * @return TokenParser
     */
    public TokenParser withCookieValue(String cookieValue) {
        Objects.requireNonNull(cookieValue, NotNull.COOKIE_VALUE);
        
        this.cookieValue = cookieValue;
        return this;
    }
    
    public String getSharedSecret() {
        return sharedSecret;
    }

    public String getCookieValue() {
        return cookieValue;
    }

    /**
     * Parses the cookie value to a token
     * @return A Token
     * @throws MangooTokenException if parsing fails
     */
    public Token parse() throws MangooTokenException {
        try {
            var paseto = Pasetos.parserBuilder()
                .setSharedSecret(sharedSecret.getBytes(StandardCharsets.UTF_8))
                .build()
                .parse(cookieValue);
            
            return new Token(paseto);
        } catch (PasetoException e) {
           throw new MangooTokenException(e); 
        }
    }
}