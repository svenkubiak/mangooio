package io.mangoo.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;

import java.util.concurrent.atomic.AtomicInteger;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.llorllale.cactoos.matchers.RunsInThreads;

import io.mangoo.TestExtension;
import io.mangoo.enums.HmacShaAlgorithm;

@ExtendWith({TestExtension.class})
public class TotpUtilsTest {
	private static final int PASSWORD_LENGTH = 6;
	private static final int SECRET_LENGTH = 64;

	@Test
	public void testCreateKey() {
        //given
        String secret = TotpUtils.createSecret();
        
        //then
        assertThat(secret, not(nullValue()));
        assertThat(secret.length(), equalTo(SECRET_LENGTH));
	}
	
	@Test
	public void testCreateKeyConcurrent() throws InterruptedException {
        MatcherAssert.assertThat(t -> {
            //given
            String secret = TotpUtils.createSecret();
            
            // then
            return secret.length() == SECRET_LENGTH;
        }, new RunsInThreads<>(new AtomicInteger(), TestExtension.THREADS));
	}
	
	@Test
	public void testGetTotp() {
        //given
		String secret = TotpUtils.createSecret();
		
		//when
        String totp = TotpUtils.getTotp(secret);
        
        //then
        assertThat(totp, not(nullValue()));
        assertThat(totp.length(), equalTo(PASSWORD_LENGTH));
	}
	
	@Test
	public void testGetTotpConcurrent() throws InterruptedException {
        MatcherAssert.assertThat(t -> {
            //given
            String secret = TotpUtils.createSecret();
        
            //when
            String totp = TotpUtils.getTotp(secret);
            
            // then
            return totp.length() == PASSWORD_LENGTH;
        }, new RunsInThreads<>(new AtomicInteger(), TestExtension.THREADS));
	}
	
	@Test
	public void testVerifyTotp() {
        //given
		String secret = TotpUtils.createSecret();
		String totp = TotpUtils.getTotp(secret);
		
        //then
        assertThat(true, equalTo(TotpUtils.verifiedTotp(secret, totp)));
	}
	
	@Test
	public void testVerifyTotpConcurrent() throws InterruptedException {
        MatcherAssert.assertThat(t -> {
            //given
            String secret = TotpUtils.createSecret();
            String totp = TotpUtils.getTotp(secret);
        
            // then
            return TotpUtils.verifiedTotp(secret, totp);
        }, new RunsInThreads<>(new AtomicInteger(), TestExtension.THREADS));
	}
	
	@Test
	public void testGetQRCode() {
        //given
		String secret = TotpUtils.createSecret();
		String qr = TotpUtils.getQRCode("test", "issuer", secret, HmacShaAlgorithm.HMAC_SHA_512, "6", "30");
		
        //then
		assertThat(qr, not(nullValue()));
        assertThat(qr, startsWith("https://chart.googleapis.com/chart?chs=200x200&cht=qr&chl=200x200&chld=M|0&cht=qr&chl=otpauth%3A%2F%2Ftotp%2Ftest%3Fsecret"));
	}
	
	@Test
	public void testGetTotpURL() {
        //given
		String secret = "foo";
		String qr = TotpUtils.getOtpauthURL("test", "issuer", secret, HmacShaAlgorithm.HMAC_SHA_512, "6", "30");

        //then
		assertThat(qr, not(nullValue()));
        assertThat(qr, equalTo("otpauth%3A%2F%2Ftotp%2Ftest%3Fsecret%3DMZXW6%26algorithm%3DSHA512%26issuer%3Dissuer%26digits%3D6%26period%3D30"));
	}
	
	@Test
	public void testGetTotpURLConcurrent() throws InterruptedException {
        MatcherAssert.assertThat(t -> {
            //given
            String secret = "foo";
            String qr = TotpUtils.getOtpauthURL("test", "issuer", secret, HmacShaAlgorithm.HMAC_SHA_512, "6", "30");
        
            // then
            return qr.equals(TotpUtils.getOtpauthURL("test", "issuer", secret, HmacShaAlgorithm.HMAC_SHA_512, "6", "30"));
        }, new RunsInThreads<>(new AtomicInteger(), TestExtension.THREADS));
	}
}