package io.mangoo.test.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.mangoo.configuration.Config;
import io.mangoo.core.Application;
import io.mangoo.enums.Default;
import io.mangoo.enums.Header;
import io.undertow.util.HttpString;
import io.undertow.util.Methods;

/**
 *
 * @author svenkubiak
 *
 */
public class WebResponse {
    private static final Logger LOG = LogManager.getLogger(WebResponse.class);
    private final CookieStore cookieStore = new BasicCookieStore();
    private final Map<String, String> headers = new HashMap<>(); //NOSONAR
    private final MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
    private String responseUrl;
    private String responseUri;
    private String responseRequestBody;
    private String responseContent = "";
    private String responseContentType;
    private HttpString responseMethod;
    private HttpResponse httpResponse;
    private HttpClientBuilder httpClientBuilder;
    private List<NameValuePair> postParameter = new ArrayList<>();
    private boolean responseDisbaleRedirects;
    private boolean hasFileBody;

    public WebResponse (String uri, HttpString method) {
        this.responseUri = uri;
        this.responseMethod = method;
        init();
    }

    public WebResponse() {
        init();
    }

    private void init () {
        final Config config = Application.getInstance(Config.class);
        multipartEntityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

        final String host = config.getConnectorHttpHost();
        final int port =  config.getConnectorHttpPort();

        this.cookieStore.clear();
        this.responseUrl = "http://" + host + ":" + port;
        this.httpClientBuilder = HttpClientBuilder.create()
                .setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build())
                .setDefaultCookieStore(this.cookieStore);
    }

    /**
     * Sets the ContentType of the request
     *
     * @param contentType The content type to use
     * @return Response
     */
    public WebResponse withContentType(String contentType) {
        Objects.requireNonNull(contentType, "contentType can not be null");

        this.responseContentType = contentType;
        return this;
    }

    /**
     * Sets the RequestBody of the request
     *
     * @param requestBody The request body to use
     * @return Response
     */
    public WebResponse withRequestBody(String requestBody) {
        this.responseRequestBody = requestBody;
        return this;
    }

    /**
     * Sets Post parameter to the request
     *
     * @param postParameter A list of post parameter
     * @return Response
     */
    public WebResponse withPostParameters(List<NameValuePair> postParameter) {
        Objects.requireNonNull(postParameter, "postParameter can not be null");

        this.postParameter = Collections.unmodifiableList(postParameter);
        return this;
    }

    /**
     * Disables redirects when the request is executed
     *
     * @param disableRedirects true or false
     * @return Response
     */
    public WebResponse withDisableRedirects(boolean disableRedirects) {
        this.responseDisbaleRedirects = disableRedirects;
        return this;
    }
    
    /**
     * Enables the LaxRedirectsStragey allowing more methods to be redirected
     *
     * @return Response
     */
    public WebResponse withLaxRedirectStrategy() {
        this.httpClientBuilder.setRedirectStrategy(new LaxRedirectStrategy());
        return this;
    }

    /**
     * Sets the URI to be executed by the request
     *
     * @param uri The URI to call
     * @return Response
     */
    public WebResponse withUri(String uri) {
        Objects.requireNonNull(uri, "uri can not be null");

        this.responseUri = uri;
        return this;
    }

    /**
     * Adds a FileBody to the request
     *
     * @param name The the of the file body
     * @param fileBody The file body
     * @return Response
     */
    public WebResponse withFileBody(String name, FileBody fileBody) {
        Objects.requireNonNull(fileBody, "fileBody can not be null");

        this.hasFileBody = true;
        this.multipartEntityBuilder.addPart(name, fileBody);
        return this;
    }

    /**
     * Sets the HTTP method to execute the request with
     *
     * @param method The HTTP Method
     * @return Response
     */
    public WebResponse withMethod(HttpString method) {
        Objects.requireNonNull(method, "method can not be null");

        this.responseMethod = method;
        return this;
    }

    /**
     * Adds an additional header to the request
     *
     * @param name The name of the header
     * @param value The value of the header
     * @return Response
     */
    public WebResponse withHeader(String name, String value) {
        Objects.requireNonNull(name, "name can not be null");
        Objects.requireNonNull(value, "value can not be null");

        this.headers.put(name, value);
        return this;
    }

    /**
     * Adds an additional cookie to the request
     *
     * @param cookie The cookie of the header
     * @return Response
     */
    public WebResponse withCookie(Cookie cookie) {
        Objects.requireNonNull(cookie, "cookie can not be null");

        this.cookieStore.addCookie(cookie);
        return this;
    }

    /**
     * Sets Basic HTTP Authentication the the request
     *
     * @param username The username
     * @param password The password
     * @return Response
     */
    public WebResponse withBasicauthentication(String username, String password) {
        Objects.requireNonNull(username, "username can not be null");
        Objects.requireNonNull(password, "password can not be null");

        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
        this.httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider).setDefaultCookieStore(this.cookieStore);

        return this;
    }

    /**
     * Execute the HTTP request
     *
     * @return Response
     */
    public WebResponse execute() {
        if ((Methods.GET).equals(this.responseMethod)) {
            final HttpGet httpGet = new HttpGet(this.responseUrl + this.responseUri);

            return doRequest(httpGet);
        } else if ((Methods.POST).equals(this.responseMethod)) {
            final HttpPost httpPost = new HttpPost(responseUrl + responseUri);

            try {
                if (this.hasFileBody) {
                    httpPost.setEntity(multipartEntityBuilder.build());
                } else if (StringUtils.isNotBlank(this.responseRequestBody)) {
                    httpPost.setEntity(new StringEntity(this.responseRequestBody));
                } else {
                    httpPost.setEntity(new UrlEncodedFormEntity(this.postParameter, Default.ENCODING.toString()));
                }
            } catch (final UnsupportedEncodingException e) {
                LOG.error("Failed to create HttpPost request", e);
            }

            return doRequest(httpPost);
        } else if ((Methods.PUT).equals(this.responseMethod)) {
            final HttpPut httpPut = new HttpPut(responseUrl + responseUri);

            try {
                if (StringUtils.isNotBlank(this.responseRequestBody)) {
                    httpPut.setEntity(new StringEntity(this.responseRequestBody));
                } else {
                    httpPut.setEntity(new UrlEncodedFormEntity(this.postParameter, Default.ENCODING.toString()));
                }
            } catch (final UnsupportedEncodingException e) {
                LOG.error("Failed to create HttpPut request", e);
            }

            return doRequest(httpPut);
        } else if ((Methods.DELETE).equals(this.responseMethod)) {
            final HttpDelete httpDelete = new HttpDelete(this.responseUrl + this.responseUri);

            return doRequest(httpDelete);
        } else if ((Methods.HEAD).equals(this.responseMethod)) {
            final HttpHead httpHead = new HttpHead(this.responseUrl + this.responseUri);

            return doRequest(httpHead);
        } else if ((Methods.OPTIONS).equals(this.responseMethod)) {
            final HttpOptions httpOptions = new HttpOptions(this.responseUrl + this.responseUri);

            return doRequest(httpOptions);
        } else if ((Methods.PATCH).equals(this.responseMethod)) {
            final HttpPatch httpPatch = new HttpPatch(this.responseUrl + this.responseUri);

            try {
                if (StringUtils.isNotBlank(this.responseRequestBody)) {
                    httpPatch.setEntity(new StringEntity(this.responseRequestBody));
                } else {
                    httpPatch.setEntity(new UrlEncodedFormEntity(this.postParameter, Default.ENCODING.toString()));
                }
            } catch (final UnsupportedEncodingException e) {
                LOG.error("Failed to create HttpPut request", e);
            }
            
            return doRequest(httpPatch);
        } else {
            // Ignore any other HTTP methods
        }

        return this;
    }

    /**
     * Performs the actual HTTP request
     *
     * @param request The HTTP request
     * @return Response
     */
    private WebResponse doRequest(HttpUriRequest request) {
        if (this.responseContentType != null) {
            request.setHeader(Header.CONTENT_TYPE.toString(), responseContentType);
        }

        this.headers.entrySet().forEach(entry -> request.setHeader(entry.getKey(), entry.getValue())); //NOSONAR

        if (this.responseDisbaleRedirects) {
            this.httpClientBuilder.disableRedirectHandling();
        }

        try {
            this.httpResponse = this.httpClientBuilder.build().execute(request);

            final HttpEntity httpEntity = this.httpResponse.getEntity();
            if (httpEntity != null) {
                this.responseContent = EntityUtils.toString(httpEntity);
            }
        } catch (final IOException e) {
            LOG.error("Failed to execute request to " + responseUrl, e);
        }

        return this;
    }

    /**
     * @return The response content
     */
    public String getContent() {
        return this.responseContent;
    }

    /**
     * @return The HTTP response object
     */
    public HttpResponse getHttpResponse() {
        return this.httpResponse;
    }

    /**
     * @return The status code of the response
     */
    public int getStatusCode() {
        return this.httpResponse.getStatusLine().getStatusCode();
    }

    /**
     * Retrieves a specific header with the given name
     *
     * @param name The name of the header
     * @return The value of the header or null if not found
     */
    public String getHeader(String name) {
        return (this.httpResponse.getFirstHeader(name) == null ) ? null : this.httpResponse.getFirstHeader(name).getValue();
    }

    /**
     * @return The response cookie or an empty list
     */
    public List<Cookie> getCookies() {
        return this.cookieStore.getCookies();
    }

    /**
     * Retrieves the cookie from the cookie store with a given name
     *
     * @param name The name of the cookie
     * @return A Cookie or null if non found by name
     */
    public Cookie getCookie(String name) {
        for (final Cookie cookie : this.cookieStore.getCookies()) {
            if (name.equals(cookie.getName())) {
                return cookie;
            }
        }

        return null;
    }

    /**
     * @return The content type of the response
     */
    public String getContentType() {
        return this.httpResponse.getEntity().getContentType().getValue();
    }

    /**
     * @return The response URL
     */
    public String getResponseUrl() {
        return responseUrl;
    }
}