package io.mangoo.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jwa.AlgorithmConstraints.ConstraintType;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.keys.HmacKey;
import org.junit.Test;

import com.google.common.base.Charsets;

import io.mangoo.configuration.Config;
import io.mangoo.core.Application;
import io.mangoo.crypto.Crypto;
import io.mangoo.enums.ClaimKey;
import io.mangoo.routing.bindings.Session;
import io.mangoo.test.utils.WebRequest;
import io.mangoo.test.utils.WebResponse;
import io.mangoo.utils.ByteUtils;
import io.mangoo.utils.DateUtils;
import io.undertow.util.StatusCodes;

/**
 *
 * @author svenkubiak
 *
 */
public class SessionControllerTest {
    
    @Test
    public void testSessionCookie() {
        //when
        Config config = Application.getInstance(Config.class);
        WebResponse response = WebRequest.get("/session").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getCookie(config.getSessionCookieName()).getName(), equalTo(config.getSessionCookieName()));
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testSessionCookieWithValue() throws InvalidJwtException, MalformedClaimException {
        //given
        Config config = Application.getInstance(Config.class);
        String uuid = UUID.randomUUID().toString();
        WebResponse response = WebRequest.get("/session/valued/" + uuid).execute();
        
        //when
        String cleaned = response.getCookie(config.getSessionCookieName()).getValue().replace("\"", "").trim();
        String decryptedValue = Application.getInstance(Crypto.class).decrypt(cleaned, config.getSessionCookieEncryptionKey());
        JwtConsumer jwtConsumer = new JwtConsumerBuilder()
                .setVerificationKey(new HmacKey(config.getSessionCookieSignKey().getBytes(Charsets.UTF_8)))
                .setJwsAlgorithmConstraints(new AlgorithmConstraints(ConstraintType.WHITELIST, AlgorithmIdentifiers.HMAC_SHA512))
                .build();
        
        JwtClaims jwtClaims = jwtConsumer.processToClaims(decryptedValue);
        LocalDateTime expires = LocalDateTime.parse(jwtClaims.getClaimValue(ClaimKey.EXPIRES.toString(), String.class), DateUtils.formatter);
        
         Session session = Session.build()
                    .withContent(ByteUtils.copyMap(jwtClaims.getClaimValue(ClaimKey.DATA.toString(), Map.class)))
                    .withAuthenticity(jwtClaims.getClaimValue(ClaimKey.AUTHENTICITY.toString(), String.class))
                    .withExpires(expires);                         

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(session.get("uuid"), equalTo(uuid));
    }
}