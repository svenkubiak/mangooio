package io.mangoo.admin;

import io.mangoo.TestExtension;
import io.mangoo.TestUtils;
import io.mangoo.constants.Key;
import io.mangoo.core.Application;
import io.mangoo.core.Config;
import io.mangoo.utils.TotpUtils;
import io.mangoo.utils.internal.MangooUtils;
import io.undertow.util.StatusCodes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@ExtendWith({TestExtension.class})
class AdminStreamTest {
    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    private static HttpRequest.Builder stream() {
        Config config = Application.getInstance(Config.class);
        return HttpRequest.newBuilder(URI.create("http://" + config.getConnectorHttpHost() + ":" + config.getConnectorHttpPort() + "/@admin/stream"))
                .timeout(Duration.ofSeconds(15));
    }

    private static String cookie(String value) {
        return MangooUtils.getAdminCookieName() + "=" + value;
    }

    @Test
    void testStreamWithoutAdminCookieIsUnauthorized() throws Exception {
        //when
        HttpResponse<String> response = CLIENT.send(stream().build(), HttpResponse.BodyHandlers.ofString());

        //then
        assertThat(response.statusCode(), equalTo(StatusCodes.UNAUTHORIZED));
    }

    @Test
    void testStreamWithInvalidAdminCookieIsUnauthorized() throws Exception {
        //when
        HttpResponse<String> response = CLIENT.send(stream().header("Cookie", cookie("invalid")).build(), HttpResponse.BodyHandlers.ofString());

        //then
        assertThat(response.statusCode(), equalTo(StatusCodes.UNAUTHORIZED));
    }

    @Test
    void testStreamWithPendingSecondFactorIsUnauthorized() throws Exception {
        //given
        String value;
        try (var _ = TestUtils.withConfigValue(Key.APPLICATION_ADMIN_SECRET, TotpUtils.createSecret())) {
            value = MangooUtils.getAdminCookie(true).getValue();
        }

        //when
        HttpResponse<String> response = CLIENT.send(stream().header("Cookie", cookie(value)).build(), HttpResponse.BodyHandlers.ofString());

        //then
        assertThat(response.statusCode(), equalTo(StatusCodes.UNAUTHORIZED));
    }

    @Test
    void testStreamPushesTheSectionsOfTheDashboard() throws Exception {
        //given
        String value = MangooUtils.getAdminCookie(false).getValue();

        //when
        CompletableFuture<HttpResponse<InputStream>> response = CLIENT.sendAsync(
                stream().header("Cookie", cookie(value)).build(), HttpResponse.BodyHandlers.ofInputStream());

        //then the first push arrives within the interval of five seconds
        HttpResponse<InputStream> stream = response.get(10, TimeUnit.SECONDS);
        assertThat(stream.statusCode(), equalTo(StatusCodes.OK));
        assertThat(stream.headers().firstValue("Content-Type").orElse(""), startsWith("text/event-stream"));

        try (var reader = new BufferedReader(new InputStreamReader(stream.body(), StandardCharsets.UTF_8))) {
            String event = null;
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("event:")) {
                    event = line.substring("event:".length()).strip();
                } else if ("instances".equals(event) && line.startsWith("data:")) {
                    assertThat(line, containsString(Application.getInstanceId()));
                    return;
                }
            }
        } finally {
            response.cancel(true);
        }

        throw new AssertionError("No instances event received");
    }

    @Test
    void testRenderedSectionsAreSingleLines() throws Exception {
        //when
        Map<String, String> sections = Application.getInstance(AdminStream.class).render();

        //then
        assertThat(sections.keySet(), contains("instances", "instance", "events", "requests", "status", "routes", "failures"));
        assertThat(sections.get("instances"), containsString(Application.getInstanceId()));
        assertThat(sections.values(), everyItem(not(containsString("\n"))));
    }

    @Test
    void testExpiryOfTheAdminCookie() throws Exception {
        //given
        var cookie = MangooUtils.getAdminCookie(false);

        //then
        assertThat(AdminStream.expiresAt(cookie.getValue()).isPresent(), equalTo(true));
        assertThat(AdminStream.expiresAt(null).isPresent(), equalTo(false));
        assertThat(AdminStream.expiresAt("invalid").isPresent(), equalTo(false));
    }
}
