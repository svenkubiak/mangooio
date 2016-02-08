package io.mangoo.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import org.junit.Test;

/**
 * 
 * @author svenkubiak
 *
 */
public class TwoFactorUtilsTest {
    private static final String LINK = "https://chart.googleapis.com/chart?chs=200x200&cht=qr&chl=200x200&chld=M|0&cht=qr&chl=otpauth://totp/MyKeyId?secret=MySecureSecret";

    @Test
    public void testGetQRCode() {
        //given
        String keyId = "MyKeyId";
        String secret = "MySecureSecret";
        
        //when
        String qrCode = TwoFactorUtils.getQRCode(keyId, secret);
        
        //then
        assertThat(qrCode, not(equalTo(nullValue())));
        assertThat(qrCode, equalTo(LINK));
    }
    
    @Test
    public void testGenerateBase32Secret() {
        //when
        String secret = TwoFactorUtils.generateBase32Secret();

        //then
        assertThat(secret, not(equalTo(nullValue())));
        assertThat(secret.length(), equalTo(16));
        assertThat(secret, not(containsString("1")));
        assertThat(secret, not(containsString("8")));
        assertThat(secret, not(containsString("9")));
        assertThat(secret, not(containsString("0")));
    }
    
    @Test
    public void testGenerateCurrentNumber() {
        //given
        String secret = "MySecureSecret";
        
        //when
        String number = TwoFactorUtils.generateCurrentNumber(secret);

        //then
        assertThat(number, not(nullValue()));
        assertThat(number.length(), equalTo(6));
    }
}