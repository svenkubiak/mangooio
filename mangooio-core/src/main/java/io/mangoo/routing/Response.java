package io.mangoo.routing;

import com.google.common.net.MediaType;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.mangoo.constants.Header;
import io.mangoo.constants.NotNull;
import io.mangoo.constants.Template;
import io.mangoo.models.Error;
import io.mangoo.utils.JsonUtils;
import io.undertow.server.handlers.Cookie;
import io.undertow.server.handlers.CookieImpl;
import io.undertow.util.HttpString;
import io.undertow.util.StatusCodes;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;

import java.io.IOException;
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
    private String body = Strings.EMPTY;
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

    private Response(int statusCode, boolean rendered) {
        this.statusCode = statusCode;
        this.rendered = rendered;
    }

    private Response(String redirectTo) {
        Objects.requireNonNull(redirectTo, NotNull.REDIRECT_TO);
        
        this.redirect = true;
        this.rendered = false;
        this.redirectTo = redirectTo;
    }

    /**
     * Creates a response object with HTTP status code 200
     * @deprecated As of release 8.6.0, replaced by {@link #ok()}
     *
     * @return The response object
     */
    @Deprecated(since = "8.6.0", forRemoval = true)
    public static Response withOk() {
        return new Response(StatusCodes.OK, true);
    }

    /**
     * Creates a response object with HTTP status code 200
     * and rendering a response body from a template
     *
     * @return The response object
     */
    public static Response ok() {
        return new Response(StatusCodes.OK, true);
    }

    /**
     * Creates a response object with HTTP status code 201
     * @deprecated As of release 8.6.0, replaced by {@link #created()}
     *
     * @return The response object
     */
    @Deprecated(since = "8.6.0", forRemoval = true)
    public static Response withCreated() {
        return new Response(StatusCodes.CREATED, true);
    }

    /**
     * Creates a response object with HTTP status code 201
     * and rendering a response body from a template
     *
     * @return The response object
     */
    public static Response created() {
        return new Response(StatusCodes.CREATED, true);
    }

    /**
     * Creates a response object with HTTP status code 404
     * @deprecated As of release 8.6.0, replaced by {@link #notFound()}
     *
     * @return The response object
     */
    @Deprecated(since = "8.6.0", forRemoval = true)
    public static Response withNotFound() {
        return new Response(StatusCodes.NOT_FOUND, true);
    }

    /**
     * Creates a response object with HTTP status code 404
     * and rendering a response body from a template
     *
     * @return The response object
     */
    public static Response notFound() {
        return new Response(StatusCodes.NOT_FOUND, true);
    }

    /**
     * Creates a response object with HTTP status code 401
     * @deprecated As of release 8.6.0, replaced by {@link #forbidden()}
     *
     * @return The response object
     */
    @Deprecated(since = "8.6.0", forRemoval = true)
    public static Response withForbidden() {
        return new Response(StatusCodes.FORBIDDEN, true);
    }

    /**
     * Creates a response object with HTTP status code 401
     * and rendering a response body from a template
     *
     * @return The response object
     */
    public static Response forbidden() {
        return new Response(StatusCodes.FORBIDDEN, true);
    }

    /**
     * Creates a response object with HTTP status code 403
     * @deprecated As of release 8.6.0, replaced by {@link #unauthorized()}
     *
     * @return The response object
     */
    @Deprecated(since = "8.6.0", forRemoval = true)
    public static Response withUnauthorized() {
        return new Response(StatusCodes.UNAUTHORIZED, true);
    }

    /**
     * Creates a response object with HTTP status code 403
     * and rendering a response body from a template
     *
     * @return The response object
     */
    public static Response unauthorized() {
        return new Response(StatusCodes.UNAUTHORIZED, true);
    }

    /**
     * Creates a response object with HTTP status code 400
     * @deprecated As of release 8.6.0, replaced by {@link #badRequest()}
     *
     * @return The response object
     */
    @Deprecated(since = "8.6.0", forRemoval = true)
    public static Response withBadRequest() {
        return new Response(StatusCodes.BAD_REQUEST, true);
    }

    /**
     * Creates a response object with HTTP status code 400
     * and rendering a response body from a template
     *
     * @return The response object
     */
    public static Response badRequest() {
        return new Response(StatusCodes.BAD_REQUEST, true);
    }
    
    /**
     * Creates a response object with HTTP status code 500
     * @deprecated As of release 8.6.0, replaced by {@link #internalServerError()}
     *
     * @return The response object
     */
    @Deprecated(since = "8.6.0", forRemoval = true)
    public static Response withInternalServerError() {
        return new Response(StatusCodes.INTERNAL_SERVER_ERROR, true);
    }

    /**
     * Creates a response object with HTTP status code 500
     * and rendering a response body from a template
     *
     * @return The response object
     */
    public static Response internalServerError() {
        return new Response(StatusCodes.INTERNAL_SERVER_ERROR, true);
    }
    
    /**
     * Creates a response object with a given HTTP status code
     * @deprecated As of release 8.6.0, replaced by {@link #status(int)}
     *
     * @param statusCode The status code to set
     * @return The response object
     */
    @Deprecated(since = "8.6.0", forRemoval = true)
    public static Response withStatusCode(int statusCode) {
        return new Response(statusCode, true);
    }

    /**
     * Creates a response object with a given HTTP status code
     * and rendering a response body from a template
     *
     * @param statusCode The status code to set
     * @return The response object
     */
    public static Response status(int statusCode) {
        return new Response(statusCode, true);
    }

    /**
     * Creates a response object with a given url to redirect to
     * @deprecated As of release 8.6.0, replaced by {@link #redirect(String)}
     *
     * @param redirectTo The URL to redirect to
     * @return The response object
     */
    @Deprecated(since = "8.6.0", forRemoval = true)
    public static Response withRedirect(String redirectTo) {
        return redirect(redirectTo);
    }

    /**
     * Creates a response object with a given url to redirect to
     *
     * @param redirectTo The URL to redirect to
     * @return The response object
     */
    public static Response redirect(String redirectTo) {
        Objects.requireNonNull(redirectTo, NotNull.REDIRECT_TO);

        return new Response(redirectTo);
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getContentType() {
        return contentType;
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
     * Sets a specific template to use for the response
     * @deprecated As of release 8.6.0, replaced by {@link #template(String)}
     *
     * @param template The path to the template (e.g. /mytemplate/template.ftl)
     * @return The response object
     */
    @Deprecated(since = "8.6.0", forRemoval = true)
    public Response andTemplate(String template) {
        Objects.requireNonNull(template, NotNull.TEMPLATE);
        this.template = template;

        return this;
    }

    /**
     * Sets a specific template to use for the response
     *
     * @param template The path to the template (e.g. /path-to-template/template.ftl)
     * @return The response object
     */
    public Response template(String template) {
        Objects.requireNonNull(template, NotNull.TEMPLATE);
        this.template = template;

        return this;
    }

    /**
     * Sets a specific content type to use for the response. Default is "text/html"
     * @deprecated As of release 8.6.0, replaced by {@link #contentType(String)}
     *
     * @param contentType The content type to use
     * @return The response object
     */
    @Deprecated(since = "8.6.0", forRemoval = true)
    public Response andContentType(String contentType) {
        Objects.requireNonNull(contentType, NotNull.CONTENT_TYPE);
        
        headers.put(Header.CONTENT_TYPE, contentType);
        this.contentType = contentType;

        return this;
    }

    /**
     * Sets a specific content type to use for the response. Default is "text/html"
     *
     * @param contentType The content type to use
     * @return The response object
     */
    public Response contentType(String contentType) {
        Objects.requireNonNull(contentType, NotNull.CONTENT_TYPE);

        headers.put(Header.CONTENT_TYPE, contentType);
        this.contentType = contentType;

        return this;
    }

    /**
     * Adds a value to the template that can be accessed using ${name} in the template
     * @deprecated As of release 8.6.0, replaced by {@link #render(String, Object)}
     *
     * @param name The name of the value
     * @param object The actual value
     * @return The response object
     */
    @Deprecated(since = "8.6.0", forRemoval = true)
    public Response andContent(String name, Object object) {
        Objects.requireNonNull(name, NotNull.NAME);
        content.put(name, object);

        return this;
    }

    /**
     * Adds a value to the template that can be accessed using ${name} in the template
     *
     * @param name The name of the value
     * @param object The actual value
     * @return The response object
     */
    public Response render(String name, Object object) {
        Objects.requireNonNull(name, NotNull.NAME);
        content.put(name, object);

        return this;
    }

    /**
     * Sets the body of the response. If a body is added, no template rendering will be
     * performed. The default content type "text/html" will be used.
     * @deprecated As of release 8.6.0, replaced by {@link #bodyHtml(String)}
     *
     * @param html The html for the body
     * @return The response object
     */
    @Deprecated(since = "8.6.0", forRemoval = true)
    public Response andHtmlBody(String html) {
        this.body = html;
        rendered = false;

        return this;
    }

    /**
     * Sets the body of the response. If a body is added, no template rendering will be
     * performed. The default content type "text/html" will be used.
     *
     * @param html The html for the body
     * @return The response object
     */
    public Response bodyHtml(String html) {
        this.body = html;
        rendered = false;

        ok();
        unauthorized();
        forbidden();
        internalServerError();
        badRequest();

        return this;
    }

    public Response bodyDefault() {
        switch (statusCode) {
            case StatusCodes.OK:
                this.body = Template.ok();
                break;
            case StatusCodes.UNAUTHORIZED:
                this.body = Template.unauthorized();
                break;
            case StatusCodes.NOT_FOUND:
                this.body = Template.notFound();
                break;
            case StatusCodes.FORBIDDEN:
                this.body = Template.forbidden();
                break;
            case StatusCodes.INTERNAL_SERVER_ERROR:
                this.body = Template.internalServerError();
                break;
            case StatusCodes.BAD_REQUEST:
                this.body = Template.badRequest();
                break;
            default:
                this.body = Template.xxx().replaceAll("###xxx###", String.valueOf(statusCode));
        }
        rendered = false;

        return this;
    }

    /**
     * Adds a Cookie to the response which is passed to the client
     * @deprecated As of release 8.6.0, replaced by {@link #cookie(Cookie)}
     *
     * @param cookie The cookie to add
     * @return The response object
     */
    @Deprecated(since = "8.6.0", forRemoval = true)
    public Response andCookie(Cookie cookie) {
        Objects.requireNonNull(cookie, NotNull.COOKIE);
        cookies.add(cookie);

        return this;
    }

    /**
     * Adds a Cookie to the response which is passed to the client
     *
     * @param cookie The cookie to add
     * @return The response object
     */
    public Response cookie(Cookie cookie) {
        Objects.requireNonNull(cookie, NotNull.COOKIE);
        cookies.add(cookie);

        return this;
    }

    /**
     * Converts a given Object to JSON and passing it to the response. If an object is given, no
     * template rendering will be performed and the content type for the response will be set to
     * "application/json"
     * @deprecated As of release 8.6.0, replaced by {@link #bodyJson(Object)}
     *
     * @param jsonObject The object to convert to JSON
     * @return The response object
     */
    @Deprecated(since = "8.6.0", forRemoval = true)
    public Response andJsonBody(Object jsonObject) {
        Objects.requireNonNull(jsonObject, NotNull.JSON_OBJECT);

        this.body = JsonUtils.toJson(jsonObject);
        contentType = MediaType.JSON_UTF_8.withoutParameters().toString();
        rendered = false;

        return this;
    }

    /**
     * Converts a given Object to JSON and passing it to the response. If an object is given, no
     * template rendering will be performed and the content type for the response will be set to
     * "application/json"
     *
     * @param object The object to convert to JSON
     * @return The response object
     */
    public Response bodyJson(Object object) {
        Objects.requireNonNull(object, NotNull.OBJECT);

        this.body = JsonUtils.toJson(object);
        contentType = MediaType.JSON_UTF_8.withoutParameters().toString();
        rendered = false;

        return this;
    }

    /**
     * Sets a JSON error string as body
     * @param message The error message to return
     * @return The response object
     */
    public Response bodyJsonError(String message) {
        Objects.requireNonNull(message, NotNull.MESSAGE);

        this.body = JsonUtils.toJson(Error.of(message, statusCode));
        contentType = MediaType.JSON_UTF_8.withoutParameters().toString();
        rendered = false;

        return this;
    }
    
    /**
     * Sets a given JSON string as body. If a String is given, no template rendering will be
     * performed and the content type for the response will be set to "application/json"
     * @deprecated As of release 8.6.0, replaced by {@link #bodyJson(String)}
     *
     * @param json The String to set as JSON
     * @return The response object
     */
    @Deprecated(since = "8.6.0", forRemoval = true)
    public Response andJsonBody(String json) {
        Objects.requireNonNull(json, NotNull.JSON);

        this.body = json;
        contentType = MediaType.JSON_UTF_8.withoutParameters().toString();
        rendered = false;

        return this;
    }

    /**
     * Sets a given JSON string as body. If a String is given, no template rendering will be
     * performed and the content type for the response will be set to "application/json"
     *
     * @param json The String to set as JSON
     * @return The response object
     */
    public Response bodyJson(String json) {
        Objects.requireNonNull(json, NotNull.JSON);

        this.body = json;
        contentType = MediaType.JSON_UTF_8.withoutParameters().toString();
        rendered = false;

        return this;
    }

    /**
     * Sends a binary file to the client skipping rendering
     * @deprecated As of release 8.6.0 with not replacement
     *
     * @param file The file to send
     * @return The response object
     */
    @Deprecated(since = "8.6.0", forRemoval = true)
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
     * @deprecated As of release 8.6.0 without replacement
     *
     * @param content The content to send
     * @return The response object
     */
    @Deprecated(since = "8.6.0", forRemoval = true)
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
     * @deprecated As of release 8.6.0, replaced by {@link #bodyText(String)}
     *
     * @param body The text for the response
     *
     * @return The response object
     */
    @Deprecated(since = "8.6.0", forRemoval = true)
    public Response andTextBody(String body) {
        this.body = body;
        contentType = MediaType.PLAIN_TEXT_UTF_8.withoutParameters().toString();
        rendered = false;

        return this;
    }

    /**
     * Sets the body of the response. If a body is added, no template rendering will be
     * performed. The content type "text/plain" will be used.
     *
     * @param text The text for the body
     *
     * @return The response object
     */
    public Response bodyText(String text) {
        this.body = text;
        contentType = MediaType.PLAIN_TEXT_UTF_8.withoutParameters().toString();
        rendered = false;

        return this;
    }

    /**
     * Disables template rendering, sending an empty body with content-type
     * text/plain in the response
     * @deprecated As of release 8.6.0, replaced by {@link #bodyEmpty()}
     *
     * @return The response object
     */
    @Deprecated(since = "8.6.0", forRemoval = true)
    public Response andEmptyBody() {
        contentType = MediaType.PLAIN_TEXT_UTF_8.withoutParameters().toString();
        rendered = false;

        return this;
    }

    /**
     * Disables template rendering, sending an empty body with content-type
     * text/plain in the response
     *
     * @return The response object
     */
    public Response bodyEmpty() {
        contentType = MediaType.PLAIN_TEXT_UTF_8.withoutParameters().toString();
        rendered = false;

        return this;
    }

    /**
     * Adds a header to the request response. If a header
     * key already exists, it will be overwritten with the latest value.
     * @deprecated As of release 8.6.0, replaced by {@link #header(String, String)}
     *
     * @param key The header key
     * @param value The header value
     *
     * @return The response object
     */
    @Deprecated(since = "8.6.0", forRemoval = true)
    public Response andHeader(String key, String value) {
        Objects.requireNonNull(key, NotNull.KEY);
        headers.put(new HttpString(key), value);

        return this;
    }

    /**
     * Adds a header to the request response. If a header
     * key already exists, it will be overwritten with the latest value.
     *
     * @param key The header key
     * @param value The header value
     *
     * @return The response object
     */
    public Response header(String key, String value) {
        Objects.requireNonNull(key, NotNull.KEY);
        headers.put(new HttpString(key), value);

        return this;
    }

    /**
     * Adds a content map to the content rendered in the template.
     * Already existing values with the same key are overwritten.
     * @deprecated As of release 8.6.0, replaced by {@link #render(Map)}
     *
     * @param content The content map to add
     * @return The response object
     */
    @Deprecated(since = "8.6.0", forRemoval = true)
    public Response andContent(Map<String, Object> content) {
        Objects.requireNonNull(content, NotNull.CONTENT);
        this.content.putAll(content);

        return this;
    }

    /**
     * Adds a content map to the content rendered in the template.
     * Already existing values with the same key are overwritten.
     *
     * @param content The content map to add
     * @return The response object
     */
    public Response render(Map<String, Object> content) {
        Objects.requireNonNull(content, NotNull.CONTENT);
        this.content.putAll(content);

        return this;
    }

    /**
     * Adds a header map to the response.
     * Already existing values with the same key are overwritten.
     * @deprecated As of release 8.6.0, replaced by {@link #headers(Map)}
     *
     * @param headers The headers map to add
     * @return The response object
     */
    @Deprecated(since = "8.6.0", forRemoval = true)
    public Response andHeaders(Map<HttpString, String> headers) {
        Objects.requireNonNull(headers, NotNull.HEADERS);
        this.headers.putAll(headers);

        return this;
    }

    /**
     * Adds a header map to the response.
     * Already existing values with the same key are overwritten.
     *
     * @param headers The headers map to add
     * @return The response object
     */
    public Response headers(Map<HttpString, String> headers) {
        Objects.requireNonNull(headers, NotNull.HEADERS);
        this.headers.putAll(headers);

        return this;
    }
    
    /**
     * Disposes a cookie by setting the expired date of the give cookie name
     * to a date in the past, max age to -1 and an empty value
     * @deprecated As of release 8.6.0, replaced by {@link #disposeCookie(String)}
     * 
     * @param cookieName The name of the cookie to dispose
     * @return The response object
     */
    @Deprecated(since = "8.6.0", forRemoval = true)
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
     * Disposes a cookie by setting the expired date of the give cookie name
     * to a date in the past, max age to -1 and an empty value
     *
     * @param cookieName The name of the cookie to dispose
     * @return The response object
     */
    public Response disposeCookie(String cookieName) {
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
     * @deprecated As of release 8.6.0, replaced by {@link #end()}
     *
     * @return The response object
     */
    @Deprecated(since = "8.6.0", forRemoval = true)
    public Response andEndResponse() {
        endResponse = true;

        return this;
    }

    /**
     * Tells a filter that the response ends and that the request handler
     * should not execute further filters by sending the current response
     * to the client. This is only used within a filter.
     *
     * @return The response object
     */
    public Response end() {
        endResponse = true;

        return this;
    }
}