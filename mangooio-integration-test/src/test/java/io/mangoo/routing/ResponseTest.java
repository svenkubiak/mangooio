package io.mangoo.routing;

import com.google.common.net.MediaType;
import io.mangoo.TestExtension;
import io.undertow.server.handlers.Cookie;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;
import io.undertow.util.StatusCodes;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.mock;

/**
 * 
 * @author svenkubiak
 *
 */
@ExtendWith({TestExtension.class})
class ResponseTest {
    
    @Test
    void testAndContent() {
        //given
        Response response = new Response();
        
        //when
        response.render("foo", null);
        response.render("foo2", "bar");
        
        //then
        assertThat(response.getContent(), not(nullValue()));
        assertThat(response.getContent().get("foo"), equalTo(null));
        assertThat(response.getContent().get("foo2"), equalTo("bar"));
    }
    
    @Test
    void testWithOk() {
        //given
        Response response = Response.ok();
        
        //then
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
    }

    @Test
    void testWithInternalServerError() {
        //given
        Response response = Response.internalServerError();

        //then
        assertThat(response.getStatusCode(), equalTo(StatusCodes.INTERNAL_SERVER_ERROR));
    }

    @Test
    void testGetHeader() {
        //given
        Response response = Response.ok().header("foo", "bar");

        //then
        assertThat(response.getHeader(new HttpString("foo")), equalTo("bar"));
    }
    
    @Test
    void testWithNotFound() {
        //given
        Response response = Response.notFound();
        
        //then
        assertThat(response.getStatusCode(), equalTo(StatusCodes.NOT_FOUND));
    }
    
    @Test
    void testWithForbidden() {
        //given
        Response response = Response.forbidden();
       
        //then
        assertThat(response.getStatusCode(), equalTo(StatusCodes.FORBIDDEN));
    }
    
    @Test
    void testWithUnauthorized() {
        //given
        Response response = Response.unauthorized();
        
        //then
        assertThat(response.getStatusCode(), equalTo(StatusCodes.UNAUTHORIZED));
    }
    
    @Test
    void testWithBadRequest() {
        //given
        Response response = Response.badRequest();
        
        //then
        assertThat(response.getStatusCode(), equalTo(StatusCodes.BAD_REQUEST));
    }
    
    @Test
    void testWithCreated() {
        //given
        Response response = Response.created();
        
        //then
        assertThat(response.getStatusCode(), equalTo(StatusCodes.CREATED));
    }
    
    @Test
    void testStatusCode() {
        //given
        Response response = Response.status(305);
        
        //then
        assertThat(response.getStatusCode(), equalTo(305));
    }
    
    @Test
    void testWithRedirect() {
        //given
        Response response = Response.redirect("/foo");
        
        //then
        assertThat(response.getStatusCode(), equalTo(200));
        assertThat(response.isRendered(), equalTo(false));
        assertThat(response.isRedirect(), equalTo(true));
        assertThat(response.getRedirectTo(), equalTo("/foo"));
    }
    
    @Test
    void testAndTemplate() {
        //given
        Response response = Response.ok();
        
        //when
        response.template("mytemplate/foo.ftl");
        
        //then
        assertThat(response.getTemplate(), equalTo("mytemplate/foo.ftl"));
    }
    
    @Test
    void testAndContentTypes() {
        //given
        Response response = Response.ok();
        
        //when
        response.contentType("application/json");
        
        //then
        assertThat(response.getContentType(), equalTo("application/json"));
    }

    @Test
    void testAndHtmlBody() {
        //given
        Response response = Response.ok();
        
        //when
        response.bodyHtml("This is a Body! Remember: Winter is coming!");
        
        //then
        assertThat(response.getBody(), equalTo("This is a Body! Remember: Winter is coming!"));
    }
    
    @Test
    void testAndCookie() {
        //given
        Response response = Response.ok();
        Cookie cookie = mock(Cookie.class);
        
        //when
        response.cookie(cookie);
        
        //then
        assertThat(response.getCookies().getFirst(), equalTo(cookie));
    }
    
    @Test
    void testAndJsonBody() {
        //given
        Response response = Response.ok();
        
        //when
        response.bodyJson(List.of("foo", "bar"));
        
        //then
        assertThat(response.getContentType(), equalTo(MediaType.JSON_UTF_8.withoutParameters().toString()));
        assertThat(response.isRendered(), equalTo(false));
        assertThat(response.getBody(), equalTo("[\"foo\",\"bar\"]"));
    }
    
    @Test
    void testAndBinaryFile() throws FileNotFoundException, IOException {
        //given
        Response response = Response.ok();
        Path file = Paths.get(UUID.randomUUID().toString());
        Files.createFile(file);
        InputStream fileInputStream = Files.newInputStream(file);
        
        //when
        response.file(file);
        
        //then
        assertThat(response.getBinaryFileName(), equalTo(file.getFileName().toString()));
        assertThat(response.isBinary(), equalTo(true));
        assertThat(response.isRendered(), equalTo(false));
        assertThat(response.getBinaryContent(), equalTo(IOUtils.toByteArray(fileInputStream)));
        fileInputStream.close();
        Files.delete(file);
    }

    @Test
    void testAndTextBody() throws IOException {
        //given
        Response response = Response.ok();
        
        //when
        response.bodyText("This is a text body!");
        
        //then
        assertThat(response.isRendered(), equalTo(false));
        assertThat(response.getBody(), equalTo("This is a text body!"));
        assertThat(response.getContentType(), equalTo(MediaType.PLAIN_TEXT_UTF_8.withoutParameters().toString()));
    }
    
    @Test
    void testAndEmptyBody() throws IOException {
        //given
        Response response = Response.ok();
        
        //when
        response.bodyEmpty();
        
        //then
        assertThat(response.isRendered(), equalTo(false));
        assertThat(response.getBody(), equalTo(""));
        assertThat(response.getContentType(), equalTo(MediaType.PLAIN_TEXT_UTF_8.withoutParameters().toString()));
    }
    
    @Test
    void testAndHeader() throws IOException {
        //given
        Response response = Response.ok();
        
        //when
        response.header("gzip", "true");
        
        //then
        assertThat(response.getHeaders().get(Headers.GZIP), equalTo("true"));
    }
    
    @Test
    void testAndEnd() throws IOException {
        //given
        Response response = Response.ok();
        
        //when
        response.end();
        
        //then
        assertThat(response.isEndResponse(), equalTo(true));
    }
    
    @Test
    void testAndUnrenderedText() {
        //given
        Response response = Response.ok().bodyText("");
        
        //then
        assertThat(response.isRendered(), equalTo(false));
    }
    
    @Test
    void testAndUnrenderedHtml() {
        //given
        Response response = Response.ok().bodyHtml("");
        
        //then
        assertThat(response.isRendered(), equalTo(false));
    }
}