package mangoo.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import mangoo.io.enums.ContentType;
import mangoo.io.test.MangooRequest;
import mangoo.io.test.MangooResponse;

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
    
    @Test
    public void invalidFormTest() {
        List<NameValuePair> parameter = new ArrayList<NameValuePair>();
        parameter.add(new BasicNameValuePair("phone", "1234567890123"));
        
        MangooResponse response = MangooRequest.post("/validateform").contentType(ContentType.APPLICATION_X_WWW_FORM_URLENCODED).postParameters(parameter).execute();
        assertNotNull(response.getContent());
        
        String [] lines = response.getContent().split(System.getProperty("line.separator"));
        assertEquals("name is required", lines[0]);
        assertEquals("email must be a valid eMail address", lines[1]);
        assertEquals("email2 must match email2confirm", lines[2]);
        assertEquals("password must exactly match passwordconfirm", lines[3]);
        assertEquals("ipv4 must be a valid IPv4 address", lines[4]);
        assertEquals("ipv6 must be a valid IPv6 address", lines[5]);
        assertEquals("phone can be max 12 characters", lines[6]);
        assertEquals("fax must be at least 11 characters", lines[7]);
    }
    
    @Test
    public void validFormTest() {
        List<NameValuePair> parameter = new ArrayList<NameValuePair>();
        parameter.add(new BasicNameValuePair("name", "this is my name"));
        parameter.add(new BasicNameValuePair("email", "foo@bar.com"));
        parameter.add(new BasicNameValuePair("email2", "game@thrones.com"));
        parameter.add(new BasicNameValuePair("email2confirm", "game@thrones.com"));
        parameter.add(new BasicNameValuePair("password", "Secret"));
        parameter.add(new BasicNameValuePair("passwordconfirm", "Secret"));
        parameter.add(new BasicNameValuePair("ipv4", "11.12.23.42"));
        parameter.add(new BasicNameValuePair("ipv6", "2001:db8:85a3:8d3:1319:8a2e:370:7348"));
        parameter.add(new BasicNameValuePair("phone", "abcdef"));
        parameter.add(new BasicNameValuePair("fax", "abchdjskcjsa"));
        
        MangooResponse response = MangooRequest.post("/validateform").contentType(ContentType.APPLICATION_X_WWW_FORM_URLENCODED).postParameters(parameter).execute();
        assertNotNull(response.getContent());
        assertEquals("Fancy that!", response.getContent());
    }
}