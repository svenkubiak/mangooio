package io.mangoo.routing;

import com.google.common.base.Preconditions;
import com.google.common.net.MediaType;
import io.mangoo.constants.Header;
import io.mangoo.constants.NotNull;
import io.mangoo.constants.Template;
import io.mangoo.models.Error;
import io.mangoo.utils.JsonUtils;
import io.undertow.server.handlers.Cookie;
import io.undertow.server.handlers.CookieImpl;
import io.undertow.util.HttpString;
import io.undertow.util.StatusCodes;
import org.apache.logging.log4j.util.Strings;

import java.util.*;

public class Response {
    private static final String VALID_HTTP = "Valid HTTP status codes are between 100 and 599 inclusive";
    private final Map<HttpString, String> headers = new HashMap<>();
    private final Map<String, Object> content = new HashMap<>();
    private final List<Cookie> cookies = new ArrayList<>();
    private String redirectTo;
    private String contentType = MediaType.PLAIN_TEXT_UTF_8.withoutParameters().toString();
    private String body = Strings.EMPTY;
    private String template;
    private byte[] binaryBody;
    private boolean endResponse;
    private boolean rendered;
    private boolean redirect;
    private boolean binary;
    private int statusCode = StatusCodes.OK;

    public Response() {
        //Empty constructor for Google Guice
    }

    private Response(int statusCode) {
        Preconditions.checkArgument(statusCode >= 100 && statusCode <= 599, VALID_HTTP);
        this.statusCode = statusCode;
    }

    private Response(int statusCode, String contentType) {
        Preconditions.checkArgument(statusCode >= 100 && statusCode <= 599, VALID_HTTP);
        this.statusCode = statusCode;
        this.contentType = Objects.requireNonNull(contentType, NotNull.CONTENT_TYPE);
    }

    private Response(String redirectTo) {
        Objects.requireNonNull(redirectTo, NotNull.REDIRECT_TO);
        
        this.redirect = true;
        this.redirectTo = redirectTo;
    }

    /**
     * Creates a response object with HTTP status code 200
     * with default Content-Type "text/plain; charset=UTF-8"
     *
     * @return The response object
     */
    public static Response ok() {
        return new Response(StatusCodes.OK);
    }

    /**
     * Creates a response object with HTTP status code 201
     * with default Content-Type "text/plain; charset=UTF-8"
     *
     * @return The response object
     */
    public static Response created() {
        return new Response(StatusCodes.CREATED);
    }

    /**
     * Creates a response object with HTTP status code 202
     * with default Content-Type "text/plain; charset=UTF-8"
     *
     * @return The response object
     */
    public static Response accepted() {
        return new Response(StatusCodes.ACCEPTED);
    }

    /**
     * Creates a response object with HTTP status code 404
     * with default Content-Type "text/plain; charset=UTF-8"
     *
     * @return The response object
     */
    public static Response notFound() {
        return new Response(StatusCodes.NOT_FOUND);
    }

    /**
     * Creates a response object with HTTP status code 401
     * with default Content-Type "text/plain; charset=UTF-8"
     *
     * @return The response object
     */
    public static Response unauthorized() {
        return new Response(StatusCodes.UNAUTHORIZED);
    }

    /**
     * Creates a response object with HTTP status code 403
     * with default Content-Type "text/plain; charset=UTF-8"
     *
     * @return The response object
     */
    public static Response forbidden() {
        return new Response(StatusCodes.FORBIDDEN);
    }

    /**
     * Creates a response object with HTTP status code 400
     * with default Content-Type "text/plain; charset=UTF-8"
     *
     * @return The response object
     */
    public static Response badRequest() {
        return new Response(StatusCodes.BAD_REQUEST);
    }

    /**
     * Creates a response object with HTTP status code 304
     * with default Content-Type "text/plain; charset=UTF-8"
     *
     * @return The response object
     */
    public static Response notModified() {
        return new Response(StatusCodes.NOT_MODIFIED);
    }

    /**
     * Creates a response object with HTTP status code 500
     * with default Content-Type "text/plain; charset=UTF-8"
     *
     * @return The response object
     */
    public static Response internalServerError() {
        return new Response(StatusCodes.INTERNAL_SERVER_ERROR);
    }

    /**
     * Creates a response object with a given HTTP status code
     * with default Content-Type "text/plain; charset=UTF-8"
     *
     * @param statusCode The status code to set
     * @return The response object
     */
    public static Response status(int statusCode) {
        Preconditions.checkArgument(statusCode >= 100 && statusCode <= 599, VALID_HTTP);
        return new Response(statusCode);
    }

    /**
     * Creates a response object with a given HTTP status code
     *
     * @param statusCode The status code to set
     * @param contentType The status code to set
     *
     * @return The response object
     */
    public static Response status(int statusCode, String contentType) {
        Preconditions.checkArgument(statusCode >= 100 && statusCode <= 599, VALID_HTTP);
        Objects.requireNonNull(contentType, NotNull.CONTENT_TYPE);

        return new Response(statusCode, contentType);
    }

    /**
     * Creates a response object with a given url to redirect to
     * with default Content-Type "text/plain"
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

    public byte[] getBinaryBody() {
        return binaryBody;
    }

    public List<Cookie> getCookies() {
        return new ArrayList<>(cookies);
    }

    public String getTemplate() {
        return template;
    }

    public Map<String, Object> getContent() {
        return content;
    }

    public boolean isRedirect() {
        return redirect;
    }

    public boolean isRendered() {
        return rendered;
    }

    public boolean isBinary() {
        return binary;
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
     *
     * @param template The path to the template (e.g. /path-to-template/template.ftl)
     * @return The response object
     */
    public Response template(String template) {
        Objects.requireNonNull(template, NotNull.TEMPLATE);
        this.template = template;
        rendered = true;

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
     *
     * @param name The name of the value
     * @param object The actual value
     * @return The response object
     */
    public Response render(String name, Object object) {
        Objects.requireNonNull(name, NotNull.NAME);
        content.put(name, object);
        rendered = true;
        this.contentType = MediaType.HTML_UTF_8.withoutParameters().toString();

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
        this.contentType = MediaType.HTML_UTF_8.withoutParameters().toString();
        rendered = false;
        this.body = html;

        return this;
    }

    /**
     * Sets the body of the response. If a body is added, it will be sent
     * as a binary byte array. No rendering will be performed.
     * Content-Type will be automatically detected if not set.
     *
     * @param data The html for the body
     * @return The response object
     */
    public Response bodyBinary(byte[] data) {
        this.binaryBody = Objects.requireNonNull(data, NotNull.DATA);
        rendered = false;
        binary = true;

        return this;
    }

    public Response bodyDefault() {
        this.contentType = MediaType.HTML_UTF_8.withoutParameters().toString();
        rendered = false;
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
                this.body = Template.xxx().replace("###xxx###", String.valueOf(statusCode));
        }

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
     *
     * @param content The content map to add
     * @return The response object
     */
    public Response render(Map<String, Object> content) {
        this.contentType = MediaType.HTML_UTF_8.withoutParameters().toString();
        Objects.requireNonNull(content, NotNull.CONTENT);
        this.content.putAll(content);
        rendered = true;

        return this;
    }

    /**
     * Sets that this response is rendered by a freemarker template
     *
     * @return The response object
     */
    public Response render() {
        this.contentType = MediaType.HTML_UTF_8.withoutParameters().toString();
        rendered = true;

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
     *
     * @return The response object
     */
    public Response end() {
        endResponse = true;

        return this;
    }
}