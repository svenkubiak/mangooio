package io.mangoo;

import com.nimbusds.jwt.JWTClaimsSet;
import io.mangoo.constants.Const;
import io.mangoo.core.Application;
import io.mangoo.core.Config;
import io.mangoo.exceptions.MangooJwtException;
import io.mangoo.test.http.TestRequest;
import io.mangoo.test.http.TestResponse;
import io.mangoo.utils.JwtUtils;

import java.net.HttpCookie;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;

public final class TestUtils {
    public static Csrf getCsrf() {
        Config config = Application.getInstance(Config.class);

        TestResponse response = TestRequest.get("/@admin/login")
                .execute();

        HttpCookie cookie = response
                .getCookie(config.getSessionCookieName());

        var jwtData = JwtUtils.jwtData()
                .withKey(config.getSessionCookieKey())
                .withSecret(config.getSessionCookieSecret().getBytes(StandardCharsets.UTF_8))
                .withIssuer(config.getApplicationName())
                .withAudience(config.getSessionCookieName())
                .withTtlSeconds(config.getSessionCookieTokenExpires());


        try {
            JWTClaimsSet jwtClaimsSet = JwtUtils.parseJwt(cookie.getValue(), jwtData);

            System.out.println("cookie value: " + cookie);
            System.out.println("jwt claim: " + jwtClaimsSet.getClaimAsString(Const.CSRF_TOKEN));

            return new Csrf(cookie, jwtClaimsSet.getClaimAsString(Const.CSRF_TOKEN));
        } catch (ParseException | MangooJwtException e) {
            throw new RuntimeException(e);
        }
    }
}
