package io.mangoo.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.mangoo.configuration.Config;
import io.mangoo.enums.ContentType;
import io.mangoo.enums.Default;
import io.mangoo.enums.Key;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;
import io.undertow.util.Methods;

/**
 *
 * @author svenkubiak
 *
 */
public class MangooResponse {
    private static final Logger LOG = LogManager.getLogger(MangooResponse.class);
    private String responseUrl;
    private String responseUri;
    private String responseRequestBody;
    private String responseContent = "";
    private HttpString responseMethod;
    private HttpResponse httpResponse;
    private ContentType responseContentType;
    private HttpClientBuilder httpClientBuilder;
    private List<NameValuePair> postParameter = new ArrayList<NameValuePair>();
    private CookieStore cookieStore = new BasicCookieStore();
    private Map<String, String> headers = new HashMap<String, String>();
    private boolean responseDisbaleRedirects;
    
    public MangooResponse (String uri, HttpString method) {
        this.responseUri = uri;
        this.responseMethod = method;
        init();
    }

    public MangooResponse() {
        init();
    }

    private void init () {
        Config config = MangooInstance.TEST.getInstance(Config.class);

        String host = config.getString(Key.APPLICATION_HOST, Default.APPLICATION_HOST.toString());
        int port = config.getInt(Key.APPLICATION_PORT, Default.APPLICATION_PORT.toInt());

        this.cookieStore.clear();
        this.responseUrl = "http://" + host + ":" + port;
        this.httpClientBuilder = HttpClientBuilder.create().setDefaultCookieStore(this.cookieStore);
    }

    /**
     * Sets the ContentType of the request
     * 
     * @param contentType The content type to use
     * @return MangooResponse 
     */
    public MangooResponse withContentType(ContentType contentType) {
        this.responseContentType = contentType;
        return this;
    }

    /**
     * Sets the RequestBody of the request
     * 
     * @param requestBody The request body to use
     * @return MangooResponse 
     */
    public MangooResponse withRequestBody(String requestBody) {
        this.responseRequestBody = requestBody;
        return this;
    }

    /**
     * Sets Post parameter to the request
     * 
     * @param postParameter A list of post parameter
     * @return MangooResponse
     */
    public MangooResponse withPostParameters(List<NameValuePair> postParameter) {
        this.postParameter = Collections.unmodifiableList(postParameter);
        return this;
    }

    /**
     * Disables redirects when the request is executed
     * 
     * @param disableRedirects true or false
     * @return MangooResponse
     */
    public MangooResponse withDisableRedirects(boolean disableRedirects) {
        this.responseDisbaleRedirects = disableRedirects;
        return this;
    }

    /**
     * Sets the URI to be executed by the request
     * 
     * @param uri The URI to call
     * @return MangooResponse
     */
    public MangooResponse withUri(String uri) {
        this.responseUri = uri;
        return this;
    }
    
    /**
     * Sets the HTTP method to execute the request with
     * 
     * @param method The HTTP Method 
     * @return MangooResponse
     */
    public MangooResponse withMethod(HttpString method) {
        this.responseMethod = method;
        return this;
    }

    /**
     * Adds an additional header to the request
     * 
     * @param name The name of the header
     * @param value The value of the header
     * @return MangooResponse
     */
    public MangooResponse withHeader(String name, String value) {
        this.headers.put(name, value);
        return this;
    }
    
    /**
     * Sets Basic HTTP Authentication the the request
     * 
     * @param username The username
     * @param password The password
     * @return MangooResponse
     */
    public MangooResponse withBasicauthentication(String username, String password) {
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
        this.httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider).setDefaultCookieStore(this.cookieStore);
        
        return this;
    }

    /**
     * Execute the HTTP request
     * 
     * @return MangooResponse
     */
    public MangooResponse execute() {
        if ((Methods.GET).equals(this.responseMethod)) {
            HttpGet httpGet = new HttpGet(this.responseUrl + this.responseUri);

            return doRequest(httpGet);
        } else if ((Methods.POST).equals(this.responseMethod)) {
            HttpPost httpPost = new HttpPost(responseUrl + responseUri);

            try {
                if (StringUtils.isNotBlank(this.responseRequestBody)) {
                    httpPost.setEntity(new StringEntity(this.responseRequestBody));
                } else {
                    httpPost.setEntity(new UrlEncodedFormEntity(this.postParameter, "UTF-8"));
                }
            } catch (UnsupportedEncodingException e) {
                LOG.error("Failed to create HttpPost request", e);
            }

            return doRequest(httpPost);
        } else if ((Methods.PUT).equals(this.responseMethod)) {
            HttpPut httpPut = new HttpPut(responseUrl + responseUri);

            try {
                if (StringUtils.isNotBlank(this.responseRequestBody)) {
                    httpPut.setEntity(new StringEntity(this.responseRequestBody));
                } else {
                    httpPut.setEntity(new UrlEncodedFormEntity(this.postParameter, "UTF-8"));
                }
            } catch (UnsupportedEncodingException e) {
                LOG.error("Failed to create HttpPut request", e);
            }

            return doRequest(httpPut);     
        } else if ((Methods.DELETE.equals(this.responseMethod))) {
            HttpDelete httpDelete = new HttpDelete(this.responseUrl + this.responseUri);

            return doRequest(httpDelete);
        }

        return this;
    }

    /**
     * Performs the actual HTTP request
     * 
     * @param request The HTTP request
     * @return MangooResponse
     */
    private MangooResponse doRequest(HttpUriRequest request) {
        if (this.responseContentType != null) {
            request.setHeader(Headers.CONTENT_TYPE_STRING, responseContentType.toString());
        }

        this.headers.entrySet().forEach((entry) -> request.setHeader(entry.getKey(), entry.getValue()));

        if (responseDisbaleRedirects) {
            this.httpClientBuilder.disableRedirectHandling();
        } 
        
        try {
            this.httpResponse = this.httpClientBuilder.build().execute(request);
            
            HttpEntity httpEntity = this.httpResponse.getEntity();
            if (httpEntity != null) {
                this.responseContent = EntityUtils.toString(httpEntity);
            }
        } catch (IOException e) {
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
     * @return The response cookie or an empty list
     */
    public List<Cookie> getCookies() {
        return (this.cookieStore.getCookies() == null) ? new ArrayList<Cookie>() : this.cookieStore.getCookies();
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