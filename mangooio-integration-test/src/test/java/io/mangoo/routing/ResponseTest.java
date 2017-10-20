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
import java.util.Arrays;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import com.google.common.net.MediaType;

import io.undertow.server.handlers.Cookie;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;

/**
 * 
 * @author svenkubiak
 *
 */
public class ResponseTest {
    
    @Test
    public void testAndConent() {
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
    public void testWithOk() {
        //given
        Response response = Response.withOk();
        
        //then
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
    }
    
    @Test
    public void testWithNotFound() {
        //given
        Response response = Response.withNotFound();
        
        //then
        assertThat(response.getStatusCode(), equalTo(StatusCodes.NOT_FOUND));
    }
    
    @Test
    public void testWithForbidden() {
        //given
        Response response = Response.withForbidden();
       
        //then
        assertThat(response.getStatusCode(), equalTo(StatusCodes.FORBIDDEN));
    }
    
    @Test
    public void testWithUnauthorized() {
        //given
        Response response = Response.withUnauthorized();
        
        //then
        assertThat(response.getStatusCode(), equalTo(StatusCodes.UNAUTHORIZED));
    }
    
    @Test
    public void testWithBadRequest() {
        //given
        Response response = Response.withBadRequest();
        
        //then
        assertThat(response.getStatusCode(), equalTo(StatusCodes.BAD_REQUEST));
    }
    
    @Test
    public void testWithCreated() {
        //given
        Response response = Response.withCreated();
        
        //then
        assertThat(response.getStatusCode(), equalTo(StatusCodes.CREATED));
    }
    
    @Test
    public void testStatusCode() {
        //given
        Response response = Response.withStatusCode(305);
        
        //then
        assertThat(response.getStatusCode(), equalTo(305));
    }
    
    @Test
    public void testWithRedirect() {
        //given
        Response response = Response.withRedirect("/foo");
        
        //then
        assertThat(response.getStatusCode(), equalTo(200));
        assertThat(response.isRendered(), equalTo(true));
        assertThat(response.isRedirect(), equalTo(true));
        assertThat(response.getRedirectTo(), equalTo("/foo"));
    }
    
    @Test
    public void testAndTemplate() {
        //given
        Response response = Response.withOk();
        
        //when
        response.andTemplate("mytemplate/foo.ftl");
        
        //then
        assertThat(response.getTemplate(), equalTo("mytemplate/foo.ftl"));
    }
    
    @Test
    public void testAndContentTypes() {
        //given
        Response response = Response.withOk();
        
        //when
        response.andContentType("application/json");
        
        //then
        assertThat(response.getContentType(), equalTo("application/json"));
    }
    
    @Test
    public void testAndCharset() {
        //given
        Response response = Response.withOk();
        
        //when
        response.andCharset("UTF-8");
        
        //then
        assertThat(response.getCharset(), equalTo("UTF-8"));
    }
    
    @Test
    public void testAndBody() {
        //given
        Response response = Response.withOk();
        
        //when
        response.andCharset("This is a Body! Remember: Winter is coming!");
        
        //then
        assertThat(response.getCharset(), equalTo("This is a Body! Remember: Winter is coming!"));
    }
    
    @Test
    public void testAndCookie() {
        //given
        Response response = Response.withOk();
        Cookie cookie = mock(Cookie.class);
        
        //when
        response.andCookie(cookie);
        
        //then
        assertThat(response.getCookies().get(0), equalTo(cookie));
    }
    
    @Test
    public void testAndJsonBody() {
        //given
        Response response = Response.withOk();
        
        //when
        response.andJsonBody(Arrays.asList("foo", "bar"));
        
        //then
        assertThat(response.getContentType(), equalTo(MediaType.JSON_UTF_8.withoutParameters().toString()));
        assertThat(response.isRendered(), equalTo(true));
        assertThat(response.getBody(), equalTo("[\"foo\",\"bar\"]"));
    }
    
    @Test
    public void testAndBinaryFile() throws FileNotFoundException, IOException {
        //given
        Response response = Response.withOk();
        File file = new File(UUID.randomUUID().toString());
        file.createNewFile();
        FileInputStream fileInpuStream = new FileInputStream(file);
        
        //when
        response.andBinaryFile(file);
        
        //then
        assertThat(response.getBinaryFileName(), equalTo(file.getName()));
        assertThat(response.isBinary(), equalTo(true));
        assertThat(response.isRendered(), equalTo(true));
        assertThat(response.getBinaryContent(), equalTo(IOUtils.toByteArray(fileInpuStream)));
        fileInpuStream.close();
        assertThat(file.delete(), equalTo(true));
    }
    
    @Test
    public void testAndBinaryConent() throws IOException {
        //given
        Response response = Response.withOk();
        File file = new File(UUID.randomUUID().toString());
        file.createNewFile();
        FileInputStream fileInputStream = new FileInputStream(file);
        
        //when
        response.andBinaryContent(IOUtils.toByteArray(fileInputStream));
        
        //then
        assertThat(response.isBinary(), equalTo(true));
        assertThat(response.isRendered(), equalTo(true));
        assertThat(response.getBinaryContent(), equalTo(IOUtils.toByteArray(fileInputStream)));
        fileInputStream.close();
        assertThat(file.delete(), equalTo(true));
    }
    
    @Test
    public void testAndTextBody() throws IOException {
        //given
        Response response = Response.withOk();
        
        //when
        response.andTextBody("This is a text body!");
        
        //then
        assertThat(response.isRendered(), equalTo(true));
        assertThat(response.getBody(), equalTo("This is a text body!"));
        assertThat(response.getContentType(), equalTo(MediaType.PLAIN_TEXT_UTF_8.withoutParameters().toString()));
    }
    
    @Test
    public void testAndEmptyBody() throws IOException {
        //given
        Response response = Response.withOk();
        
        //when
        response.andEmptyBody();
        
        //then
        assertThat(response.isRendered(), equalTo(true));
        assertThat(response.getBody(), equalTo(""));
        assertThat(response.getContentType(), equalTo(MediaType.PLAIN_TEXT_UTF_8.withoutParameters().toString()));
    }
    
    @Test
    public void testAndHeader() throws IOException {
        //given
        Response response = Response.withOk();
        
        //when
        response.andHeader(Headers.GZIP, "true");
        
        //then
        assertThat(response.getHeaders().get(Headers.GZIP), equalTo("true"));
    }
    
    @Test
    public void testAndEtag() throws IOException {
        //given
        Response response = Response.withOk();
        
        //when
        response.andEtag();
        
        //then
        assertThat(response.isETag(), equalTo(true));
    }
    
    @Test
    public void testAndEnd() throws IOException {
        //given
        Response response = Response.withOk();
        
        //when
        response.end();
        
        //then
        assertThat(response.isEndResponse(), equalTo(true));
    }
}