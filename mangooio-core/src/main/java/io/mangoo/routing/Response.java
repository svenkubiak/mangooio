package io.mangoo.routing;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.io.IOUtils;
import org.boon.json.JsonFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;

import io.mangoo.enums.ContentType;
import io.undertow.util.HttpString;
import io.undertow.util.StatusCodes;

/**
 *
 * @author svenkubiak
 *
 */
public final class Response {
    private static final Logger LOG = LoggerFactory.getLogger(Response.class);
    private Map<HttpString, String> headers = new HashMap<HttpString, String>();
    private Map<String, Object> content = new HashMap<String, Object>();
    private String redirectTo;
    private String contentType = ContentType.TEXT_HTML.toString();
    private String charset = Charsets.UTF_8.name();
    private String body = "";
    private String template;
    private String binaryFileName;
    private byte[] binaryContent;
    private boolean endResponse;
    private boolean etag;
    private boolean binary;
    private boolean rendered;
    private boolean redirect;
    private int statusCode = StatusCodes.OK;
    
    public Response(){
    }

    private Response(int statusCode) {
        this.statusCode = statusCode;
    }

    private Response(String redirectTo) {
        this.redirect = true;
        this.rendered = true;
        this.redirectTo = Objects.requireNonNull(redirectTo, "redirectTo can not be null");
    }

    public int getStatusCode() {
        return this.statusCode;
    }

    public String getContentType() {
        return this.contentType;
    }

    public String getCharset() {
        return this.charset;
    }

    public String getBody() {
        return this.body;
    }

    public byte[] getBinaryContent() {
        return this.binaryContent.clone();
    }

    public String getTemplate() {
        return this.template;
    }

    public boolean isETag() {
        return this.etag;
    }

    public String getBinaryFileName() {
        return this.binaryFileName;
    }

    public Map<String, Object> getContent() {
        return this.content;
    }

    public boolean isRedirect() {
        return this.redirect;
    }

    public boolean isBinary() {
        return this.binary;
    }

    public boolean isRendered() {
        return this.rendered;
    }

    public boolean isEndResponse() {
        return this.endResponse;
    }

    public String getRedirectTo() {
        return this.redirectTo;
    }

    public Map<HttpString, String> getHeaders() {
        return headers;
    }

    /**
     * Creates a response object with HTTP status code 200
     *
     * @return A response object {@link io.mangoo.routing.Response}
     */
    public static Response withOk() {
        return new Response(StatusCodes.OK);
    }

    /**
     * Creates a response object with HTTP status code 201
     *
     * @return A response object {@link io.mangoo.routing.Response}
     */
    public static Response withCreated() {
        return new Response(StatusCodes.CREATED);
    }

    /**
     * Creates a response object with HTTP status code 404
     *
     * @return A response object {@link io.mangoo.routing.Response}
     */
    public static Response withNotFound() {
        return new Response(StatusCodes.NOT_FOUND);
    }

    /**
     * Creates a response object with HTTP status code 401
     *
     * @return A response object {@link io.mangoo.routing.Response}
     */
    public static Response withForbidden() {
        return new Response(StatusCodes.FORBIDDEN);
    }

    /**
     * Creates a response object with HTTP status code 403
     *
     * @return A response object {@link io.mangoo.routing.Response}
     */
    public static Response withUnauthorized() {
        return new Response(StatusCodes.UNAUTHORIZED);
    }

    /**
     * Creates a response object with HTTP status code 500
     *
     * @return A response object {@link io.mangoo.routing.Response}
     */
    public static Response withBadRequest() {
        return new Response(StatusCodes.BAD_REQUEST);
    }

    /**
     * Creates a response object with a given HTTP status code
     *
     * @param statusCode The status code to set
     * @return A response object {@link io.mangoo.routing.Response}
     */
    public static Response withStatusCode(int statusCode) {
        return new Response(statusCode);
    }

    /**
     * Creates a response object with a given url to redirect to
     *
     * @param redirectTo The URL to redirect to
     * @return A response object {@link io.mangoo.routing.Response}
     */
    public static Response withRedirect(String redirectTo) {
        return new Response(redirectTo);
    }

    /**
     * Sets a specific template to use for the response
     *
     * @param template The path to the template (e.g. /mytemplate/template.ftl)
     * @return A response object {@link io.mangoo.routing.Response}
     */
    public Response andTemplate(String template) {
        this.template = template;

        return this;
    }

    /**
     * Sets a specific content type to use for the response. Default is "text/html"
     *
     * @param contentType The content type to use
     * @return A response object {@link io.mangoo.routing.Response}
     */
    public Response andContentType(String contentType) {
        this.contentType = contentType;

        return this;
    }

    /**
     * Sets a specific charset to the response
     *
     * @param charset The charset to use
     * @return A response object {@link io.mangoo.routing.Response}
     */
    public Response andCharset(String charset) {
        this.charset = charset;

        return this;
    }

    /**
     * Adds a value to the template that can be accessed using ${name} in the template
     *
     * @param name The name of the value
     * @param object The actual value
     * @return A response object {@link io.mangoo.routing.Response}
     */
    public Response andContent(String name, Object object) {
        this.content.put(name, object);

        return this;
    }

    /**
     * Sets the body of the response. If a body is added, no template rendering will be
     * performed. The default content type "text/html" will be used.
     *
     * @param body The body for the response
     * @return A response object {@link io.mangoo.routing.Response}
     */
    public Response andBody(String body) {
        this.body = body;
        this.rendered = true;

        return this;
    }

    /**
     * Converts a given Object to JSON and passing it to the response. If an object is given, no
     * template rendering will be performed and the content type for the response will be set to
     * "application/json"
     *
     * @param jsonObject The object to convert to JSON
     * @return A response object {@link io.mangoo.routing.Response}
     */
    public Response andJsonBody(Object jsonObject) {
        this.contentType = ContentType.APPLICATION_JSON.toString();
        this.body = JsonFactory.create().toJson(jsonObject);
        this.rendered = true;

        return this;
    }

    /**
     * Sends a binary file to the client skipping rendering
     *
     * @param file The file to send
     * @return A response object {@link io.mangoo.routing.Response}
     */
    public Response andBinaryFile(File file) {
        try (FileInputStream fileInputStream = new FileInputStream(file)){
            this.binaryFileName = file.getName();
            this.binaryContent = IOUtils.toByteArray(fileInputStream);
            this.binary = true;
            this.rendered = true;
        } catch (IOException e) {
            LOG.error("Failed to handle binary file", e);
        }

        return this;
    }

    /**
     * Sends binary content to the client skipping rendering
     *
     * @param content The content to to send
     * @return A response object {@link io.mangoo.routing.Response}
     */
    public Response andBinaryContent(byte [] content) {
        this.binaryContent = content.clone();
        this.binary = true;
        this.rendered = true;

        return this;
    }

    /**
     * Sets the body of the response. If a body is added, no template rendering will be
     * performed. The content type "text/plain" will be used.
     *
     * @param text The text for the response
     *
     * @return A response object {@link io.mangoo.routing.Response}
     */
    public Response andTextBody(String text) {
        this.contentType = ContentType.TEXT_PLAIN.toString();
        this.body = text;
        this.rendered = true;

        return this;
    }

    /**
     * Disables template rendering, sending an empty body in the response
     *
     * @return A response object {@link io.mangoo.routing.Response}
     */
    public Response andEmptyBody() {
        this.rendered = true;

        return this;
    }

    /**
     * Adds an additional header to the request response. If an header
     * key already exists, it will we overwritten with the latest value.
     *
     * @param key The header constant from Headers class (e.g. Headers.CONTENT_TYPE)
     * @param value The header value
     *
     * @return A response object {@link io.mangoo.routing.Response}
     */
    public Response andHeader(HttpString key, String value) {
        this.headers.put(key, value);

        return this;
    }

    /**
     * Adds an additional content map to the content rendered in the template.
     * Already existing values with the same key are overwritten.
     *
     * @param content The content map to add
     * @return A response object {@link io.mangoo.routing.Response}
     */
    public Response andContent(Map<String, Object> content) {
        this.content.putAll(content);

        return this;
    }

    /**
     * Adds an additional header map to the response.
     * Already existing values with the same key are overwritten.
     *
     * @param headers The headers map to add
     * @return A response object {@link io.mangoo.routing.Response}
     */
    public Response andHeaders(Map<HttpString, String> headers) {
        this.headers.putAll(headers);

        return this;
    }

    /**
     * Adds an ETag header to the response by hashing (MD5) the response body.
     *
     * Be aware that for every request the hash has to be generated. This will
     * most likely increase CPU usage.
     *
     * See <a href="https://en.wikipedia.org/wiki/HTTP_ETag">https://en.wikipedia.org/wiki/HTTP_ETag</a>
     *
     * @return A response object {@link io.mangoo.routing.Response}
     */
    public Response andEtag() {
        this.etag = true;

        return this;
    }

    /**
     * Tells a filter that the response ends and that the request handler
     * should not execute further filters by sending the current response
     * to the client. This is only used within a filter.
     *
     * @return A response object {@link io.mangoo.routing.Response}
     */
    public Response end() {
        this.endResponse = true;

        return this;
    }
}