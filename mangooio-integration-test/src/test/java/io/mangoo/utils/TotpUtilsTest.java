package io.mangoo.utils;

import io.mangoo.TestExtension;
import io.mangoo.enums.HmacShaAlgorithm;
import io.mangoo.test.concurrent.ConcurrentRunner;
import io.mangoo.utils.totp.TotpUtils;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@ExtendWith({TestExtension.class})
class TotpUtilsTest {
	private static final int PASSWORD_LENGTH = 6;
	private static final int SECRET_LENGTH = 64;

	@Test
	void testCreateKey() {
        //given
        String secret = TotpUtils.createSecret();
        
        //then
        assertThat(secret, not(nullValue()));
        assertThat(secret.length(), equalTo(SECRET_LENGTH));
	}
	
	@Test
	void testCreateKeyConcurrent() throws InterruptedException {
        MatcherAssert.assertThat(t -> {
            //given
            String secret = TotpUtils.createSecret();
            
            // then
            return secret.length() == SECRET_LENGTH;
        }, new ConcurrentRunner<>(new AtomicInteger(), TestExtension.THREADS));
	}
	
	@Test
	void testGetTotp() {
        //given
		String secret = TotpUtils.createSecret();
		
		//when
        String totp = TotpUtils.getTotp(secret);
        
        //then
        assertThat(totp, not(nullValue()));
        assertThat(totp.length(), equalTo(PASSWORD_LENGTH));
	}
	
	@Test
	void testGetTotpConcurrent() throws InterruptedException {
        MatcherAssert.assertThat(t -> {
            //given
            String secret = TotpUtils.createSecret();
        
            //when
            String totp = TotpUtils.getTotp(secret);
            
            // then
            return totp.length() == PASSWORD_LENGTH;
        }, new ConcurrentRunner<>(new AtomicInteger(), TestExtension.THREADS));
	}
	
	@Test
	void testVerifyTotp() {
        //given
		String secret = TotpUtils.createSecret();
		String totp = TotpUtils.getTotp(secret);
		
        //then
        assertThat(true, equalTo(TotpUtils.verifiedTotp(secret, totp)));
	}
	
	@Test
	void testVerifyTotpConcurrent() throws InterruptedException {
        MatcherAssert.assertThat(t -> {
            //given
            String secret = TotpUtils.createSecret();
            String totp = TotpUtils.getTotp(secret);
        
            // then
            return TotpUtils.verifiedTotp(secret, totp);
        }, new ConcurrentRunner<>(new AtomicInteger(), TestExtension.THREADS));
	}
	
	@Test
	void testGetQRCode() {
        //given
		String secret = TotpUtils.createSecret();
		String qr = TotpUtils.getQRCode("test", "issuer", secret, HmacShaAlgorithm.HMAC_SHA_512, "6", "30");
		
        //then
		assertThat(qr, not(nullValue()));
        assertThat(qr, startsWith("https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=otpauth"));
	}
	
	@Test
	void testGetTotpURL() {
        //given
		String secret = "foo";
		String qr = TotpUtils.getOtpauthURL("test", "issuer", secret, HmacShaAlgorithm.HMAC_SHA_512, "6", "30");

        //then
		assertThat(qr, not(nullValue()));
        assertThat(qr, equalTo("otpauth%3A%2F%2Ftotp%2Ftest%3Fsecret%3DMZXW6%26algorithm%3DSHA512%26issuer%3Dissuer%26digits%3D6%26period%3D30"));
	}
	
	@Test
	void testGetTotpURLConcurrent() throws InterruptedException {
        MatcherAssert.assertThat(t -> {
            //given
            String secret = "foo";
            String qr = TotpUtils.getOtpauthURL("test", "issuer", secret, HmacShaAlgorithm.HMAC_SHA_512, "6", "30");
        
            // then
            return qr.equals(TotpUtils.getOtpauthURL("test", "issuer", secret, HmacShaAlgorithm.HMAC_SHA_512, "6", "30"));
        }, new ConcurrentRunner<>(new AtomicInteger(), TestExtension.THREADS));
	}
}