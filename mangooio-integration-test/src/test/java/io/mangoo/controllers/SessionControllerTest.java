package io.mangoo.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.mangoo.TestExtension;
import io.mangoo.core.Application;
import io.mangoo.core.Config;
import io.mangoo.test.http.TestRequest;
import io.mangoo.test.http.TestResponse;
import io.undertow.util.StatusCodes;

/**
 *
 * @author svenkubiak
 *
 */
@ExtendWith({TestExtension.class})
public class SessionControllerTest {
    
    @Test
    public void testSessionCookie() {
        //when
        Config config = Application.getInstance(Config.class);
        TestResponse response = TestRequest.get("/session").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getCookie(config.getSessionCookieName()).getName(), equalTo(config.getSessionCookieName()));
    }
    
//    @Test
//    @SuppressWarnings("unchecked")
//    public void testSessionCookieWithValue() throws InvalidJwtException, MalformedClaimException {
//         //given
//         Config config = Application.getInstance(Config.class);
//         String uuid = UUID.randomUUID().toString();
//         TestResponse response = TestRequest.get("/session/valued/" + uuid).execute();
//        
//         //when
//         String cookieValue = response.getCookie(config.getSessionCookieName()).getValue();
//         String decryptedValue = Application.getInstance(Crypto.class).decrypt(cookieValue, config.getSessionCookieEncryptionKey());
//         JwtConsumer jwtConsumer = new JwtConsumerBuilder()
//                .setVerificationKey(new HmacKey(config.getSessionCookieSignKey().getBytes(Charsets.UTF_8)))
//                .setJwsAlgorithmConstraints(new AlgorithmConstraints(ConstraintType.WHITELIST, AlgorithmIdentifiers.HMAC_SHA512))
//                .build();
//        
//         JwtClaims jwtClaims = jwtConsumer.processToClaims(decryptedValue);
//         String expiresValue = jwtClaims.getClaimValue(ClaimKey.EXPIRES.toString(), String.class);
//        
//         LocalDateTime expires = null;
//         if (!("-1").equals(expiresValue)) {
//            expires = LocalDateTime.parse(jwtClaims.getClaimValue(ClaimKey.EXPIRES.toString(), String.class), DateUtils.formatter);            
//         } 
//        
//         Session session = Session.create()
//                    .withContent(MangooUtils.copyMap(jwtClaims.getClaimValue(ClaimKey.DATA.toString(), Map.class)))
//                    .withAuthenticity(jwtClaims.getClaimValue(ClaimKey.AUTHENTICITY.toString(), String.class));
//         
//         if (!("-1").equals(expiresValue)) {
//             session.withExpires(expires);            
//          } 
//         
//         //then
//         assertThat(response, not(nullValue()));
//         assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
//         assertThat(session.get("uuid"), equalTo(uuid));
//    }
//    
//    @Test
//    @SuppressWarnings("unchecked")
//    public void testSessionCookieWithValueConcurrent() throws InvalidJwtException, MalformedClaimException {
//        MatcherAssert.assertThat(t -> {
//            //given
//            Config config = Application.getInstance(Config.class);
//            String uuid = UUID.randomUUID().toString();
//            TestResponse response = TestRequest.get("/session/valued/" + uuid).execute();
//           
//            //when
//            String cookieValue = response.getCookie(config.getSessionCookieName()).getValue();
//            
//            JwtConsumer jwtConsumer = new JwtConsumerBuilder()
//                   .setVerificationKey(new HmacKey(config.getSessionCookieSignKey().getBytes(Charsets.UTF_8)))
//                   .setJwsAlgorithmConstraints(new AlgorithmConstraints(ConstraintType.WHITELIST, AlgorithmIdentifiers.HMAC_SHA512))
//                   .build();
//           
//            String decryptedValue = Application.getInstance(Crypto.class).decrypt(cookieValue, config.getSessionCookieEncryptionKey());
//            JwtClaims jwtClaims = jwtConsumer.processToClaims(decryptedValue);
//            Session session = Session.create()
//                       .withContent(MangooUtils.copyMap(jwtClaims.getClaimValue(ClaimKey.DATA.toString(), Map.class)));
//            
//            // then
//            return response != null && response.getStatusCode() == StatusCodes.OK && session != null && session.get("uuid") != null && session.get("uuid").equals(uuid);
//        }, new RunsInThreads<>(new AtomicInteger(), TestExtension.THREADS));
//    }
}