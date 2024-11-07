package io.mangoo.utils.paseto;

import io.mangoo.constants.NotNull;
import io.mangoo.exceptions.MangooTokenException;
import io.mangoo.utils.JsonUtils;
import io.mangoo.utils.MangooUtils;
import org.apache.logging.log4j.util.Strings;
import org.paseto4j.commons.SecretKey;
import org.paseto4j.commons.Version;
import org.paseto4j.version4.PasetoLocal;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

public class PasetoBuilder {
    private final Token token = new Token();
    private String secret;

    private PasetoBuilder() {
        token.setId(MangooUtils.randomString(32));
        token.setSubject(MangooUtils.randomString(32));
    }

    public static PasetoBuilder create() {
        return new PasetoBuilder();
    }

    public PasetoBuilder withExpires(LocalDateTime expires) {
        Objects.requireNonNull(expires, NotNull.EXPIRES);
        token.setExpires(expires);

        return this;
    }
    
    public PasetoBuilder withSecret(String secret) {
        Objects.requireNonNull(secret, NotNull.SECRET);
        this.secret = secret;

        return this;
    }

    public PasetoBuilder withClaim(String key, String value) {
        Objects.requireNonNull(key, NotNull.KEY);
        token.getClaims().put(key, value);

        return this;
    }

    public PasetoBuilder withClaims(Map<String, String> claims) {
        Objects.requireNonNull(claims, NotNull.CLAIMS);
        token.setClaims(claims);

        return this;
    }
    
    public PasetoBuilder withSubject(String subject) {
        Objects.requireNonNull(subject, NotNull.SUBJECT);
        token.setSubject(subject);

        return this;
    }

    public PasetoBuilder withId(String id) {
        Objects.requireNonNull(id, NotNull.ID);
        token.setId(id);

        return this;
    }

    public PasetoBuilder withIssuer(String issue) {
        Objects.requireNonNull(issue, NotNull.ISSUER);
        token.setIssuer(issue);

        return this;
    }

    public String build() throws MangooTokenException {
        try {
            return PasetoLocal.encrypt(
                    new SecretKey(secret.substring(0, 32).getBytes(StandardCharsets.UTF_8), Version.V4),
                    JsonUtils.toJson(token),
                    Strings.EMPTY,
                    Strings.EMPTY);
        } catch (Exception e) {
            throw new MangooTokenException(e);
        }
    }

    public String getSecret() {
        return secret;
    }

    public Token getToken() {
        return token;
    }
}