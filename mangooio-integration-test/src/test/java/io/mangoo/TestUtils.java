package io.mangoo;

import com.nimbusds.jwt.JWTClaimsSet;
import io.mangoo.constants.Const;
import io.mangoo.core.Application;
import io.mangoo.core.Config;
import io.mangoo.exceptions.MangooJwtException;
import io.mangoo.test.http.TestRequest;
import io.mangoo.test.http.TestResponse;
import io.mangoo.utils.JwtUtils;

import java.lang.reflect.Field;
import java.net.HttpCookie;
import java.text.ParseException;
import java.util.Map;

public final class TestUtils {
    public static Csrf getCsrf() {
        Config config = Application.getInstance(Config.class);

        TestResponse response = TestRequest.get("/@admin/login")
                .execute();

        HttpCookie cookie = response
                .getCookie(config.getSessionCookieName());

        var jwtData = JwtUtils.jwtData()
                .withKey(config.getSessionCookieKey())
                .withSecret(config.getSessionCookieSecret())
                .withIssuer(config.getApplicationName())
                .withAudience(config.getSessionCookieName())
                .withTtlSeconds(config.getSessionCookieTokenExpires());


        try {
            JWTClaimsSet jwtClaimsSet = JwtUtils.parseJwt(cookie.getValue(), jwtData);

            return new Csrf(cookie, jwtClaimsSet.getClaimAsString(Const.CSRF_TOKEN));
        } catch (ParseException | MangooJwtException e) {
            throw new RuntimeException(e);
        }
    }

    /** Sets a value in the config of the running application until the returned handle is closed. */
    public static AutoCloseable withConfigValue(String key, String value) {
        Map<String, String> values = configValues();
        String previous = values.put(key, value);

        return () -> {
            if (previous == null) {
                values.remove(key);
            } else {
                values.put(key, previous);
            }
        };
    }

    @SuppressWarnings("unchecked")
    private static Map<String, String> configValues() {
        try {
            Field field = Config.class.getDeclaredField("values");
            field.setAccessible(true);
            return (Map<String, String>) field.get(Application.getInstance(Config.class));
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
}
