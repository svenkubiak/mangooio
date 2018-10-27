package io.mangoo.test.http;

import java.io.IOException;
import java.net.Authenticator;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Multimap;

import biz.gabrys.lesscss.compiler.StringUtils;
import io.mangoo.core.Application;
import io.mangoo.core.Config;
import io.mangoo.enums.Default;
import io.undertow.util.Methods;

/**
 * 
 * @author svenkubiak
 *
 */
public class Response {
    private static final Logger LOG = LogManager.getLogger(Response.class);
    private CookieManager cookieManager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
    private Authenticator authenticator;
    private HttpRequest.Builder httpRequest;
    private BodyPublisher body = BodyPublishers.noBody();
    private HttpClient.Builder httpClient;
    private HttpResponse<String> httpResponse;
    private String uri;
    private String url;
    private String method;
    
    public Response (String uri, String method) {
        this.uri = uri;
        this.method = method;
        init();
    }

    public Response() {
        init();
    }

    private void init() {
        this.httpRequest = HttpRequest.newBuilder();
        this.httpRequest.timeout(Duration.of(2, ChronoUnit.SECONDS));
        this.httpClient = HttpClient.newBuilder();
        this.httpClient.followRedirects(HttpClient.Redirect.ALWAYS);
        this.httpClient.cookieHandler(this.cookieManager);
    }
    
    /**
     * Adds an additional header to the request
     *
     * @param name The name of the header
     * @param value The value of the header
     * @return TestResponse instance
     */
    public Response withHeader(String name, String value) {
        Objects.requireNonNull(name, "name can not be null");
        Objects.requireNonNull(value, "value can not be null");
        
        this.httpRequest.header(name, value);
        
        return this;
    }
    
    /**
     * Sets the HTTP method to execute the request with
     *
     * @param method The HTTP Method
     * 
     * @return TestResponse instance
     */
    public Response withHTTPMethod(String method) {
        Objects.requireNonNull(method, "method can not be null");
        
        this.method = method;
        
        return this;
    }
    
    /**
     * Sets a timeout to the HTTP request
     * 
     * Default is 2 seconds
     *
     * @param amount The amount of time
     * @param unit The unit of time
     * 
     * @return TestResponse instance
     */
    public Response withTimeout(long amount, TemporalUnit unit) {
        Objects.requireNonNull(method, "method can not be null");

        this.httpRequest.timeout(Duration.of(amount, unit));
        
        return this;
    }
    
    /**
     * Sets Basic HTTP Authentication the the request
     *
     * @param username The username
     * @param password The password
     * 
     * @return TestResponse instance
     */
    public Response withBasicAuthentication(String username, String password) {
        Objects.requireNonNull(username, "username can not be null");
        Objects.requireNonNull(password, "password can not be null");

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
    
    /**
     * Sets the URI to be executed by the request
     *
     * @param uri The URI to call
     * 
     * @return TestResponse instance
     */
    public Response to(String uri) {
        Objects.requireNonNull(uri, "uri can not be null");

        this.uri = uri;
        return this;
    }
    
    /**
     * Adds an additional cookie to the request
     *
     * @param cookie The cookie of the header
     * 
     * @return TestResponse instance
     */
    public Response withCookie(HttpCookie cookie) {
        Objects.requireNonNull(cookie, "cookie can not be null");

        this.cookieManager.getCookieStore().add(null, cookie);
        
        return this;
    }
    
    /**
     * Sets the RequestBody of the request
     *
     * @param body The request body to use
     * 
     * @return TestResponse instance
     */
    public Response withStringBody(String body) {
        if (StringUtils.isNotBlank(body)) {
            this.body = BodyPublishers.ofString(body);   
        }
        
        return this;
    }
    
    /**
     * Sets the ContentType of the request
     *
     * @param contentType The content type to use
     * 
     * @return TestResponse instance
     */
    public Response withContentType(String contentType) {
        Objects.requireNonNull(contentType, "contentType can not be null");

        this.httpRequest.header("Content-Type", contentType);
        
        return this;
    }
    
    /**
     * Disables redirects when the request is executed
     * 
     * Default is HttpClient.Redirect.ALWAYS
     *
     * @param disableRedirects true or false
     * 
     * @return TestResponse instance
     */
    public Response withDisableRedirects(boolean redirect) {
        if (redirect) {
            this.httpClient.followRedirects(HttpClient.Redirect.NEVER);
        }
        
        return this;
    }

    /**
     * Simulates a FORM post by setting:
     * 
     * Content-Type to application/x-www-form-urlencoded
     * HTTP method to POST
     * URLEncoding the given parameters
     * 
     * @param parameters The parameters to use
     * @return TestResponse instance
     */
    public Response withForm(Multimap<String, String> parameters) {
        String form = parameters.entries()
                .stream()
                .map(entry -> entry.getKey() + "=" + URLEncoder.encode(entry.getValue(), Charset.forName(Default.ENCODING.toString())))
                .collect(Collectors.joining("&"));
        
        this.httpRequest.header("Content-Type", "application/x-www-form-urlencoded");
        this.body = BodyPublishers.ofString(form);
        this.method = Methods.POST.toString();
        
        return this;
    }
    
    /**
     * Executes the HTTP request
     * 
     * @return TestResponse instance with response parameters
     */
    public Response execute() {
        final Config config = Application.getInstance(Config.class);
        final String host = config.getConnectorHttpHost();
        final int port =  config.getConnectorHttpPort();
        
        try {
            this.url = "http://" + host + ":" + port;
            this.httpRequest
                .uri(new URI(this.url + this.uri))
                .method(this.method, this.body);
            
            if (this.authenticator != null ) {
                this.httpClient.authenticator(this.authenticator);
            }
            
            this.httpResponse = this.httpClient
                    .build()
                    .send(this.httpRequest.build(), HttpResponse.BodyHandlers.ofString());
        } catch (URISyntaxException | IOException | InterruptedException e) {
            LOG.error("Failed to execute HTTP request", e);
        }
        
        return this;
    }
    
    /**
     * @return The response content
     */
    public String getContent() {
        return this.httpResponse.body();
    }

    /**
     * @return The HTTP response object
     */
    public HttpResponse<String> getHttpResponse() {
        return this.httpResponse;
    }

    /**
     * @return The status code of the response
     */
    public int getStatusCode() {
        return this.httpResponse.statusCode();
    }

    /**
     * @return The content type of the response
     */
    public String getContentType() {
        return this.httpResponse.headers().firstValue("Content-Type").orElse("");
    }

    /**
     * @return The URL of the response
     */
    public String getResponseUrl() {
        return this.url;
    }
    
    /**
     * @return The response cookie or an empty list
     */
    public List<HttpCookie> getCookies() {
        return this.cookieManager.getCookieStore().getCookies();
    }

    /**
     * Retrieves a cookie from the cookie store with a given name
     *
     * @param name The name of the cookie
     * @return A Cookie or null if non found by name
     */
    public HttpCookie getCookie(String name) {
        return this.cookieManager.getCookieStore()
            .getCookies()
            .stream()
            .filter(cookie -> cookie.getName().equals(name))
            .findFirst()
            .orElse(null);
    }

    /**
     * Retrieves a specific header with the given name
     *
     * @param name The name of the header
     * @return The value of the header or an empty string if not found
     */
    public String getHeader(String name) {
        return this.httpResponse.headers().firstValue(name).orElse("");
    }
}