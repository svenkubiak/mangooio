package io.mangoo.controllers;

import static io.mangoo.test.hamcrest.RegexMatcher.matches;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.mangoo.TestExtension;
import io.mangoo.configuration.Config;
import io.mangoo.core.Application;
import io.mangoo.enums.Default;
import io.mangoo.enums.Header;
import io.mangoo.test.utils.WebRequest;
import io.mangoo.test.utils.WebResponse;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;

/**
 *
 * @author svenkubiak
 *
 */
@ExtendWith({TestExtension.class})
public class ApplicationControllerTest {
    private static final String JSON = "{\"foo\":\"bar\"}";
    private static final String JSON_PLAIN = "{foo=bar}";
    private static final String TEXT_PLAIN = "text/plain; charset=UTF-8";
    private static final String TEXT_HTML = "text/html; charset=UTF-8";

    @Test
    public void testIndex() {
        //given
        final WebResponse response = WebRequest.get("/").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getContentType(), equalTo(TEXT_HTML));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
    }
    
    @Test
    public void testOverwriteDefaultTemplates() {
        //given
        final WebResponse response = WebRequest.get("/lala").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getContentType(), equalTo(TEXT_HTML));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.NOT_FOUND));
        assertThat(response.getContent(), equalTo("my 404"));
    }
    
    @Test
    public void testRoute() {
        //given
        final WebResponse response = WebRequest.get("/route").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("/route"));
        assertThat(response.getContentType(), equalTo(TEXT_HTML));
    }
    
    @Test
    public void testReverse() {
        //given
        final WebResponse response = WebRequest.get("/reverse").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("/string\n/int/23\n/multiple/11/42"));
        assertThat(response.getContentType(), equalTo(TEXT_HTML));
    }
    
    @Test
    public void testLocation() {
        //given
        final WebResponse response = WebRequest.get("/location").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getContentType(), equalTo(TEXT_HTML));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("we are at location"));
    }
    
    @Test
    public void testLocationWithParameter() {
        //given
        final WebResponse response = WebRequest.get("/location/8282838477").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getContentType(), equalTo(TEXT_HTML));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("we are at location"));
    }
    
    @Test
    public void testPrettyTime() {
        //given
        final WebResponse response = WebRequest.get("/prettytime")
                .withHeader("Accept-Language", "de-DE")
                .execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getContentType(), equalTo(TEXT_HTML));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), containsString("gerade"));
    }
    
    @Test
    public void testLimit() {
        //given
        WebResponse response = null;

        //then
        for (int i=0; i <= 10; i++) {
            response = WebRequest.get("/limit").execute();   
            assertThat(response, not(nullValue()));
            assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        }
        response = WebRequest.get("/limit").execute();   
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.TOO_MANY_REQUESTS));
    }

    @Test
    public void testRequest() {
        //given
        final WebResponse response = WebRequest.get("/request").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getContentType(), equalTo(TEXT_PLAIN));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("/request"));
    }

    @Test
    public void testRedirectWithDisableRedirects() {
        //given
        final WebResponse response = WebRequest.get("/redirect").withDisableRedirects(true).execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.FOUND));
        assertThat(response.getResponseUrl(), equalTo("http://localhost:10808"));
    }

    @Test
    public void testRedirectWithoutDisableRedirects() {
        //given
        final WebResponse response = WebRequest.get("/redirect").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getContentType(), equalTo(TEXT_HTML));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getResponseUrl(), equalTo("http://localhost:10808"));
    }

    @Test
    public void testPlainText() {
        //given
        final WebResponse response = WebRequest.get("/text").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getContentType(), equalTo(TEXT_PLAIN));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
    }

    @Test
    public void testNotFound() {
        //given
        final WebResponse response = WebRequest.get("/foo").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getContentType(), equalTo(TEXT_HTML));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.NOT_FOUND));
    }

    @Test
    public void testForbidden() {
        //given
        final WebResponse response = WebRequest.get("/forbidden").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getContentType(), equalTo(TEXT_PLAIN));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.FORBIDDEN));
    }

    @Test
    public void testBadRequest() {
        //given
        final WebResponse response = WebRequest.get("/badrequest").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getContentType(), equalTo(TEXT_PLAIN));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.BAD_REQUEST));
    }

    @Test
    public void testUnauthorized() {
        //given
        final WebResponse response = WebRequest.get("/unauthorized").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getContentType(), equalTo(TEXT_PLAIN));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.UNAUTHORIZED));
    }

    @Test
    public void testAdditionalHeaders() {
        //given
        final WebResponse response = WebRequest.get("/header").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getContentType(), equalTo(TEXT_PLAIN));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getHeader("Access-Control-Allow-Origin"), equalTo("https://mangoo.io"));
    }

    @Test
    public void testBinaryDownload() throws IOException {
        //given
        final Config config = Application.getInjector().getInstance(Config.class);
        final String host = config.getConnectorHttpHost();
        final int port = config.getConnectorHttpPort();
        final File file = new File(UUID.randomUUID().toString());
        final FileOutputStream fileOutputStream = new FileOutputStream(file);

        //when
        final CloseableHttpClient httpclient = HttpClients.custom().build();
        final HttpGet httpget = new HttpGet("http://" + host + ":" + port + "/binary");
        final CloseableHttpResponse response = httpclient.execute(httpget);
        fileOutputStream.write(EntityUtils.toByteArray(response.getEntity()));
        fileOutputStream.close();
        response.close();

        //then
        assertThat(response.getStatusLine().getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(FileUtils.readFileToString(file, Default.ENCODING.toString()), equalTo("This is an attachment"));
        assertThat(file.delete(), equalTo(true));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testEtag() {
        //given
        WebResponse response = WebRequest.get("/etag").execute();
        final String etag = response.getHeader(Headers.ETAG_STRING);

        //then
        assertThat(etag, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(etag, matches("^[a-f0-9]{32}$"));

        //given
        response = WebRequest.get("/etag").withHeader(Headers.IF_NONE_MATCH_STRING, etag).execute();

        //then
        assertThat(response.getStatusCode(), equalTo(StatusCodes.NOT_MODIFIED));
        assertThat(response.getContent(), equalTo(""));
    }

    @Test
    public void testPost() {
        //given
        final WebResponse response = WebRequest.post("/post")
                .withRequestBody("Winter is coming!")
                .execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContentType(), equalTo(TEXT_PLAIN));
        assertThat(response.getContent(), equalTo("Winter is coming!"));
    }

    @Test
    public void testPut() {
        //given
        final WebResponse response = WebRequest.put("/put")
                .withRequestBody("The king of the north!")
                .execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContentType(), equalTo(TEXT_PLAIN));
        assertThat(response.getContent(), equalTo("The king of the north!"));
    }

    @Test
    public void testJsonPathWithPost() {
        //given
        final WebResponse response = WebRequest.post("/jsonpathpost")
                .withRequestBody(JSON)
                .execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContentType(), equalTo(TEXT_PLAIN));
        assertThat(response.getContent(), equalTo(JSON));
    }

    @Test
    public void testJsonPathWithPut() {
        //given
        final WebResponse response = WebRequest.put("/jsonpathput")
                .withRequestBody(JSON)
                .execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContentType(), equalTo(TEXT_PLAIN));
        assertThat(response.getContent(), equalTo(JSON));
    }

    @Test
    public void testJsonBoonWithPost() {
        //given
        final WebResponse response = WebRequest.post("/jsonboonpost")
                .withRequestBody(JSON)
                .execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContentType(), equalTo(TEXT_PLAIN));
        assertThat(response.getContent(), equalTo(JSON_PLAIN));
    }

    @Test
    public void testJsonBoonWithPut() {
        //given
        final WebResponse response = WebRequest.put("/jsonboonput")
                .withRequestBody(JSON)
                .execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContentType(), equalTo(TEXT_PLAIN));
        assertThat(response.getContent(), equalTo(JSON_PLAIN));
    }
    
    @Test
    public void testFreemarkerConfiguration() {
        //given
        final WebResponse response = WebRequest.get("/freemarker").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), containsString("Output format: HTML"));
        assertThat(response.getContent(), containsString("Auto-escaping: true"));
    }
    
    @Test
    public void testHeaders() {
        //given
        final WebResponse response = WebRequest.get("/").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getHeader(Header.X_XSS_PPROTECTION.toString()), equalTo("1"));
        assertThat(response.getHeader(Header.X_CONTENT_TYPE_OPTIONS.toString()), equalTo("nosniff"));
        assertThat(response.getHeader(Header.X_FRAME_OPTIONS.toString()), equalTo("DENY"));
        assertThat(response.getHeader(Headers.SERVER.toString()), equalTo("Undertow"));
        assertThat(response.getHeader(Header.CONTENT_SECURITY_POLICY.toString()), equalTo(""));
        assertThat(response.getHeader(Header.REFERER_POLICY.toString()), equalTo("no-referrer"));
    }
}