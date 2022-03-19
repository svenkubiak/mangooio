package io.mangoo.routing;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.google.common.net.MediaType;

import io.mangoo.TestExtension;
import io.undertow.server.handlers.Cookie;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;

/**
 * 
 * @author svenkubiak
 *
 */
@ExtendWith({TestExtension.class})
class ResponseTest {
    
    @Test
    void testAndConent() {
        //given
        Response response = new Response();
        
        //when
        response.andContent("foo", null);
        response.andContent("foo2", "bar");
        
        //then
        assertThat(response.getContent(), not(nullValue()));
        assertThat(response.getContent().get("foo"), equalTo(null));
        assertThat(response.getContent().get("foo2"), equalTo("bar"));
    }
    
    @Test
    void testWithOk() {
        //given
        Response response = Response.withOk();
        
        //then
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
    }
    
    @Test
    void testWithNotFound() {
        //given
        Response response = Response.withNotFound();
        
        //then
        assertThat(response.getStatusCode(), equalTo(StatusCodes.NOT_FOUND));
    }
    
    @Test
    void testWithForbidden() {
        //given
        Response response = Response.withForbidden();
       
        //then
        assertThat(response.getStatusCode(), equalTo(StatusCodes.FORBIDDEN));
    }
    
    @Test
    void testWithUnauthorized() {
        //given
        Response response = Response.withUnauthorized();
        
        //then
        assertThat(response.getStatusCode(), equalTo(StatusCodes.UNAUTHORIZED));
    }
    
    @Test
    void testWithBadRequest() {
        //given
        Response response = Response.withBadRequest();
        
        //then
        assertThat(response.getStatusCode(), equalTo(StatusCodes.BAD_REQUEST));
    }
    
    @Test
    void testWithCreated() {
        //given
        Response response = Response.withCreated();
        
        //then
        assertThat(response.getStatusCode(), equalTo(StatusCodes.CREATED));
    }
    
    @Test
    void testStatusCode() {
        //given
        Response response = Response.withStatusCode(305);
        
        //then
        assertThat(response.getStatusCode(), equalTo(305));
    }
    
    @Test
    void testWithRedirect() {
        //given
        Response response = Response.withRedirect("/foo");
        
        //then
        assertThat(response.getStatusCode(), equalTo(200));
        assertThat(response.isRendered(), equalTo(false));
        assertThat(response.isRedirect(), equalTo(true));
        assertThat(response.getRedirectTo(), equalTo("/foo"));
    }
    
    @Test
    void testAndTemplate() {
        //given
        Response response = Response.withOk();
        
        //when
        response.andTemplate("mytemplate/foo.ftl");
        
        //then
        assertThat(response.getTemplate(), equalTo("mytemplate/foo.ftl"));
    }
    
    @Test
    void testAndContentTypes() {
        //given
        Response response = Response.withOk();
        
        //when
        response.andContentType("application/json");
        
        //then
        assertThat(response.getContentType(), equalTo("application/json"));
    }
    
    @Test
    void testAndCharset() {
        //given
        Response response = Response.withOk();
        
        //when
        response.andCharset("UTF-8");
        
        //then
        assertThat(response.getCharset(), equalTo("UTF-8"));
    }
    
    @Test
    void testAndBody() {
        //given
        Response response = Response.withOk();
        
        //when
        response.andCharset("This is a Body! Remember: Winter is coming!");
        
        //then
        assertThat(response.getCharset(), equalTo("This is a Body! Remember: Winter is coming!"));
    }
    
    @Test
    void testAndCookie() {
        //given
        Response response = Response.withOk();
        Cookie cookie = mock(Cookie.class);
        
        //when
        response.andCookie(cookie);
        
        //then
        assertThat(response.getCookies().get(0), equalTo(cookie));
    }
    
    @Test
    void testAndJsonBody() {
        //given
        Response response = Response.withOk();
        
        //when
        response.andJsonBody(List.of("foo", "bar"));
        
        //then
        assertThat(response.getContentType(), equalTo(MediaType.JSON_UTF_8.withoutParameters().toString()));
        assertThat(response.isRendered(), equalTo(false));
        assertThat(response.getBody(), equalTo("[\"foo\",\"bar\"]"));
    }
    
    @Test
    void testAndBinaryFile() throws FileNotFoundException, IOException {
        //given
        Response response = Response.withOk();
        Path file = Paths.get(UUID.randomUUID().toString());
        Files.createFile(file);
        InputStream fileInpuStream = Files.newInputStream(file);
        
        //when
        response.andBinaryFile(file);
        
        //then
        assertThat(response.getBinaryFileName(), equalTo(file.getFileName().toString()));
        assertThat(response.isBinary(), equalTo(true));
        assertThat(response.isRendered(), equalTo(false));
        assertThat(response.getBinaryContent(), equalTo(IOUtils.toByteArray(fileInpuStream)));
        fileInpuStream.close();
        Files.delete(file);
    }
    
    @Test
    void testAndBinaryConent() throws IOException {
        //given
        Response response = Response.withOk();
        File file = new File(UUID.randomUUID().toString());
        file.createNewFile();
        FileInputStream fileInputStream = new FileInputStream(file);
        
        //when
        response.andBinaryContent(IOUtils.toByteArray(fileInputStream));
        
        //then
        assertThat(response.isBinary(), equalTo(true));
        assertThat(response.isRendered(), equalTo(false));
        assertThat(response.getBinaryContent(), equalTo(IOUtils.toByteArray(fileInputStream)));
        fileInputStream.close();
        assertThat(file.delete(), equalTo(true));
    }
    
    @Test
    void testAndTextBody() throws IOException {
        //given
        Response response = Response.withOk();
        
        //when
        response.andTextBody("This is a text body!");
        
        //then
        assertThat(response.isRendered(), equalTo(false));
        assertThat(response.getBody(), equalTo("This is a text body!"));
        assertThat(response.getContentType(), equalTo(MediaType.PLAIN_TEXT_UTF_8.withoutParameters().toString()));
    }
    
    @Test
    void testAndEmptyBody() throws IOException {
        //given
        Response response = Response.withOk();
        
        //when
        response.andEmptyBody();
        
        //then
        assertThat(response.isRendered(), equalTo(false));
        assertThat(response.getBody(), equalTo(""));
        assertThat(response.getContentType(), equalTo(MediaType.PLAIN_TEXT_UTF_8.withoutParameters().toString()));
    }
    
    @Test
    void testAndHeader() throws IOException {
        //given
        Response response = Response.withOk();
        
        //when
        response.andHeader("gzip", "true");
        
        //then
        assertThat(response.getHeaders().get(Headers.GZIP), equalTo("true"));
    }
    
    @Test
    void testAndEnd() throws IOException {
        //given
        Response response = Response.withOk();
        
        //when
        response.andEndResponse();
        
        //then
        assertThat(response.isEndResponse(), equalTo(true));
    }
    
    @Test
    void testAndUnrenderedText() {
        //given
        Response response = Response.withOk().andUnrenderedBody();
        
        //then
        assertThat(response.isRendered(), equalTo(false));
    }
    
    @Test
    void testAndUnrenderedHtml() {
        //given
        Response response = Response.withOk().andUnrenderedBody();
        
        //then
        assertThat(response.isRendered(), equalTo(false));
    }
}