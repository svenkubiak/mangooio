package io.mangoo.models;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtParser;

/**
 * 
 * @author svenkubiak
 *
 */
public class JsonWebToken {
    private final JwtParser jwtParser;
    private final String bearer;

    public JsonWebToken(JwtParser jwtParser, String bearer) {
        this.jwtParser = jwtParser;
        this.bearer = bearer;
    }

    public JwtParser must() {
        return this.jwtParser;
    }
    
    public Jws<Claims> validate() {
        return this.jwtParser.parseClaimsJws(bearer);
    }
}