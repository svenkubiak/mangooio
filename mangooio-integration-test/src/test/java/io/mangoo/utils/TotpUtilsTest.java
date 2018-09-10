package io.mangoo.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;

import java.util.concurrent.atomic.AtomicInteger;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.llorllale.cactoos.matchers.RunsInThreads;

import io.mangoo.TestExtension;
import io.mangoo.enums.HmacShaAlgorithm;

/**
 * 
 * @author svenkubiak
 *
 */
public class TotpUtilsTest {
	private static final int PASSWORD_LENGTH = 6;
	private static final int SECRET_LENGTH = 64;

	@Test
	public void testCreateKey() {
        //given
        String secret = TotpUtils.createSecret().get();
        
        //then
        assertThat(secret, not(nullValue()));
        assertThat(secret.length(), equalTo(SECRET_LENGTH));
	}
	
	@Test
	public void testCreateKeyConcurrent() throws InterruptedException {
        MatcherAssert.assertThat(t -> {
            //given
            String secret = TotpUtils.createSecret().get();
            
            // then
            return secret.length() == SECRET_LENGTH;
        }, new RunsInThreads<>(new AtomicInteger(), TestExtension.THREADS));
	}
	
	@Test
	public void testGetTotp() {
        //given
		String secret = TotpUtils.createSecret().get();
		
		//when
        String totp = TotpUtils.getTotp(secret, HmacShaAlgorithm.HMAC_SHA_512).get();
        
        //then
        assertThat(totp, not(nullValue()));
        assertThat(totp.length(), equalTo(PASSWORD_LENGTH));
	}
	
	@Test
	public void testGetTotpConcurrent() throws InterruptedException {
        MatcherAssert.assertThat(t -> {
            //given
            String secret = TotpUtils.createSecret().get();
        
            //when
            String totp = TotpUtils.getTotp(secret, HmacShaAlgorithm.HMAC_SHA_512).get();
            
            // then
            return totp.length() == PASSWORD_LENGTH;
        }, new RunsInThreads<>(new AtomicInteger(), TestExtension.THREADS));
	}
	
	@Test
	public void testVerifyTotp() {
        //given
		String secret = TotpUtils.createSecret().get();
		String totp = TotpUtils.getTotp(secret, HmacShaAlgorithm.HMAC_SHA_512).get();
		
        //then
        assertThat(true, equalTo(TotpUtils.verifiedTotp(secret, totp, HmacShaAlgorithm.HMAC_SHA_512)));
	}
	
	@Test
	public void testVerifyTotpConcurrent() throws InterruptedException {
        MatcherAssert.assertThat(t -> {
            //given
            String secret = TotpUtils.createSecret().get();
            String totp = TotpUtils.getTotp(secret, HmacShaAlgorithm.HMAC_SHA_512).get();
        
            // then
            return TotpUtils.verifiedTotp(secret, totp, HmacShaAlgorithm.HMAC_SHA_512);
        }, new RunsInThreads<>(new AtomicInteger(), TestExtension.THREADS));
	}
	
	@Test
	public void testGetQRCode() {
        //given
		String secret = TotpUtils.createSecret().get();
		String qr = TotpUtils.getQRCode("test", "issuer", secret, HmacShaAlgorithm.HMAC_SHA_512);
		
        //then
		assertThat(qr, not(nullValue()));
        assertThat(qr, startsWith("https://chart.googleapis.com/chart?chs=200x200&cht=qr&chl=200x200&chld=M|0&cht=qr&chl=otpauth://totp/test?secret="));
	}
	
	@Test
	public void testGetTotpURL() {
        //given
		String secret = "foo";
		String qr = TotpUtils.getOtpauthURL("test", "issuer", secret, HmacShaAlgorithm.HMAC_SHA_512);

        //then
		assertThat(qr, not(nullValue()));
        assertThat(qr, equalTo("otpauth://totp/test?secret=MZXW6&algorithm=HmacSHA512&issuer=issuer"));
	}
	
	@Test
	public void testGetTotpURLConcurrent() throws InterruptedException {
        MatcherAssert.assertThat(t -> {
            //given
            String secret = "foo";
            String qr = TotpUtils.getOtpauthURL("test", "issuer", secret, HmacShaAlgorithm.HMAC_SHA_512);
        
            // then
            return qr.equals(TotpUtils.getOtpauthURL("test", "issuer", secret, HmacShaAlgorithm.HMAC_SHA_512));
        }, new RunsInThreads<>(new AtomicInteger(), TestExtension.THREADS));
	}
}