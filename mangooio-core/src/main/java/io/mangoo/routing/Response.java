package io.mangoo.routing;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.net.MediaType;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.mangoo.enums.Header;
import io.mangoo.enums.Required;
import io.mangoo.utils.JsonUtils;
import io.undertow.server.handlers.Cookie;
import io.undertow.util.HttpString;
import io.undertow.util.StatusCodes;

/**
 *
 * @author svenkubiak
 *
 */
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
    private boolean unrendered;
    private int statusCode = StatusCodes.OK;

    public Response() {
        //Empty constructor for Google Guice
    }

    private Response(int statusCode) {
        this.statusCode = statusCode;
        this.rendered = true;
    }

    private Response(String redirectTo) {
        Objects.requireNonNull(redirectTo, Required.REDIRECT_TO.toString());
        
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
    
    public boolean isUnrendered() {
        return unrendered;
    }

    public String getRedirectTo() {
        return redirectTo;
    }

    public Map<HttpString, String> getHeaders() {
        return headers;
    }
    
    public String getHeader(HttpString header) {
        Objects.requireNonNull(header, Required.HEADER.toString());
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
        Objects.requireNonNull(redirectTo, Required.REDIRECT_TO.toString());

        return new Response(redirectTo);
    }

    /**
     * Sets a specific template to use for the response
     *
     * @param template The path to the template (e.g. /mytemplate/template.ftl)
     * @return A response object {@link io.mangoo.routing.Response}
     */
    public Response andTemplate(String template) {
        Objects.requireNonNull(template, Required.TEMPLATE.toString());
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
        Objects.requireNonNull(contentType, Required.CONTENT_TYPE.toString());
        
        headers.put(Header.CONTENT_TYPE.toHttpString(), contentType);
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
        Objects.requireNonNull(charset, Required.CHARSET.toString());
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
        Objects.requireNonNull(name, Required.NAME.toString());
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
    public Response andBody(String body) {
        this.body = body;
        rendered = false;

        return this;
    }
    
    /**
     * Sets the content of a given file placed in the templates folder
     * in /templates/CONTROLLER_NAME/METHOD_NAME.body as body without rendering the
     * file in the template engine
     * 
     * @return A response object {@link io.mangoo.routing.Response}
     */
    public Response andUnrenderedBody() {
        rendered = false;
        unrendered = true;

        return this;
    }

    /**
     * Adds an additional Cookie to the response which is passed to the client
     *
     * @param cookie The cookie to add
     * @return A response object {@link io.mangoo.routing.Response}
     */
    public Response andCookie(Cookie cookie) {
        Objects.requireNonNull(cookie, Required.COOKIE.toString());
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
        Objects.requireNonNull(jsonObject, Required.JSON_OBJECT.toString());

        this.body = JsonUtils.toJson(jsonObject);
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
        Objects.requireNonNull(file, Required.FILE.toString());

        try (InputStream inputStream = Files.newInputStream(file)) {
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
     * @param content The content to to send
     * @return A response object {@link io.mangoo.routing.Response}
     */
    public Response andBinaryContent(byte [] content) {
        Objects.requireNonNull(content, Required.CONTENT.toString());

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
     * Disables template rendering, sending an empty body in the response
     *
     * @return A response object {@link io.mangoo.routing.Response}
     */
    public Response andEmptyBody() {
        contentType = MediaType.PLAIN_TEXT_UTF_8.withoutParameters().toString();
        rendered = false;

        return this;
    }

    /**
     * Adds an additional header to the request response. If an header
     * key already exists, it will we overwritten with the latest value.
     *
     * @param key The header constant from Headers class (e.g. Header.CONTENT_TYPE.toString())
     * @param value The header value
     *
     * @return A response object {@link io.mangoo.routing.Response}
     */
    public Response andHeader(HttpString key, String value) {
        Objects.requireNonNull(key, Required.KEY.toString());
        headers.put(key, value);

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
        Objects.requireNonNull(content, Required.CONTENT.toString());
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
        Objects.requireNonNull(headers, Required.HEADERS.toString());
        this.headers.putAll(headers);

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