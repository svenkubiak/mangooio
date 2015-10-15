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
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger LOG = LoggerFactory.getLogger(MangooResponse.class);
    private String responseUrl;
    private String responseUri;
    private HttpString responseMethod;
    private String responseContent = "";
    private HttpResponse httpResponse;
    private ContentType responseContentType;
    private String responseRequestBody;
    private boolean responseDisbaleRedirects;
    private List<NameValuePair> postParameter = new ArrayList<NameValuePair>();
    private CookieStore cookieStore = new BasicCookieStore();
    private HttpClient httpClient;
    private HttpClient httpClientNoRedirects;
    private Map<String, String> headers = new HashMap<String, String>();

    public MangooResponse (String uri, HttpString method) {
        this.responseUri = uri;
        this.responseMethod = method;
        init();
    }

    public MangooResponse() {
        init();
    }

    private void init () {
        Config config = MangooTestInstance.IO.getInjector().getInstance(Config.class);

        String host = config.getString(Key.APPLICATION_HOST, Default.APPLICATION_HOST.toString());
        int port = config.getInt(Key.APPLICATION_PORT, Default.APPLICATION_PORT.toInt());

        this.cookieStore.clear();
        this.responseUrl = "http://" + host + ":" + port;
        this.httpClient = HttpClientBuilder.create().setDefaultCookieStore(this.cookieStore).build();
        this.httpClientNoRedirects = HttpClientBuilder.create().setDefaultCookieStore(this.cookieStore).disableRedirectHandling().build();
    }

    public MangooResponse contentType(ContentType contentType) {
        this.responseContentType = contentType;
        return this;
    }

    public MangooResponse requestBody(String requestBody) {
        this.responseRequestBody = requestBody;
        return this;
    }

    public MangooResponse postParameters(List<NameValuePair> postParameter) {
        this.postParameter = Collections.unmodifiableList(postParameter);
        return this;
    }

    public MangooResponse disableRedirects(boolean disableRedirects) {
        this.responseDisbaleRedirects = disableRedirects;
        return this;
    }

    public MangooResponse uri(String uri) {
        this.responseUri = uri;
        return this;
    }

    public MangooResponse header(String name, String value) {
        this.headers.put(name, value);
        return this;
    }

    public MangooResponse method(HttpString method) {
        this.responseMethod = method;
        return this;
    }

    public MangooResponse execute() {
        if (this.responseMethod.equals(Methods.GET)) {
            HttpGet httpGet = new HttpGet(this.responseUrl + this.responseUri);

            return doRequest(httpGet);
        } else if (this.responseMethod.equals(Methods.POST)) {
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
        } else if (this.responseMethod.equals(Methods.PUT)) {
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
        } else if (this.responseMethod.equals(Methods.DELETE)) {
            HttpDelete httpDelete = new HttpDelete(this.responseUrl + this.responseUri);

            return doRequest(httpDelete);
        }

        return this;
    }

    private MangooResponse doRequest(HttpUriRequest request) {
        if (this.responseContentType != null) {
            request.setHeader(Headers.CONTENT_TYPE_STRING, responseContentType.toString());
        }

        for (Map.Entry<String, String> entry : this.headers.entrySet()) {
            request.setHeader(entry.getKey(), entry.getValue());
        }

        try {
            if (this.responseDisbaleRedirects) {
                this.httpResponse = this.httpClientNoRedirects.execute(request);
            } else {
                this.httpResponse = this.httpClient.execute(request);
            }
            HttpEntity httpEntity = this.httpResponse.getEntity();
            if (httpEntity != null) {
                this.responseContent = EntityUtils.toString(this.httpResponse.getEntity());
            }
        } catch (IOException e) {
            LOG.error("Failed to execute request to " + responseUrl, e);
        }

        return this;
    }

    public String getContent() {
        return this.responseContent;
    }

    public HttpResponse getHttpResponse() {
        return this.httpResponse;
    }

    public int getStatusCode() {
        return this.httpResponse.getStatusLine().getStatusCode();
    }

    public List<Cookie> getCookies() {
        return (this.cookieStore.getCookies() == null) ? new ArrayList<Cookie>() : this.cookieStore.getCookies();
    }

    public String getContentType() {
        return this.httpResponse.getEntity().getContentType().getValue();
    }

    public String getResponseUrl() {
        return responseUrl;
    }
}