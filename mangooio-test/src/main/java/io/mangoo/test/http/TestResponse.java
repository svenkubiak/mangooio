package io.mangoo.test.http;

import com.google.common.collect.Multimap;
import io.mangoo.constants.Required;
import io.mangoo.core.Application;
import io.mangoo.core.Config;
import io.undertow.util.Methods;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;

import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class TestResponse {
    private static final Logger LOG = LogManager.getLogger(TestResponse.class);
    private static final String CONTENT_TYPE = "Content-Type";
    private static final int FIVE_SECONDS = 5;
    private CookieManager cookieManager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
    private HttpRequest.Builder httpRequest = HttpRequest.newBuilder().timeout(Duration.of(FIVE_SECONDS, ChronoUnit.SECONDS));
    private BodyPublisher body = BodyPublishers.noBody();
    private HttpClient.Builder httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(FIVE_SECONDS))
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .cookieHandler(this.cookieManager);
    private Authenticator authenticator;
    private HttpResponse<String> httpResponse;
    private String uri;
    private String url;
    private String method;

    public TestResponse() {
    }
    
    public TestResponse (String uri, String method) {
        Objects.requireNonNull(uri, Required.URI);
        Objects.requireNonNull(method, Required.HTTP_METHOD);
        
        this.uri = uri;
        this.method = method;
    }
    
    public TestResponse withHeader(String name, String value) {
        Objects.requireNonNull(name, Required.NAME);
        Objects.requireNonNull(value, Required.VALUE);
        
        this.httpRequest.header(name, value);
        
        return this;
    }
    
    public TestResponse withHTTPMethod(String method) {
        Objects.requireNonNull(method, Required.HTTP_METHOD);
        
        this.method = method;
        
        return this;
    }
    
    /** Defaults to five seconds. */
    public TestResponse withTimeout(long amount, TemporalUnit unit) {
        Objects.requireNonNull(method, Required.HTTP_METHOD);
        Objects.requireNonNull(method, Required.UNIT);

        this.httpRequest.timeout(Duration.of(amount, unit));
        
        return this;
    }
    
    public TestResponse withBasicAuthentication(String username, String password) {
        Objects.requireNonNull(username, Required.USERNAME);
        Objects.requireNonNull(password, Required.PASSWORD);

        this.authenticator = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
              return new PasswordAuthentication(
                username, 
                password.toCharArray());
            }
        };
        
        return this;
    }
    
    public TestResponse to(String uri) {
        Objects.requireNonNull(uri, Required.URI);

        this.uri = uri;
        return this;
    }
    
    public TestResponse withCookie(HttpCookie cookie) {
        Objects.requireNonNull(cookie, Required.COOKIE);
        Config config = Application.getInstance(Config.class);
        String host = config.getConnectorHttpHost();
        int port =  config.getConnectorHttpPort();

        try {
            var requestUri = new URI("http://" + host + ":" + port);
            this.cookieManager.getCookieStore().add(requestUri, cookie);
        } catch (URISyntaxException e) {
            LOG.error("Failed to add cookie", e);
        }

        return this;
    }
    
    public TestResponse withStringBody(String body) {
        if (StringUtils.isNotBlank(body)) {
            this.body = BodyPublishers.ofString(body);   
        }
        
        return this;
    }
    
    public TestResponse withContentType(String contentType) {
        Objects.requireNonNull(contentType, Required.CONTENT_TYPE);

        this.httpRequest.header(CONTENT_TYPE, contentType);
        
        return this;
    }
    
    /** Redirects are followed by default. */
    public TestResponse withDisabledRedirects() {
        this.httpClient.followRedirects(HttpClient.Redirect.NEVER);

        return this;
    }

    /** Sends the parameters URL-encoded as a form POST, overriding method and Content-Type. */
    public TestResponse withForm(Multimap<String, String> parameters) {
        String form = parameters.entries()
                .stream()
                .map(entry -> entry.getKey() + "=" + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
        
        this.httpRequest.header(CONTENT_TYPE, "application/x-www-form-urlencoded");
        this.body = BodyPublishers.ofString(form);
        this.method = Methods.POST.toString();
        
        return this;
    }
    
    public TestResponse execute() {
        final Config config = Application.getInstance(Config.class);
        final String host = config.getConnectorHttpHost();
        final int port =  config.getConnectorHttpPort();
        
        try (var client = this.httpClient.build()) {
            this.url = "http://" + host + ":" + port;
            this.httpRequest
                .uri(new URI(this.url + this.uri))
                .method(this.method, this.body);
            
            if (this.authenticator != null ) {
                this.httpClient.authenticator(this.authenticator);
            }

            this.httpResponse = client.send(this.httpRequest.build(), HttpResponse.BodyHandlers.ofString());
        } catch (URISyntaxException | IOException | InterruptedException e) {
            LOG.error("Failed to execute HTTP request", e);
            Thread.currentThread().interrupt();
        }
        
        return this;
    }
    
    public String getContent() {
        return this.httpResponse.body();
    }

    public HttpResponse<String> getHttpResponse() {
        return this.httpResponse;
    }

    public int getStatusCode() {
        return this.httpResponse.statusCode();
    }

    public String getContentType() {
        return this.httpResponse.headers().firstValue(CONTENT_TYPE).orElse(Strings.EMPTY);
    }

    public String getResponseUrl() {
        return this.url;
    }
    
    public List<HttpCookie> getCookies() {
        return this.cookieManager.getCookieStore().getCookies();
    }

    /** Returns null if no cookie with the given name exists. */
    public HttpCookie getCookie(String name) {
        return this.cookieManager.getCookieStore()
            .getCookies()
            .stream()
            .filter(cookie -> cookie.getName().equals(name))
            .findFirst()
            .orElse(null);
    }

    /** Returns an empty string if the header is not present. */
    public String getHeader(String name) {
        return this.httpResponse.headers().firstValue(name).orElse("");
    }
}