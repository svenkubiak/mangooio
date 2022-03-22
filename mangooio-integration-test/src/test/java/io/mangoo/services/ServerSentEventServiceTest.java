package io.mangoo.services;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.launchdarkly.eventsource.EventHandler;
import com.launchdarkly.eventsource.EventSource;
import com.launchdarkly.eventsource.ReadyState;

import dev.paseto.jpaseto.Pasetos;
import io.mangoo.TestExtension;
import io.mangoo.core.Application;
import io.mangoo.core.Config;
import io.mangoo.enums.ClaimKey;
import okhttp3.Headers;

/**
 *
 * @author svenkubiak
 *
 */
@ExtendWith({TestExtension.class})
class ServerSentEventServiceTest {
    private final static Pattern UUID_REGEX_PATTERN = Pattern.compile("^[{]?[0-9a-fA-F]{8}-([0-9a-fA-F]{4}-){3}[0-9a-fA-F]{12}[}]?$");
     
	@Test
	void testSendData() throws InterruptedException {
        //given
        Config config = Application.getInstance(Config.class);
        
        String url = String.format("http://" + config.getConnectorHttpHost() + ":" + config.getConnectorHttpPort() + "/sse");
        EventHandler eventHandler = new SimpleEventHandler();
        EventSource.Builder builder = new EventSource.Builder(eventHandler, URI.create(url)).reconnectTime(Duration.ofMillis(3000));
        
        try (EventSource eventSource = builder.build()) {
              eventSource.start();
              
              //then
              await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> assertThat(eventSource.getState(), equalTo(ReadyState.OPEN)));
              
              //then
              await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> assertThat(isValidUUID(EventData.data), equalTo(true)));
        } 	    
	}

    @Test
    void testSendDataWithInvalidAuthentication() throws InterruptedException, IllegalArgumentException {
        //given
        Config config = Application.getInstance(Config.class);
        
        String token = Pasetos.V1.LOCAL.builder()
                .setSubject("foo")
                .claim(ClaimKey.TWO_FACTOR.toString(), false)
                .setExpiration(LocalDateTime.now().plusHours(1).toInstant(ZoneOffset.UTC))
                .setSharedSecret(new SecretKeySpec("oskdlwsodkcmansjdkwsowekd5jfvsq2mckdkalsodkskajsfdsfdsfvvkdkcskdsqidsjk".getBytes(StandardCharsets.UTF_8), "AES"))
                .compact();    
        
        String cookie = config.getAuthenticationCookieName() + "=" + token;
        
        String url = String.format("http://" + config.getConnectorHttpHost() + ":" + config.getConnectorHttpPort() + "/sseauth");
        EventHandler eventHandler = new SimpleEventHandler();
        EventSource.Builder builder = new EventSource.Builder(eventHandler, URI.create(url)).reconnectTime(Duration.ofMillis(3000));
        Headers headers = new Headers.Builder()
            .add("Accept", "text/event-stream")
            .add("Cache-Control", "no-cache")
            .add("Set-Cookie", cookie)
            .build();
        
        builder.headers(headers);
        
        try (EventSource eventSource = builder.build()) {
              eventSource.start();

              //then
              await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> assertThat(eventSource.getState(), equalTo(ReadyState.CLOSED)));
              
              //then
              await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> assertThat(isValidUUID(EventData.data), not(equalTo(true))));
        } 
    }
	
    private static boolean isValidUUID(String str) {
        if (str == null) {
            return false;
        }
        
        return UUID_REGEX_PATTERN.matcher(str).matches();
    }
}