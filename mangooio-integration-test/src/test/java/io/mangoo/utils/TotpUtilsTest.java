package io.mangoo.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.util.regex.Pattern;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Execution(ExecutionMode.CONCURRENT)
class TotpUtilsTest {

    @Test
    void testCreateSecret() {
        //given
        // No setup needed - method generates random secret

        //when
        String secret = TotpUtils.createSecret();

        //then
        assertThat(secret, not(nullValue()));
        assertThat(secret, not(emptyString()));
        assertThat(secret.length(), equalTo(64));
        // Should contain only valid Base32 characters (A-Z, 2-7)
        assertThat(secret, matchesRegex("^[A-Z2-7]{64}$"));
    }

    @Test
    void testCreateSecretUniqueness() {
        //given
        // No setup needed - method generates random secret

        //when
        String secret1 = TotpUtils.createSecret();
        String secret2 = TotpUtils.createSecret();

        //then
        assertThat(secret1, not(equalTo(secret2)));
    }

    @Test
    void testCreateSecretMultipleTimes() {
        //given
        // No setup needed - method generates random secret

        //when
        String secret1 = TotpUtils.createSecret();
        String secret2 = TotpUtils.createSecret();
        String secret3 = TotpUtils.createSecret();

        //then
        assertThat(secret1, not(equalTo(secret2)));
        assertThat(secret2, not(equalTo(secret3)));
        assertThat(secret1, not(equalTo(secret3)));
    }

    @Test
    void testCreateSecretCharacterRange() {
        //given
        // No setup needed - method generates random secret

        //when
        String secret = TotpUtils.createSecret();

        //then
        assertThat(secret, not(nullValue()));
        assertThat(secret.length(), equalTo(64));
        // Check that all characters are in the valid range (A-Z, 2-7)
        for (char c : secret.toCharArray()) {
            assertThat(c, anyOf(
                    allOf(greaterThanOrEqualTo('A'), lessThanOrEqualTo('Z')),
                    allOf(greaterThanOrEqualTo('2'), lessThanOrEqualTo('7'))
            ));
        }
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
    void testGetTotpWithDifferentSecrets() {
        //given
        String secret1 = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
        String secret2 = "ZYXWVUTSRQPONMLKJIHGFEDCBA765432ZYXWVUTSRQPONMLKJIHGFEDCBA765432";

        //when
        String totp1 = TotpUtils.getTotp(secret1);
        String totp2 = TotpUtils.getTotp(secret2);

        //then
        assertThat(totp1, not(nullValue()));
        assertThat(totp2, not(nullValue()));
        assertThat(totp1, not(equalTo(totp2)));
    }

    @Test
    void testGetTotpWithShortSecret() {
        //given
        String secret = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

        //when
        String totp = TotpUtils.getTotp(secret);

        //then
        assertThat(totp, not(nullValue()));
        assertThat(totp, not(emptyString()));
        assertThat(totp.length(), equalTo(6));
        assertThat(totp, matchesRegex("^\\d{6}$"));
    }

    @Test
    void testGetTotpWithLongSecret() {
        //given
        String secret = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567ABCDEFGHIJKLMNOPQRSTUVWXYZ234567ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

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
        NullPointerException exception = assertThrows(NullPointerException.class, 
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
        assertThat(exception.getMessage(), containsString("Secret must not be empty"));
    }

    @Test
    void testGetTotpConsistency() {
        //given
        String secret = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

        //when
        String totp1 = TotpUtils.getTotp(secret);
        String totp2 = TotpUtils.getTotp(secret);

        //then
        assertThat(totp1, not(nullValue()));
        assertThat(totp2, not(nullValue()));
        // TOTP values should be the same when generated at the same time
        assertThat(totp1, equalTo(totp2));
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
        NullPointerException exception = assertThrows(NullPointerException.class, 
                () -> TotpUtils.verifyTotp(secret, totp));
        assertThat(exception.getMessage(), containsString("secret can not be null"));
    }

    @Test
    void testVerifyTotpWithNullTotp() {
        //given
        String secret = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
        String totp = null;

        //when & then
        NullPointerException exception = assertThrows(NullPointerException.class, 
                () -> TotpUtils.verifyTotp(secret, totp));
        assertThat(exception.getMessage(), containsString("totp can not be null"));
    }

    @Test
    void testVerifyTotpWithBothNull() {
        //given
        String secret = null;
        String totp = null;

        //when & then
        NullPointerException exception = assertThrows(NullPointerException.class, 
                () -> TotpUtils.verifyTotp(secret, totp));
        // Should throw for the first null parameter (secret)
        assertThat(exception.getMessage(), containsString("secret can not be null"));
    }

    @Test
    void testVerifyTotpWithEmptySecret() {
        //given
        String secret = "";
        String totp = "123456";

        //when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> TotpUtils.verifyTotp(secret, totp));
        assertThat(exception.getMessage(), containsString("Secret must not be empty"));
    }

    @Test
    void testVerifyTotpWithEmptyTotp() {
        //given
        String secret = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
        String totp = "";

        //when
        boolean isValid = TotpUtils.verifyTotp(secret, totp);

        //then
        assertThat(isValid, equalTo(false));
    }

    @Test
    void testVerifyTotpWithShortTotp() {
        //given
        String secret = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
        String totp = "12345";

        //when
        boolean isValid = TotpUtils.verifyTotp(secret, totp);

        //then
        assertThat(isValid, equalTo(false));
    }

    @Test
    void testVerifyTotpWithLongTotp() {
        //given
        String secret = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
        String totp = "1234567";

        //when
        boolean isValid = TotpUtils.verifyTotp(secret, totp);

        //then
        assertThat(isValid, equalTo(false));
    }

    @Test
    void testVerifyTotpWithNonNumericTotp() {
        //given
        String secret = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
        String totp = "abcdef";

        //when
        boolean isValid = TotpUtils.verifyTotp(secret, totp);

        //then
        assertThat(isValid, equalTo(false));
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
        // Should be a valid Base64 string
        assertThat(qrCode, matchesRegex("^[A-Za-z0-9+/]*={0,2}$"));
        // Should be a reasonable length for a QR code image
        assertThat(qrCode.length(), greaterThan(800));
    }

    @Test
    void testGetQRCodeWithDifferentNames() {
        //given
        String name1 = "user1@example.com";
        String name2 = "user2@example.com";
        String issuer = "TestApp";
        String secret = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

        //when
        String qrCode1 = TotpUtils.getQRCode(name1, issuer, secret);
        String qrCode2 = TotpUtils.getQRCode(name2, issuer, secret);

        //then
        assertThat(qrCode1, not(nullValue()));
        assertThat(qrCode2, not(nullValue()));
        assertThat(qrCode1, not(equalTo(qrCode2)));
    }

    @Test
    void testGetQRCodeWithDifferentIssuers() {
        //given
        String name = "test@example.com";
        String issuer1 = "App1";
        String issuer2 = "App2";
        String secret = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

        //when
        String qrCode1 = TotpUtils.getQRCode(name, issuer1, secret);
        String qrCode2 = TotpUtils.getQRCode(name, issuer2, secret);

        //then
        assertThat(qrCode1, not(nullValue()));
        assertThat(qrCode2, not(nullValue()));
        assertThat(qrCode1, not(equalTo(qrCode2)));
    }

    @Test
    void testGetQRCodeWithDifferentSecrets() {
        //given
        String name = "test@example.com";
        String issuer = "TestApp";
        String secret1 = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
        String secret2 = "ZYXWVUTSRQPONMLKJIHGFEDCBA765432ZYXWVUTSRQPONMLKJIHGFEDCBA765432";

        //when
        String qrCode1 = TotpUtils.getQRCode(name, issuer, secret1);
        String qrCode2 = TotpUtils.getQRCode(name, issuer, secret2);

        //then
        assertThat(qrCode1, not(nullValue()));
        assertThat(qrCode2, not(nullValue()));
        assertThat(qrCode1, not(equalTo(qrCode2)));
    }

    @Test
    void testGetQRCodeNullName() {
        //given
        String name = null;
        String issuer = "TestApp";
        String secret = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

        //when & then
        NullPointerException exception = assertThrows(NullPointerException.class, 
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
        NullPointerException exception = assertThrows(NullPointerException.class, 
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
        NullPointerException exception = assertThrows(NullPointerException.class, 
                () -> TotpUtils.getQRCode(name, issuer, secret));
        assertThat(exception.getMessage(), containsString("secret can not be null"));
    }

    @Test
    void testGetQRCodeWithSpecialCharacters() {
        //given
        String name = "test+user@example.com";
        String issuer = "Test App & Co.";
        String secret = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

        //when
        String qrCode = TotpUtils.getQRCode(name, issuer, secret);

        //then
        assertThat(qrCode, not(nullValue()));
        assertThat(qrCode, not(emptyString()));
        assertThat(qrCode, matchesRegex("^[A-Za-z0-9+/]*={0,2}$"));
    }

    @Test
    void testGetQRCodeWithUnicodeCharacters() {
        //given
        String name = "测试@example.com";
        String issuer = "测试应用";
        String secret = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

        //when
        String qrCode = TotpUtils.getQRCode(name, issuer, secret);

        //then
        assertThat(qrCode, not(nullValue()));
        assertThat(qrCode, not(emptyString()));
        assertThat(qrCode, matchesRegex("^[A-Za-z0-9+/]*={0,2}$"));
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
        assertThat(otpauthURL, not(nullValue()));
        assertThat(otpauthURL, not(emptyString()));
        assertThat(otpauthURL, startsWith("otpauth://totp/"));
        assertThat(otpauthURL, containsString("secret=" + secret));
        assertThat(otpauthURL, containsString("algorithm=SHA512"));
        assertThat(otpauthURL, containsString("issuer=" + issuer));
        assertThat(otpauthURL, containsString("digits=6"));
        assertThat(otpauthURL, containsString("period=30"));
    }

    @Test
    void testGetOtpauthURLWithDifferentNames() {
        //given
        String name1 = "user1@example.com";
        String name2 = "user2@example.com";
        String issuer = "TestApp";
        String secret = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

        //when
        String otpauthURL1 = TotpUtils.getOtpAuthURL(name1, issuer, secret);
        String otpauthURL2 = TotpUtils.getOtpAuthURL(name2, issuer, secret);

        //then
        assertThat(otpauthURL1, not(nullValue()));
        assertThat(otpauthURL2, not(nullValue()));
        assertThat(otpauthURL1, not(equalTo(otpauthURL2)));
        assertThat(otpauthURL1, containsString("user1@example.com"));
        assertThat(otpauthURL2, containsString("user2@example.com"));
    }

    @Test
    void testGetOtpauthURLWithDifferentIssuers() {
        //given
        String name = "test@example.com";
        String issuer1 = "App1";
        String issuer2 = "App2";
        String secret = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

        //when
        String otpauthURL1 = TotpUtils.getOtpAuthURL(name, issuer1, secret);
        String otpauthURL2 = TotpUtils.getOtpAuthURL(name, issuer2, secret);

        //then
        assertThat(otpauthURL1, not(nullValue()));
        assertThat(otpauthURL2, not(nullValue()));
        assertThat(otpauthURL1, not(equalTo(otpauthURL2)));
        assertThat(otpauthURL1, containsString("issuer=App1"));
        assertThat(otpauthURL2, containsString("issuer=App2"));
    }

    @Test
    void testGetOtpauthURLWithDifferentSecrets() {
        //given
        String name = "test@example.com";
        String issuer = "TestApp";
        String secret1 = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
        String secret2 = "ZYXWVUTSRQPONMLKJIHGFEDCBA765432ZYXWVUTSRQPONMLKJIHGFEDCBA765432";

        //when
        String otpauthURL1 = TotpUtils.getOtpAuthURL(name, issuer, secret1);
        String otpauthURL2 = TotpUtils.getOtpAuthURL(name, issuer, secret2);

        //then
        assertThat(otpauthURL1, not(nullValue()));
        assertThat(otpauthURL2, not(nullValue()));
        assertThat(otpauthURL1, not(equalTo(otpauthURL2)));
        assertThat(otpauthURL1, containsString("secret=" + secret1));
        assertThat(otpauthURL2, containsString("secret=" + secret2));
    }

    @Test
    void testGetOtpauthURLNullName() {
        //given
        String name = null;
        String issuer = "TestApp";
        String secret = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

        //when & then
        NullPointerException exception = assertThrows(NullPointerException.class, 
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
        NullPointerException exception = assertThrows(NullPointerException.class, 
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
        NullPointerException exception = assertThrows(NullPointerException.class, 
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
        String otpauthURL = TotpUtils.getOtpAuthURL(name, issuer, secret);

        //then
        assertThat(otpauthURL, not(nullValue()));
        assertThat(otpauthURL, not(emptyString()));
        assertThat(otpauthURL, startsWith("otpauth://totp/"));
        assertThat(otpauthURL, containsString("test+user@example.com"));
        assertThat(otpauthURL, containsString("Test App & Co."));
    }

    @Test
    void testGetOtpauthURLWithUnicodeCharacters() {
        //given
        String name = "测试@example.com";
        String issuer = "测试应用";
        String secret = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

        //when
        String otpauthURL = TotpUtils.getOtpAuthURL(name, issuer, secret);

        //then
        assertThat(otpauthURL, not(nullValue()));
        assertThat(otpauthURL, not(emptyString()));
        assertThat(otpauthURL, startsWith("otpauth://totp/"));
        assertThat(otpauthURL, containsString("测试@example.com"));
        assertThat(otpauthURL, containsString("测试应用"));
    }

    @Test
    void testGetOtpauthURLWithEmptyName() {
        //given
        String name = "";
        String issuer = "TestApp";
        String secret = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

        //when
        String otpauthURL = TotpUtils.getOtpAuthURL(name, issuer, secret);

        //then
        assertThat(otpauthURL, not(nullValue()));
        assertThat(otpauthURL, not(emptyString()));
        assertThat(otpauthURL, startsWith("otpauth://totp/"));
        assertThat(otpauthURL, containsString("secret=" + secret));
    }

    @Test
    void testGetOtpauthURLWithEmptyIssuer() {
        //given
        String name = "test@example.com";
        String issuer = "";
        String secret = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

        //when
        String otpauthURL = TotpUtils.getOtpAuthURL(name, issuer, secret);

        //then
        assertThat(otpauthURL, not(nullValue()));
        assertThat(otpauthURL, not(emptyString()));
        assertThat(otpauthURL, startsWith("otpauth://totp/"));
        assertThat(otpauthURL, containsString("issuer="));
    }

    @Test
    void testGetOtpauthURLWithEmptySecret() {
        //given
        String name = "test@example.com";
        String issuer = "TestApp";
        String secret = "";

        //when
        String otpauthURL = TotpUtils.getOtpAuthURL(name, issuer, secret);

        //then
        assertThat(otpauthURL, not(nullValue()));
        assertThat(otpauthURL, not(emptyString()));
        assertThat(otpauthURL, startsWith("otpauth://totp/"));
        assertThat(otpauthURL, containsString("secret="));
    }

    @Test
    void testGetOtpauthURLFormat() {
        //given
        String name = "test@example.com";
        String issuer = "TestApp";
        String secret = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

        //when
        String otpauthURL = TotpUtils.getOtpAuthURL(name, issuer, secret);

        //then
        assertThat(otpauthURL, not(nullValue()));
        // Should match the expected otpauth URL format
        Pattern otpauthPattern = Pattern.compile("^otpauth://totp/[^?]+\\?secret=[^&]+&algorithm=SHA512&issuer=[^&]+&digits=6&period=30$");
        assertThat(otpauthURL, matchesPattern(otpauthPattern));
    }

    @Test
    void testTotpRoundTrip() {
        //given
        String secret = TotpUtils.createSecret();
        String totp = TotpUtils.getTotp(secret);

        //when
        boolean isValid = TotpUtils.verifyTotp(secret, totp);

        //then
        assertThat(isValid, equalTo(true));
    }

    @Test
    void testQRCodeAndOtpauthURLConsistency() {
        //given
        String name = "test@example.com";
        String issuer = "TestApp";
        String secret = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

        //when
        String qrCode = TotpUtils.getQRCode(name, issuer, secret);
        String otpauthURL = TotpUtils.getOtpAuthURL(name, issuer, secret);

        //then
        assertThat(qrCode, not(nullValue()));
        assertThat(otpauthURL, not(nullValue()));
        // QR code should be a valid Base64 string
        assertThat(qrCode, matchesRegex("^[A-Za-z0-9+/]*={0,2}$"));
        // otpauth URL should contain the expected parameters
        assertThat(otpauthURL, containsString("secret=" + secret));
        assertThat(otpauthURL, containsString("algorithm=SHA512"));
        assertThat(otpauthURL, containsString("issuer=" + issuer));
    }
}
