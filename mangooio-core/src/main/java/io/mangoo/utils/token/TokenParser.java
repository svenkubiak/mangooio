package io.mangoo.utils.token;

import java.util.Objects;

import dev.paseto.jpaseto.Paseto;
import dev.paseto.jpaseto.PasetoException;
import dev.paseto.jpaseto.Pasetos;
import io.mangoo.enums.Required;
import io.mangoo.exceptions.MangooTokenException;

/**
 * 
 * @author svenkubiak
 *
 */
public class TokenParser extends TokenCommons {
    private String sharedSecret;
    private String cookieValue;
    
    public static TokenParser create() {
        return new TokenParser();
    }

    public TokenParser withSharedSecret(String sharedSecret) {
        Objects.requireNonNull(sharedSecret, Required.SHARED_SECRET.toString());
        
        this.sharedSecret = sharedSecret;
        return this;
    }
    
    public TokenParser withCookieValue(String cookieValue) {
        Objects.requireNonNull(cookieValue, Required.COOKIE_VALUE.toString());
        
        this.cookieValue = cookieValue;
        return this;
    }
    
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