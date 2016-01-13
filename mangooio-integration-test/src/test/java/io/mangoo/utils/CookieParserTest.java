package io.mangoo.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Before;
import org.junit.Test;

import io.mangoo.configuration.Config;
import io.mangoo.core.Application;

/**
 *
 * @author svenkubiak
 *
 */
public class CookieParserTest {
    private String secret;
    private final static String sessionCookie = "403a60f01814495528933074b157afd0eabc0d718f721c83b4cad04223eb6127fd1763c258d3b016603514bb10dbc35ddef40d2a369bb3ac8e605ae793fec069|IrJCsQX6dALPdRUd|2999-01-01T23:42:00.00|0#foo:this is a session value";
    private final static String sessionCookieEncrypted = "NkOL0aYtndMFia9do46s2nQxdU2GaLWJryBE3TEknxEzu6O+q03pV5x/M+q6g1JzGtCOiJxDpIzU5MrQmm0oa9EayrMDVvDOCTwhz/r/b0Za86+7VTvD4V5UrD9wVyk4c2ZjsJMbqKJIPuZFwnuOhap9IHDiuxUMhg1RNzLHMsgWY4bOBq+hBJWYUxg4kEsaWH6Sr8i6OE8fqYiUCOjxKG+CqDfDpgK85MKAhsHBahaNqmdNy1BToSKhKyvpGW2ICdlkhaYLSfj7PQm9LGWEQg==";
    private final static String authenticationCookie = "c1cccf94253f7399b0c09c018dfee2005c2fc1ee50f5cb9790aa3dbda5c87b7d91590725880387cfdd079b241fcc663773a539097267e249eb55cdc48faaf7e5|2999-01-01T23:42:00.00|0#foobar";
    private final static String authenticationCookieEncrypted = "rt2CmjSDtgRysf180d+vCfqhGLhfzRsLNKME7puZMJh4LGdurUSYeYEXWI4QDw65zgxJQ5mClSAnzSCSRY5ouWpsRlNpCis4npveFmmg2wQXTrRnG5Lf4ksNMwJbvBxTv6kMa/qLmHKdI1eY9hGryXA745CQMc21QiQO72Ue+1AjhDf/8XpYYWv60buAnJR/XfAFcwh6gNDelN3vzRp0LkZf7S5pQ8xqZBRiZxGYzv8=";

    @Before
    public void init() {
        this.secret = Application.getInstance(Config.class).getApplicationSecret();
    }

    @Test
    public void testValidSession() {
        //given
        final CookieParser cookieParser = new CookieParser(sessionCookie, this.secret, false);

        //then
        assertThat(cookieParser.hasValidSessionCookie(), equalTo(true));
    }

    @Test
    public void testValidSessionWithEncryption() {
        //given
        final CookieParser cookieParser = new CookieParser(sessionCookieEncrypted, this.secret, true);

        //then
        assertThat(cookieParser.hasValidSessionCookie(), equalTo(true));
    }
    
    @Test
    public void testValidAuthentication() {
        //given
        final CookieParser cookieParser = new CookieParser(authenticationCookie, this.secret, false);

        //then
        assertThat(cookieParser.hasValidAuthenticationCookie(), equalTo(true));
    }   

    @Test
    public void testValidAuthenticationWithEncryption() {
        //given
        final CookieParser cookieParser = new CookieParser(authenticationCookieEncrypted, this.secret, true);

        //then
        assertThat(cookieParser.hasValidAuthenticationCookie(), equalTo(true));
    }
}