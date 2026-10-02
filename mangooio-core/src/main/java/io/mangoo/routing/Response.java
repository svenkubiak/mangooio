package io.mangoo.routing;

import com.google.common.base.Preconditions;
import com.google.common.net.MediaType;
import io.mangoo.constants.Header;
import io.mangoo.constants.Required;
import io.mangoo.constants.Template;
import io.mangoo.models.Error;
import io.mangoo.utils.JsonUtils;
import io.undertow.server.handlers.Cookie;
import io.undertow.server.handlers.CookieImpl;
import io.undertow.util.HttpString;
import io.undertow.util.StatusCodes;
import org.apache.logging.log4j.util.Strings;

import java.nio.file.Path;
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
    private Path fileBody;
    private boolean endResponse;
    private boolean rendered;
    private boolean redirect;
    private boolean binary;
    private boolean file;
    private int statusCode = StatusCodes.OK;

    public Response() {
        // Empty constructor for Google Guice
    }

    private Response(int statusCode) {
        Preconditions.checkArgument(statusCode >= 100 && statusCode <= 599, VALID_HTTP);
        this.statusCode = statusCode;
    }

    private Response(int statusCode, String contentType) {
        Preconditions.checkArgument(statusCode >= 100 && statusCode <= 599, VALID_HTTP);
        this.statusCode = statusCode;
        this.contentType = Objects.requireNonNull(contentType, Required.CONTENT_TYPE);
    }

    private Response(String redirectTo) {
        Objects.requireNonNull(redirectTo, Required.REDIRECT_TO);
        
        this.redirect = true;
        this.redirectTo = redirectTo;
    }

    public static Response ok() {
        return new Response(StatusCodes.OK);
    }

    public static Response created() {
        return new Response(StatusCodes.CREATED);
    }

    public static Response accepted() {
        return new Response(StatusCodes.ACCEPTED);
    }

    public static Response notFound() {
        return new Response(StatusCodes.NOT_FOUND);
    }

    public static Response unauthorized() {
        return new Response(StatusCodes.UNAUTHORIZED);
    }

    public static Response forbidden() {
        return new Response(StatusCodes.FORBIDDEN);
    }

    public static Response badRequest() {
        return new Response(StatusCodes.BAD_REQUEST);
    }

    public static Response notModified() {
        return new Response(StatusCodes.NOT_MODIFIED);
    }

    public static Response internalServerError() {
        return new Response(StatusCodes.INTERNAL_SERVER_ERROR);
    }

    public static Response status(int statusCode) {
        Preconditions.checkArgument(statusCode >= 100 && statusCode <= 599, VALID_HTTP);
        return new Response(statusCode);
    }

    public static Response status(int statusCode, String contentType) {
        Preconditions.checkArgument(statusCode >= 100 && statusCode <= 599, VALID_HTTP);
        Objects.requireNonNull(contentType, Required.CONTENT_TYPE);

        return new Response(statusCode, contentType);
    }

    public static Response redirect(String redirectTo) {
        Objects.requireNonNull(redirectTo, Required.REDIRECT_TO);

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

    public Path getFileBody() {
        return fileBody;
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

    public boolean isFile() {
        return file;
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
        Objects.requireNonNull(header, Required.HEADER);
        return headers.get(header);
    }

    public Response template(String template) {
        Objects.requireNonNull(template, Required.TEMPLATE);
        this.template = template;
        rendered = true;

        return this;
    }

    public Response contentType(String contentType) {
        Objects.requireNonNull(contentType, Required.CONTENT_TYPE);

        headers.put(Header.CONTENT_TYPE, contentType);
        this.contentType = contentType;

        return this;
    }

    public Response render(String name, Object object) {
        Objects.requireNonNull(name, Required.NAME);
        content.put(name, object);
        rendered = true;
        this.contentType = MediaType.HTML_UTF_8.withoutParameters().toString();

        return this;
    }

    public Response bodyHtml(String html) {
        this.contentType = MediaType.HTML_UTF_8.withoutParameters().toString();
        rendered = false;
        this.body = html;

        return this;
    }

    /** Content-Type is detected automatically if not set. */
    public Response bodyBinary(byte[] data) {
        this.binaryBody = Objects.requireNonNull(data, Required.DATA);
        rendered = false;
        binary = true;

        return this;
    }

    /**
     * Streams the file to the client with constant memory usage; Content-Type is detected automatically if not set.
     * Existence and readability of the path are only checked when the response is sent.
     */
    public Response bodyFile(Path path) {
        this.fileBody = Objects.requireNonNull(path, Required.PATH);
        rendered = false;
        file = true;

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

    public Response cookie(Cookie cookie) {
        Objects.requireNonNull(cookie, Required.COOKIE);
        cookies.add(cookie);

        return this;
    }

    public Response bodyJson(Object object) {
        Objects.requireNonNull(object, Required.OBJECT);

        this.body = JsonUtils.toJson(object);
        contentType = MediaType.JSON_UTF_8.withoutParameters().toString();
        rendered = false;

        return this;
    }

    public Response bodyJsonError(String message) {
        Objects.requireNonNull(message, Required.MESSAGE);

        this.body = JsonUtils.toJson(Error.of(message, statusCode));
        contentType = MediaType.JSON_UTF_8.withoutParameters().toString();
        rendered = false;

        return this;
    }

    public Response bodyJson(String json) {
        Objects.requireNonNull(json, Required.JSON);

        this.body = json;
        contentType = MediaType.JSON_UTF_8.withoutParameters().toString();
        rendered = false;

        return this;
    }

    public Response bodyText(String text) {
        this.body = text;
        contentType = MediaType.PLAIN_TEXT_UTF_8.withoutParameters().toString();
        rendered = false;

        return this;
    }

    public Response header(String key, String value) {
        Objects.requireNonNull(key, Required.KEY);
        headers.put(new HttpString(key), value);

        return this;
    }

    public Response render(Map<String, Object> content) {
        this.contentType = MediaType.HTML_UTF_8.withoutParameters().toString();
        Objects.requireNonNull(content, Required.CONTENT);
        this.content.putAll(content);
        rendered = true;

        return this;
    }

    public Response render() {
        this.contentType = MediaType.HTML_UTF_8.withoutParameters().toString();
        rendered = true;

        return this;
    }

    public Response headers(Map<HttpString, String> headers) {
        Objects.requireNonNull(headers, Required.HEADERS);
        this.headers.putAll(headers);

        return this;
    }

    public Response disposeCookie(String cookieName, boolean secure) {
        Objects.requireNonNull(cookieName, Required.COOKIE);

        cookies.add(new CookieImpl(cookieName)
                .setPath("/")
                .setSecure(secure)
                .setValue("")
                .setMaxAge(-1)
                .setDiscard(true)
                .setExpires(new Date(1)));

        return this;
    }

    public Response disposeCookie(String cookieName) {
        Objects.requireNonNull(cookieName, Required.COOKIE);
        disposeCookie(cookieName, true);

        return this;
    }

    /** Only used within a filter: stops executing further filters and sends the current response to the client. */
    public Response end() {
        endResponse = true;

        return this;
    }
}