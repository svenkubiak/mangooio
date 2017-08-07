package io.mangoo.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;

import org.junit.Test;

import io.mangoo.crypto.totp.HmacShaAlgorithm;
import io.mangoo.test.utils.ConcurrentRunner;

/**
 * 
 * @author svenkubiak
 *
 */
public class TotpUtilsTest {
	private static final int PASSWORD_LENGTH = 6;
	private static final int SECRET_LENGTH = 64;
	private static final int THREADS = 50;

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
        Runnable runnable = () -> {
            //given
            String secret = TotpUtils.createSecret().get();
            
            //then
            assertThat(secret, not(nullValue()));
            assertThat(secret.length(), equalTo(SECRET_LENGTH));  
        };
        
        ConcurrentRunner.create()
            .withRunnable(runnable)
            .withThreads(THREADS)
            .run();
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
        Runnable runnable = () -> {
            //given
    			String secret = TotpUtils.createSecret().get();
    		
    			//when
            String totp = TotpUtils.getTotp(secret, HmacShaAlgorithm.HMAC_SHA_512).get();
            
            //then
            assertThat(totp, not(nullValue()));
            assertThat(totp.length(), equalTo(PASSWORD_LENGTH));
        };
        
        ConcurrentRunner.create()
            .withRunnable(runnable)
            .withThreads(THREADS)
            .run();
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
		Runnable runnable = () -> {
            //given
    			String secret = TotpUtils.createSecret().get();
    			String totp = TotpUtils.getTotp(secret, HmacShaAlgorithm.HMAC_SHA_512).get();
    			
            //then
            assertThat(true, equalTo(TotpUtils.verifiedTotp(secret, totp, HmacShaAlgorithm.HMAC_SHA_512)));
        };
        
        ConcurrentRunner.create()
            .withRunnable(runnable)
            .withThreads(THREADS)
            .run();
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
		Runnable runnable = () -> {
	        //given
			String secret = "foo";
			String qr = TotpUtils.getOtpauthURL("test", "issuer", secret, HmacShaAlgorithm.HMAC_SHA_512);

	        //then
			assertThat(qr, not(nullValue()));
	        assertThat(qr, equalTo("otpauth://totp/test?secret=MZXW6&algorithm=HmacSHA512&issuer=issuer"));
        };
        
        ConcurrentRunner.create()
            .withRunnable(runnable)
            .withThreads(THREADS)
            .run();
	}
}