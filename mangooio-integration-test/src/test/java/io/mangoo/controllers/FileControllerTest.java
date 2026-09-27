package io.mangoo.controllers;

import controllers.FileController;
import io.mangoo.TestExtension;
import io.mangoo.core.Application;
import io.mangoo.core.Config;
import io.undertow.util.StatusCodes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

/**
 *
 * @author svenkubiak
 *
 */
@ExtendWith({TestExtension.class})
class FileControllerTest {
    private static final int FIVE_SECONDS = 5;

    @Test
    void testFile() throws IOException, InterruptedException {
        //given
        byte[] expected = Files.readAllBytes(FileController.getFile());

        //when
        HttpResponse<byte[]> response = execute("/file");

        //then
        assertThat(response.statusCode(), equalTo(StatusCodes.OK));
        assertThat(response.headers().firstValue("Content-Length").orElseThrow(), equalTo(String.valueOf(expected.length)));
        assertThat(response.body(), equalTo(expected));
    }

    @Test
    void testFileDetectsContentType() throws IOException, InterruptedException {
        //when
        HttpResponse<byte[]> response = execute("/file");

        //then
        assertThat(response.statusCode(), equalTo(StatusCodes.OK));
        assertThat(response.headers().firstValue("Content-Type").orElseThrow(), equalTo("image/png"));
    }

    @Test
    void testFileWithExplicitContentType() throws IOException, InterruptedException {
        //given
        byte[] expected = Files.readAllBytes(FileController.getFile());

        //when
        HttpResponse<byte[]> response = execute("/file/contenttype");

        //then
        assertThat(response.statusCode(), equalTo(StatusCodes.OK));
        assertThat(response.headers().firstValue("Content-Type").orElseThrow(), equalTo("application/zip"));
        assertThat(response.body(), equalTo(expected));
    }

    @Test
    void testMissingFile() throws IOException, InterruptedException {
        //when
        HttpResponse<byte[]> response = execute("/file/missing");

        //then
        assertThat(response.statusCode(), equalTo(StatusCodes.INTERNAL_SERVER_ERROR));
        assertThat(new String(response.body(), StandardCharsets.UTF_8),
                not(containsString(FileController.getMissingFile().toString())));
    }

    private static HttpResponse<byte[]> execute(String uri) throws IOException, InterruptedException {
        Config config = Application.getInstance(Config.class);
        String url = "http://" + config.getConnectorHttpHost() + ":" + config.getConnectorHttpPort() + uri;

        try (var client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(FIVE_SECONDS))
                .build()) {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(FIVE_SECONDS))
                    .GET()
                    .build();

            return client.send(request, HttpResponse.BodyHandlers.ofByteArray());
        }
    }
}
