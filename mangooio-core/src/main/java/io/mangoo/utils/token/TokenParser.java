package io.mangoo.utils.token;

import java.util.Objects;

import dev.paseto.jpaseto.Paseto;
import dev.paseto.jpaseto.PasetoException;
import dev.paseto.jpaseto.Pasetos;
import io.mangoo.enums.Required;
import io.mangoo.exceptions.MangooTokenException;

public class TokenParser extends TokenCommons {
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
        Objects.requireNonNull(sharedSecret, Required.SHARED_SECRET.toString());
        
        this.sharedSecret = sharedSecret;
        return this;
    }
    
    /**
     * @param cookieValue The cookie value to parse the token of
     * @return TokenParser
     */
    public TokenParser withCookieValue(String cookieValue) {
        Objects.requireNonNull(cookieValue, Required.COOKIE_VALUE.toString());
        
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
            Paseto paseto = Pasetos.parserBuilder()
                .setSharedSecret(sharedSecret.getBytes(CHARSET))
                .build()
                .parse(cookieValue);
            
            return new Token(paseto);
        } catch (PasetoException e) {
           throw new MangooTokenException(e); 
        }
    }
}