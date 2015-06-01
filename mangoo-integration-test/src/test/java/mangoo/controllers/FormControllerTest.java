package mangoo.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import mangoo.io.enums.ContentType;
import mangoo.io.testing.MangooRequest;
import mangoo.io.testing.MangooResponse;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.junit.Test;

/**
 * 
 * @author svenkubiak
 *
 */
public class FormControllerTest {
    
    @Test
    public void postTest() {
        List<NameValuePair> parameter = new ArrayList<NameValuePair>();
        parameter.add(new BasicNameValuePair("username", "vip"));
        parameter.add(new BasicNameValuePair("password", "secret"));
        
        MangooResponse response = MangooRequest.post("/form").contentType(ContentType.APPLICATION_X_WWW_FORM_URLENCODED).postParameters(parameter).execute();
        assertNotNull(response.getContent());
        assertEquals("vip;secret", response.getContent());
    }
}