package io.mangoo.utils;

import io.mangoo.TestExtension;
import io.mangoo.constants.Hmac;
import io.mangoo.test.concurrent.ConcurrentRunner;
import io.mangoo.utils.totp.TotpUtils;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Execution(ExecutionMode.CONCURRENT)
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
		String secret = "My voice is my secret";
		String qr = TotpUtils.getQRCode("test", "issuer", secret, Hmac.SHA512, "6", "30");
		
        //then
		assertThat(qr, not(nullValue()));
        assertThat(qr, equalTo("iVBORw0KGgoAAAANSUhEUgAAAPoAAAD6AQAAAACgl2eQAAACl0lEQVR4Xu2YQW7qQBBE2/LCSx+Bm8DFkLDExeAmPoKXXiD6v+qB4CRStpS+mEUS97xI062e6rIj/16X+Bn5sT5AWx+gLRfgFqxTrvtxivWwnDJinHYK7owAfi9HRfo57yP8gdjQNnyA6JbTXFnEnrOzd87hEkc34MgfC7W98tdAKjtH4JRZZ7+P54Tqf2XxdkD9UFWOyiI64F8N82aADU48sLf5oaAR0NacrdSE0YD+GXYByKJbItZOTXuJExoQx+Cinb2A4IEuQD4P5NOEVI8+QM6rbhZ7lFqtwCNZgD6y8ADyMiYApV6RfOVDZq9SmwBXHbtTFonuS0g1Qb9ulgUwZE5BqwYaJerCcNoIqQPAiavA2JBZTZtU+UbRKb8PkO3Y1LbkE8Okfqh8jICZshIWsO5DlBK4E/MBHidmbtKvumPVHsN1I2LvB7JaQceOSLWqUJlkbbkAGA2ykOG8ycadWz9sNMoFYJqXkIJO2kuyePWDBcBVxwaXPKW8pqhvhtMBUHjmyBOGXUL/ul42QGoGpeZ6r86V4eSfQpkZAVnWnQl/VqASYE9vQj6ADKda9aBSX4VfaV8V3Qgoh9Trt/pBhjii3ikV8gFkLpnrjKRW6k5C7wXQoMqirn9Zd31H0ARFsoyAfGhAaCT1NTfVGZMXQHhe5eVKnqi8xHU7DgyAVuAWHiX5jNHcXD0LQPKJ/VAC8ko15vUmZATUKsuhaTki/nqnJItnPzgAt5YA8hSynlX5p6T6ABx20YcNEgi16lCxTRYWQH3ArJdcdW7INenbjCaAGSAhnSVUVHmpd6L4noUFgHzypMeq97YfHADZJJ29b5+Cy5A01+QDcGpZjosG5Z0E4qGmX1kYAH+uD9DWB2jrPwH+ARzYRlt+C7KXAAAAAElFTkSuQmCC"));
	}
	
	@Test
	void testGetTotpURL() {
        //given
		String secret = "foo";
		String qr = TotpUtils.getOtpauthURL("test", "issuer", secret, Hmac.SHA512, "6", "30");

        //then
		assertThat(qr, not(nullValue()));
        assertThat(qr, equalTo("otpauth://totp/test?secret=MZXW6&algorithm=SHA512&issuer=issuer&digits=6&period=30"));
	}
	
	@Test
	void testGetTotpURLConcurrent() throws InterruptedException {
        MatcherAssert.assertThat(t -> {
            //given
            String secret = "foo";
            String qr = TotpUtils.getOtpauthURL("test", "issuer", secret, Hmac.SHA512, "6", "30");
        
            // then
            return qr.equals(TotpUtils.getOtpauthURL("test", "issuer", secret, Hmac.SHA512, "6", "30"));
        }, new ConcurrentRunner<>(new AtomicInteger(), TestExtension.THREADS));
	}
}