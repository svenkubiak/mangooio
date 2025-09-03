package io.mangoo.utils;

import io.mangoo.TestExtension;
import io.mangoo.test.concurrent.ConcurrentRunner;
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
	void testGetTotp() {
        //given
        String secret = MangooUtils.randomString(SECRET_LENGTH);
		
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
            String secret = MangooUtils.randomString(SECRET_LENGTH);
        
            //when
            String totp = TotpUtils.getTotp(secret);
            
            // then
            return totp.length() == PASSWORD_LENGTH;
        }, new ConcurrentRunner<>(new AtomicInteger(), TestExtension.THREADS));
	}
	
	@Test
	void testVerifyTotp() {
        //given
        String secret = MangooUtils.randomString(SECRET_LENGTH);
		String totp = TotpUtils.getTotp(secret);
		
        //then
        assertThat(true, equalTo(TotpUtils.verifyTotp(secret, totp)));
	}
	
	@Test
	void testVerifyTotpConcurrent() throws InterruptedException {
        MatcherAssert.assertThat(t -> {
            //given
            String secret = MangooUtils.randomString(SECRET_LENGTH);
            String totp = TotpUtils.getTotp(secret);
        
            // then
            return TotpUtils.verifyTotp(secret, totp);
        }, new ConcurrentRunner<>(new AtomicInteger(), TestExtension.THREADS));
	}
	
	@Test
	void testGetQRCode() {
        //given
		String secret = "My voice is my secret";
		String qr = TotpUtils.getQRCode("test", "issuer", secret);
		
        //then
		assertThat(qr, not(nullValue()));
        assertThat(qr, equalTo("iVBORw0KGgoAAAANSUhEUgAAAPoAAAD6AQAAAACgl2eQAAACOElEQVR4Xu2YMY6DQAxFHaWg5AjcBC6GBFIuRm4yR6CkiPD+/51d7SSrtFgrXKCEecWM8/3tifnnWOz1zUucQMQJRJxAxH8CDNH5fcVjt6lswzry1ZQKaNwfXXP3h9klqLFzX5MBS7zBY29v3tzxCWhCAG+uxQat4VA5gW1gvne7Fp0nH0A9WA+dxlrfPrp3wRwMqLKkgp/HH6V3MKDYrPWC88wGI+CaIg+AHWPbfXsryjK+XsvWt8mACx4N9w75TjgKagxGkAsIe7ojy2GpSzvjUJUejgdscJbXOmHvkK8vrKw9GcBtR9+UUUEKaE5V30wB4Av7JowUWcabW2GhpQK4d9sggCL5Oi2fX1MBxh2r3pH0m7PD5wO2C+29R5b5iS1T5p8L0PAhjwflHDiRaiU9EYDK57YhCnqoNxw4t6qyUgBO5SLfzr6JQrvyKLkA2ZNUQKpDlmcO8bkAuX3MwkaPYvOE5XMpEcB6ioFYls/RU+aaCWho7xw5YvjAGuJFMIcDkgJOYSY9YM040OUC6PHMMisfR+Err/SQAuAYx1KCfYJC0jEmVXpIAazP6y6VoeZOikupgNHkTJrljNHUqT4eULD8nTfdMHp1gEwAd80scwLxmfPxyHv6lApoePWBfeImpL7JWaQ6RQpAfxYNLC+MSbiiM373zTwAcwu3f8oXlsBIBnzPclIGfLXyqAwA9UABFN7UOvHvf3kdDPDn79SXNrXMAWh1igzAhziBiBOIOIGIE4hYvgC1Hygim2deZAAAAABJRU5ErkJggg=="));
	}
	
	@Test
	void testGetTotpURL() {
        //given
		String secret = "foo";
		String qr = TotpUtils.getOtpauthURL("test", "issuer", secret);

        //then
		assertThat(qr, not(nullValue()));
        assertThat(qr, equalTo("otpauth://totp/test?secret=foo&algorithm=SHA512&issuer=issuer&digits=6&period=30"));
	}

    @Test
    void testGetTotpURLWithSecret() {
        //given
        String secret = TotpUtils.createSecret();
        String qr = TotpUtils.getOtpauthURL("test", "issuer", secret);

        //then
        assertThat(qr, not(nullValue()));
        assertThat(qr, equalTo("otpauth://totp/test?secret="+ secret + "&algorithm=SHA512&issuer=issuer&digits=6&period=30"));
    }
	
	@Test
	void testGetTotpURLConcurrent() throws InterruptedException {
        MatcherAssert.assertThat(t -> {
            //given
            String secret = "foo";
            String qr = TotpUtils.getOtpauthURL("test", "issuer", secret);
        
            // then
            return qr.equals(TotpUtils.getOtpauthURL("test", "issuer", secret));
        }, new ConcurrentRunner<>(new AtomicInteger(), TestExtension.THREADS));
	}
}