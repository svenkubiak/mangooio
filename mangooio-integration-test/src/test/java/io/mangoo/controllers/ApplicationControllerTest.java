package io.mangoo.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import io.mangoo.configuration.Config;
import io.mangoo.core.Application;
import io.mangoo.enums.Default;
import io.mangoo.enums.Key;
import io.mangoo.test.MangooRequest;
import io.mangoo.test.MangooResponse;
import io.undertow.util.FileUtils;
import io.undertow.util.StatusCodes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

/**
 * 
 * @author svenkubiak
 *
 */
public class ApplicationControllerTest {
    
    @Test
    public void indexTest() {
        MangooResponse response = MangooRequest.get("/").execute();

        assertNotNull(response);
        assertEquals("text/html; charset=UTF-8", response.getContentType());
        assertEquals(StatusCodes.OK, response.getStatusCode());
    }
    
    @Test
    public void indexTestWithContent() {
        MangooResponse response = MangooRequest.get("/").execute();
        
        assertNotNull(response);
        assertEquals("This is a test!", response.getContent());
    }
    
    @Test
    public void redirectTestWithoutRedirect() {
        MangooResponse response = MangooRequest.get("/redirect").disableRedirects(true).execute();

        assertNotNull(response);
        assertEquals(StatusCodes.FOUND, response.getStatusCode());
    }
    
    @Test
    public void redirectTestWithRedirect() {
        MangooResponse response = MangooRequest.get("/redirect").execute();
        
        assertNotNull(response);
        assertEquals(StatusCodes.OK, response.getStatusCode());
    }
    
    @Test
    public void textTest() {
        MangooResponse response = MangooRequest.get("/text").execute();

        assertNotNull(response);
        assertEquals("text/plain; charset=UTF-8", response.getContentType());
        assertEquals(StatusCodes.OK, response.getStatusCode());
    }
    
    @Test
    public void notFoundTest() {
        MangooResponse response = MangooRequest.get("/foo").execute();

        assertNotNull(response);
        assertEquals("text/html; charset=UTF-8", response.getContentType());
        assertEquals(StatusCodes.NOT_FOUND, response.getStatusCode());
    }
    
    @Test
    public void forbiddenTest() {
        MangooResponse response = MangooRequest.get("/forbidden").execute();

        assertNotNull(response);
        assertEquals("text/html; charset=UTF-8", response.getContentType());
        assertEquals(StatusCodes.FORBIDDEN, response.getStatusCode());
    }
    
    @Test
    public void badRequestTest() throws InterruptedException {
        MangooResponse response = MangooRequest.get("/badrequest").execute();
        
        assertNotNull(response);
        assertEquals("text/html; charset=UTF-8", response.getContentType());
        assertEquals(StatusCodes.BAD_REQUEST, response.getStatusCode());
    }
    
    @Test
    public void unauthorizedTest() {
        MangooResponse response = MangooRequest.get("/unauthorized").execute();

        assertNotNull(response);
        assertEquals("text/html; charset=UTF-8", response.getContentType());
        assertEquals(StatusCodes.UNAUTHORIZED, response.getStatusCode());
    }
    
    @Test
    public void headerTest() {
    	 MangooResponse response = MangooRequest.get("/header").execute();

         assertNotNull(response);
         assertEquals("Access-Control-Allow-Origin: https://mangoo.io", response.getHttpResponse().getFirstHeader("Access-Control-Allow-Origin").toString());
    }
    
    @Test
    public void binaryTest() throws ClientProtocolException, IOException {
    	Config config = Application.getInjector().getInstance(Config.class);
        String host = config.getString(Key.APPLICATION_HOST, Default.APPLICATION_HOST.toString());
        int port = config.getInt(Key.APPLICATION_PORT, Default.APPLICATION_PORT.toInt());  
        
    	File file = new File(UUID.randomUUID().toString());
    	FileOutputStream fileOutputStream = new FileOutputStream(file);
    	CloseableHttpClient httpclient = HttpClients.custom().build();
    	try {
    	    HttpGet httpget = new HttpGet("http://" + host + ":" + port + "/binary");
    	    CloseableHttpResponse response = httpclient.execute(httpget);
   	        try {
   	            fileOutputStream.write(EntityUtils.toByteArray(response.getEntity()));
   	            fileOutputStream.close();
   	        } finally {
   	            response.close();
   	        }
   	    } finally {
   	        httpclient.close();
   	    }
    	
    	assertEquals("This is an attachment", FileUtils.readFile(file));
    	assertTrue(file.delete());
	}
}