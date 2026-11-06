package io.mangoo.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Execution(ExecutionMode.CONCURRENT)
class TotpUtilsTest {

    @Test
    void testCreateSecret() {
        //given

        //when
        String secret = TotpUtils.createSecret();

        //then
        assertThat(secret, not(nullValue()));
        assertThat(secret, not(emptyString()));
        assertThat(secret.length(), equalTo(64));
        assertThat(secret, matchesRegex("^[A-Z2-7]{64}$"));
    }

    @Test
    void testGetTotp() {
        //given
        String secret = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

        //when
        String totp = TotpUtils.getTotp(secret);

        //then
        assertThat(totp, not(nullValue()));
        assertThat(totp, not(emptyString()));
        assertThat(totp.length(), equalTo(6));
        assertThat(totp, matchesRegex("^\\d{6}$"));
    }

    @Test
    void testGetTotpNullInput() {
        //given
        String secret = null;

        //when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> TotpUtils.getTotp(secret));
        assertThat(exception.getMessage(), containsString("secret can not be null"));
    }

    @Test
    void testGetTotpWithEmptySecret() {
        //given
        String secret = "";

        //when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () ->  TotpUtils.getTotp(secret));
        assertThat(exception.getMessage(), containsString("secret can not be null"));
    }

    @Test
    void testVerifyTotpWithValidTotp() {
        //given
        String secret = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
        String totp = TotpUtils.getTotp(secret);

        //when
        boolean isValid = TotpUtils.verifyTotp(secret, totp);

        //then
        assertThat(isValid, equalTo(true));
    }

    @Test
    void testVerifyTotpWithInvalidTotp() {
        //given
        String secret = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
        String invalidTotp = "123456";

        //when
        boolean isValid = TotpUtils.verifyTotp(secret, invalidTotp);

        //then
        assertThat(isValid, equalTo(false));
    }

    @Test
    void testVerifyTotpWithWrongSecret() {
        //given
        String secret1 = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
        String secret2 = "ZYXWVUTSRQPONMLKJIHGFEDCBA765432ZYXWVUTSRQPONMLKJIHGFEDCBA765432";
        String totp = TotpUtils.getTotp(secret1);

        //when
        boolean isValid = TotpUtils.verifyTotp(secret2, totp);

        //then
        assertThat(isValid, equalTo(false));
    }

    @Test
    void testVerifyTotpWithNullSecret() {
        //given
        String secret = null;
        String totp = "123456";

        //when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> TotpUtils.verifyTotp(secret, totp));
        assertThat(exception.getMessage(), containsString("secret can not be null"));
    }

    @Test
    void testVerifyTotpWithNullTotp() {
        //given
        String secret = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
        String totp = null;

        //when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> TotpUtils.verifyTotp(secret, totp));
        assertThat(exception.getMessage(), containsString("totp can not be null"));
    }

    @Test
    void testVerifyTotpWithEmptySecret() {
        //given
        String secret = "";
        String totp = "123456";

        //when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> TotpUtils.verifyTotp(secret, totp));
        assertThat(exception.getMessage(), containsString("secret can not be null"));
    }

    @Test
    void testVerifyTotpWithEmptyTotp() {
        //given
        String secret = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
        String totp = "";

        //when & then
        assertThrows(IllegalArgumentException.class, () -> TotpUtils.verifyTotp(secret, totp));
    }

    @Test
    void testGetQRCode() {
        //given
        String name = "test@example.com";
        String issuer = "TestApp";
        String secret = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

        //when
        String qrCode = TotpUtils.getQRCode(name, issuer, secret);

        //then
        assertThat(qrCode, not(nullValue()));
        assertThat(qrCode, not(emptyString()));
        assertThat(qrCode, matchesRegex("^[A-Za-z0-9+/]*={0,2}$"));
        // Should be a reasonable length for a QR code image
        assertThat(qrCode.length(), greaterThan(800));
    }

    @Test
    void testGetQRCodeNullName() {
        //given
        String name = null;
        String issuer = "TestApp";
        String secret = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

        //when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> TotpUtils.getQRCode(name, issuer, secret));
        assertThat(exception.getMessage(), containsString("name can not be null"));
    }

    @Test
    void testGetQRCodeNullIssuer() {
        //given
        String name = "test@example.com";
        String issuer = null;
        String secret = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

        //when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> TotpUtils.getQRCode(name, issuer, secret));
        assertThat(exception.getMessage(), containsString("issuer can not be null"));
    }

    @Test
    void testGetQRCodeNullSecret() {
        //given
        String name = "test@example.com";
        String issuer = "TestApp";
        String secret = null;

        //when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> TotpUtils.getQRCode(name, issuer, secret));
        assertThat(exception.getMessage(), containsString("secret can not be null"));
    }

    @Test
    void testGetOtpauthURL() {
        //given
        String name = "test@example.com";
        String issuer = "TestApp";
        String secret = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

        //when
        String otpauthURL = TotpUtils.getOtpAuthURL(name, issuer, secret);

        //then
        assertThat(otpauthURL, equalTo("otpauth://totp/test%40example.com?secret=" + secret + "&algorithm=SHA512&issuer=TestApp&digits=6&period=30"));
    }

    @Test
    void testGetOtpauthURLNullName() {
        //given
        String name = null;
        String issuer = "TestApp";
        String secret = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

        //when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> TotpUtils.getOtpAuthURL(name, issuer, secret));
        assertThat(exception.getMessage(), containsString("account name can not be null"));
    }

    @Test
    void testGetOtpauthURLNullIssuer() {
        //given
        String name = "test@example.com";
        String issuer = null;
        String secret = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

        //when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> TotpUtils.getOtpAuthURL(name, issuer, secret));
        assertThat(exception.getMessage(), containsString("issuer can not be null"));
    }

    @Test
    void testGetOtpauthURLNullSecret() {
        //given
        String name = "test@example.com";
        String issuer = "TestApp";
        String secret = null;

        //when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> TotpUtils.getOtpAuthURL(name, issuer, secret));
        assertThat(exception.getMessage(), containsString("secret can not be null"));
    }

    @Test
    void testGetOtpauthURLWithSpecialCharacters() {
        //given
        String name = "test+user@example.com";
        String issuer = "Test App & Co.";
        String secret = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

        //when
        URI otpauthURI = URI.create(TotpUtils.getOtpAuthURL(name, issuer, secret));

        //then
        assertThat(otpauthURI.getPath(), equalTo("/" + name));
        assertThat(queryParameter(otpauthURI, "issuer"), equalTo(issuer));
        assertThat(queryParameter(otpauthURI, "secret"), equalTo(secret));
        assertThat(queryParameter(otpauthURI, "digits"), equalTo("6"));
    }

    @Test
    void testGetOtpauthURLWithUnicodeCharacters() {
        //given
        String name = "测试@example.com";
        String issuer = "测试应用";
        String secret = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

        //when
        URI otpauthURI = URI.create(TotpUtils.getOtpAuthURL(name, issuer, secret));

        //then
        assertThat(otpauthURI.getPath(), equalTo("/" + name));
        assertThat(queryParameter(otpauthURI, "issuer"), equalTo(issuer));
    }

    @Test
    void testGetOtpauthURLWithEmptyName() {
        //given
        String name = "";
        String issuer = "TestApp";
        String secret = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

        //when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> TotpUtils.getOtpAuthURL(name, issuer, secret));
        assertThat(exception.getMessage(), containsString("account name can not be null"));
    }

    @Test
    void testGetOtpauthURLWithEmptyIssuer() {
        //given
        String name = "test@example.com";
        String issuer = "";
        String secret = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

        //when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> TotpUtils.getOtpAuthURL(name, issuer, secret));
        assertThat(exception.getMessage(), containsString("issuer can not be null"));
    }

    @Test
    void testGetOtpauthURLWithEmptySecret() {
        //given
        String name = "test@example.com";
        String issuer = "TestApp";
        String secret = "";

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> TotpUtils.getOtpAuthURL(name, issuer, secret));
        assertThat(exception.getMessage(), containsString("secret can not be null"));
    }

    private static String queryParameter(URI uri, String name) {
        return Arrays.stream(uri.getRawQuery().split("&"))
                .map(parameter -> parameter.split("=", 2))
                .filter(parameter -> parameter[0].equals(name))
                .map(parameter -> URLDecoder.decode(parameter[1], StandardCharsets.UTF_8))
                .findFirst()
                .orElse(null);
    }
}
