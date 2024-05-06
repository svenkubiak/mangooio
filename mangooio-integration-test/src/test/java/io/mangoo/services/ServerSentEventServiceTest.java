package io.mangoo.services;

import com.launchdarkly.eventsource.ConnectStrategy;
import com.launchdarkly.eventsource.EventSource;
import com.launchdarkly.eventsource.HttpConnectStrategy;
import com.launchdarkly.eventsource.ReadyState;
import com.launchdarkly.eventsource.background.BackgroundEventHandler;
import com.launchdarkly.eventsource.background.BackgroundEventSource;
import io.mangoo.TestExtension;
import io.mangoo.constants.ClaimKey;
import io.mangoo.core.Application;
import io.mangoo.core.Config;
import io.mangoo.exceptions.MangooTokenException;
import io.mangoo.manager.ServerSentEventManager;
import io.mangoo.utils.token.TokenBuilder;
import okhttp3.Headers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

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
        BackgroundEventHandler eventHandler = new SimpleEventHandler();
        BackgroundEventSource.Builder builder = new BackgroundEventSource.Builder(eventHandler, new EventSource.Builder(URI.create(url)));

        try (BackgroundEventSource backgroundEventSource = builder.build()) {
            backgroundEventSource.start();

            //then
            await().atMost(3, TimeUnit.SECONDS).untilAsserted(() -> assertThat(backgroundEventSource.getEventSource().getState(), equalTo(ReadyState.OPEN)));

            Application.getInstance(ServerSentEventManager.class).send("/sse", UUID.randomUUID().toString());
              
            //then
            await().atMost(3, TimeUnit.SECONDS).untilAsserted(() -> assertThat(isValidUUID(EventData.data), equalTo(true)));
        } 	    
	}

    @Test
    void testSendDataWithInvalidAuthentication() throws InterruptedException, IllegalArgumentException, MangooTokenException {
        //given
        Config config = Application.getInstance(Config.class);
        
        String token = TokenBuilder.create()
            .withSubject("foo")
            .withClaim(ClaimKey.TWO_FACTOR, false)
            .withExpires(LocalDateTime.now().plusHours(1))
            .withSharedSecret("oskdlwsodkcmansjdkwsowekd5jfvsq2mckdkalsodkskajsfdsfdsfvvkdkcskdsqidsjk")
            .build();
        
        String cookie = config.getAuthenticationCookieName() + "=" + token;
        String url = String.format("http://" + config.getConnectorHttpHost() + ":" + config.getConnectorHttpPort() + "/sseauth");
        
        Headers headers = new Headers.Builder()
                .add("Accept", "text/event-stream")
                .add("Cache-Control", "no-cache")
                .add("Set-Cookie", cookie)
                .build();
        
        HttpConnectStrategy connectStrategy = ConnectStrategy.http(URI.create(url));
        connectStrategy.connectTimeout(5, TimeUnit.SECONDS);
        connectStrategy.headers(headers);
        
        BackgroundEventHandler eventHandler = new SimpleEventHandler();
        BackgroundEventSource.Builder builder = new BackgroundEventSource.Builder(eventHandler, new EventSource.Builder(connectStrategy));
        
        try (BackgroundEventSource backgroundEventSource = builder.build()) {
            backgroundEventSource.start();

            //then
            await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> assertThat(backgroundEventSource.getEventSource().getState(), equalTo(ReadyState.CLOSED)));
              
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