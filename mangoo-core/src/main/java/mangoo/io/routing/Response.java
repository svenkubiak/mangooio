package mangoo.io.routing;

import io.undertow.util.StatusCodes;

import java.util.HashMap;
import java.util.Map;

import mangoo.io.enums.ContentType;

import org.apache.commons.lang3.StringUtils;
import org.boon.json.JsonFactory;

import com.google.common.base.Charsets;

/**
 *
 * @author svenkubiak
 *
 */
public final class Response {
    private Map<String, Object> content = new HashMap<String, Object>();
    private String redirectTo;
    private String contentType = ContentType.TEXT_HTML.toString();
    private String charset = Charsets.UTF_8.name();
    private String body = "";
    private String template;
    private boolean rendered;
    private boolean redirect;
    private int statusCode;

    public static Response withOk() {
        return new Response(StatusCodes.OK);
    }

    public static Response withCreated() {
        return new Response(StatusCodes.CREATED);
    }

    public static Response withNotFound() {
        return new Response(StatusCodes.NOT_FOUND);
    }

    public static Response withForbidden() {
        return new Response(StatusCodes.FORBIDDEN);
    }

    public static Response withUnauthorized() {
        return new Response(StatusCodes.UNAUTHORIZED);
    }

    public static Response withBadRequest() {
        return new Response(StatusCodes.BAD_REQUEST);
    }

    public static Response withStatusCode(int statusCode) {
        return new Response(statusCode);
    }

    public static Response withRedirect(String redirectTo) {
        return new Response(redirectTo);
    }

    private Response(int statusCode) {
        this.statusCode = statusCode;
    }

    private Response(String redirectTo) {
        this.redirect = true;
        this.rendered = true;
        this.redirectTo = redirectTo;
    }

    public Response andTemplate(String template) {
        if (StringUtils.isBlank(this.template)) {
            this.template = template;
        }
        return this;
    }

    public Response andContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    public Response andCharset(String charset) {
        this.charset = charset;
        return this;
    }

    public Response andContent(String name, Object object) {
        this.content.put(name, object);
        return this;
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

    public String getTemplate() {
        return this.template;
    }

    public Map<String, Object> getContent() {
        return this.content;
    }

    public boolean isRedirect() {
        return this.redirect;
    }

    public boolean isRendered() {
        return this.rendered;
    }

    public String getRedirectTo() {
        return this.redirectTo;
    }

    public Response andBody(String body) {
        this.body = body;
        this.rendered = true;

        return this;
    }

    public Response andJsonBody(Object jsonObject) {
        this.contentType = ContentType.APPLICATION_JSON.toString();
        this.body = JsonFactory.create().toJson(jsonObject);
        this.rendered = true;

        return this;
    }

    public Response andTextBody(String text) {
        this.contentType = ContentType.TEXT_PLAIN.toString();
        this.body = text;
        this.rendered = true;

        return this;
    }

    public Response andEmptyBody() {
        this.rendered = true;

        return this;
    }
}