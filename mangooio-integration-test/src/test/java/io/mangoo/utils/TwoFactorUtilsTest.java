package io.mangoo.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import org.junit.Test;

/**
 * 
 * @author svenkubiak, WilliamDunne
 *
 */
public class TwoFactorUtilsTest {
    private static final String ACCOUNT = "MyAccount";
    private static final String SECRET = "MySecureSecret";
    private static final String LINK = "https://chart.googleapis.com/chart?chs=200x200&cht=qr&chl=200x200&chld=M|0&cht=qr&chl=otpauth://totp/MyAccount?secret=MySecureSecret";

    @Test
    public void testGenerateQRCode() {
        //when
        String qrCode = TwoFactorUtils.generateQRCode(ACCOUNT, SECRET);
        
        //then
        assertThat(qrCode, not(equalTo(nullValue())));
        assertThat(qrCode, equalTo(LINK));
    }
    
    @Test
    public void testGenerateCurrentNumber() {
        //when
        String number = TwoFactorUtils.generateCurrentNumber(SECRET);
        
        //then
        assertThat(number, not(nullValue()));
        assertThat(number.length(), equalTo(6));
    }
    
    @Test
    public void testGenerateCurrentNumberWithMillis() {
        //when
        String number = TwoFactorUtils.generateCurrentNumber(SECRET, 1454934536166L);

        //then
        assertThat(number, not(nullValue()));
        assertThat(number, equalTo("453852"));
    }
    
    @Test
    public void testValidateCurrentNumber() {
        //when
        String number = TwoFactorUtils.generateCurrentNumber(SECRET);

        //then
        assertThat(TwoFactorUtils.validateCurrentNumber((Integer.valueOf(number)), SECRET), equalTo(true));
    }
    
    @Test
    public void testNumberLength() {
        //when
        String number = TwoFactorUtils.generateCurrentNumber(SECRET);

        //then
        for (int i=0 ; i <= 100000; i++) {
            assertThat(number.length(), equalTo(6));
        }
    }
}