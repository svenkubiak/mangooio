package io.mangoo.routing;

import com.google.common.net.MediaType;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.mangoo.constants.Header;
import io.mangoo.constants.NotNull;
import io.mangoo.utils.JsonUtils;
import io.undertow.server.handlers.Cookie;
import io.undertow.server.handlers.CookieImpl;
import io.undertow.util.HttpString;
import io.undertow.util.StatusCodes;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Response {
    private static final Logger LOG = LogManager.getLogger(Response.class);
    private final Map<HttpString, String> headers = new HashMap<>();
    private final Map<String, Object> content = new HashMap<>();
    private final List<Cookie> cookies = new ArrayList<>();
    private String redirectTo;
    private String contentType = MediaType.HTML_UTF_8.withoutParameters().toString();
    private String charset = StandardCharsets.UTF_8.name();
    private String body = "";
    private String template;
    private String binaryFileName;
    private byte[] binaryContent;
    private boolean endResponse;
    private boolean binary;
    private boolean rendered;
    private boolean redirect;
    private int statusCode = StatusCodes.OK;

    public Response() {
        //Empty constructor for Google Guice
    }

    private Response(int statusCode) {
        this.statusCode = statusCode;
        this.rendered = true;
    }

    private Response(String redirectTo) {
        Objects.requireNonNull(redirectTo, NotNull.REDIRECT_TO);
        
        this.redirect = true;
        this.rendered = false;
        this.redirectTo = redirectTo;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getContentType() {
        return contentType;
    }

    public String getCharset() {
        return charset;
    }

    public String getBody() {
        return body;
    }

    public List<Cookie> getCookies() {
        return new ArrayList<>(cookies);
    }

    public byte[] getBinaryContent() {
        return binaryContent.clone();
    }

    public String getTemplate() {
        return template;
    }

    public String getBinaryFileName() {
        return binaryFileName;
    }

    public Map<String, Object> getContent() {
        return content;
    }

    public boolean isRedirect() {
        return redirect;
    }

    public boolean isBinary() {
        return binary;
    }

    public boolean isRendered() {
        return rendered;
    }

    public boolean isEndResponse() {
        return endResponse;
    }
    
    public String getRedirectTo() {
        return redirectTo;
    }

    public Map<HttpString, String> getHeaders() {
        return headers;
    }
    
    public String getHeader(HttpString header) {
        Objects.requireNonNull(header, NotNull.HEADER);
        return headers.get(header);
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
     * Creates a response object with HTTP status code 400
     *
     * @return A response object {@link io.mangoo.routing.Response}
     */
    public static Response withBadRequest() {
        return new Response(StatusCodes.BAD_REQUEST);
    }
    
    /**
     * Creates a response object with HTTP status code 500
     *
     * @return A response object {@link io.mangoo.routing.Response}
     */
    public static Response withInternalServerError() {
        return new Response(StatusCodes.INTERNAL_SERVER_ERROR);
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
        Objects.requireNonNull(redirectTo, NotNull.REDIRECT_TO);

        return new Response(redirectTo);
    }

    /**
     * Sets a specific template to use for the response
     *
     * @param template The path to the template (e.g. /mytemplate/template.ftl)
     * @return A response object {@link io.mangoo.routing.Response}
     */
    public Response andTemplate(String template) {
        Objects.requireNonNull(template, NotNull.TEMPLATE);
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
        Objects.requireNonNull(contentType, NotNull.CONTENT_TYPE);
        
        headers.put(Header.CONTENT_TYPE, contentType);
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
        Objects.requireNonNull(charset, NotNull.CHARSET);
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
        Objects.requireNonNull(name, NotNull.NAME);
        content.put(name, object);

        return this;
    }

    /**
     * Sets the body of the response. If a body is added, no template rendering will be
     * performed. The default content type "text/html" will be used.
     *
     * @param body The body for the response
     * @return A response object {@link io.mangoo.routing.Response}
     */
    public Response andHtmlBody(String body) {
        this.body = body;
        rendered = false;

        return this;
    }

    /**
     * Adds a Cookie to the response which is passed to the client
     *
     * @param cookie The cookie to add
     * @return A response object {@link io.mangoo.routing.Response}
     */
    public Response andCookie(Cookie cookie) {
        Objects.requireNonNull(cookie, NotNull.COOKIE);
        cookies.add(cookie);

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
        Objects.requireNonNull(jsonObject, NotNull.JSON_OBJECT);

        this.body = JsonUtils.toJson(jsonObject);
        contentType = MediaType.JSON_UTF_8.withoutParameters().toString();
        rendered = false;

        return this;
    }
    
    /**
     * Sets a given JSON string as body. If a String is given, no template rendering will be
     * performed and the content type for the response will be set to "application/json"
     *
     * @param json The String to set as JSON
     * @return A response object {@link io.mangoo.routing.Response}
     */
    public Response andJsonBody(String json) {
        Objects.requireNonNull(json, NotNull.JSON);

        this.body = json;
        contentType = MediaType.JSON_UTF_8.withoutParameters().toString();
        rendered = false;

        return this;
    }    

    /**
     * Sends a binary file to the client skipping rendering
     *
     * @param file The file to send
     * @return A response object {@link io.mangoo.routing.Response}
     */
    @SuppressFBWarnings(justification = "null check of file on entry point of method", value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    public Response andBinaryFile(Path file) {
        Objects.requireNonNull(file, NotNull.FILE);

        try (var inputStream = Files.newInputStream(file)) {
            binaryFileName = file.getFileName().toString();
            binaryContent = IOUtils.toByteArray(inputStream);
            binary = true;
            rendered = false;
        } catch (final IOException e) {
            LOG.error("Failed to handle binary file", e);
        }

        return this;
    }

    /**
     * Sends binary content to the client skipping rendering
     *
     * @param content The content to send
     * @return A response object {@link io.mangoo.routing.Response}
     */
    public Response andBinaryContent(byte [] content) {
        Objects.requireNonNull(content, NotNull.CONTENT);

        binaryContent = content.clone();
        binary = true;
        rendered = false;

        return this;
    }

    /**
     * Sets the body of the response. If a body is added, no template rendering will be
     * performed. The content type "text/plain" will be used.
     *
     * @param body The text for the response
     *
     * @return A response object {@link io.mangoo.routing.Response}
     */
    public Response andTextBody(String body) {
        this.body = body;
        contentType = MediaType.PLAIN_TEXT_UTF_8.withoutParameters().toString();
        rendered = false;

        return this;
    }

    /**
     * Disables template rendering, sending an empty body with content-type
     * text/plain in the response
     *
     * @return A response object {@link io.mangoo.routing.Response}
     */
    public Response andEmptyBody() {
        contentType = MediaType.PLAIN_TEXT_UTF_8.withoutParameters().toString();
        rendered = false;

        return this;
    }

    /**
     * Adds a header to the request response. If a header
     * key already exists, it will be overwritten with the latest value.
     *
     * @param key The header constant from Headers class (e.g. Header.CONTENT_TYPE.toString())
     * @param value The header value
     * 
     * @return A response object {@link io.mangoo.routing.Response}
     */
    public Response andHeader(HttpString key, String value) {
        Objects.requireNonNull(key, NotNull.KEY);
        headers.put(key, value);

        return this;
    }
    
    /**
     * Adds a header to the request response. If a header
     * key already exists, it will be overwritten with the latest value.
     *
     * @param key The header key
     * @param value The header value
     *
     * @return A response object {@link io.mangoo.routing.Response}
     */
    public Response andHeader(String key, String value) {
        Objects.requireNonNull(key, NotNull.KEY);
        headers.put(new HttpString(key), value);

        return this;
    }

    /**
     * Adds a content map to the content rendered in the template.
     * Already existing values with the same key are overwritten.
     *
     * @param content The content map to add
     * @return A response object {@link io.mangoo.routing.Response}
     */
    public Response andContent(Map<String, Object> content) {
        Objects.requireNonNull(content, NotNull.CONTENT);
        this.content.putAll(content);

        return this;
    }

    /**
     * Adds a header map to the response.
     * Already existing values with the same key are overwritten.
     *
     * @param headers The headers map to add
     * @return A response object {@link io.mangoo.routing.Response}
     */
    public Response andHeaders(Map<HttpString, String> headers) {
        Objects.requireNonNull(headers, NotNull.HEADERS);
        this.headers.putAll(headers);

        return this;
    }
    
    /**
     * Disposes a cookie by setting the expired date of the give cookie name
     * to a date in the past, max age to -1 and an empty value
     * 
     * @param cookieName The cookie name to dispose
     * @return A response object {@link io.mangoo.routing.Response}
     */
    public Response andDisposeCookie(String cookieName) {
        Objects.requireNonNull(cookieName, NotNull.COOKIE);
        
        cookies.add(new CookieImpl(cookieName)
                .setPath("/")
                .setValue("")
                .setMaxAge(-1)
                .setDiscard(true)
                .setExpires(new Date(1)));
        
        return this;
    }

    /**
     * Tells a filter that the response ends and that the request handler
     * should not execute further filters by sending the current response
     * to the client. This is only used within a filter.
     *
     * @return A response object {@link io.mangoo.routing.Response}
     */
    public Response andEndResponse() {
        endResponse = true;

        return this;
    }
}