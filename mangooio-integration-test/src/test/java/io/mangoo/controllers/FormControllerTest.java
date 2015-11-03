package io.mangoo.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.junit.Test;

import io.mangoo.enums.ContentType;
import io.mangoo.test.MangooRequest;
import io.mangoo.test.MangooResponse;
import io.undertow.util.StatusCodes;

/**
 *
 * @author svenkubiak
 *
 */
public class FormControllerTest {

    @Test
    public void testFormPost() {
        //given
        List<NameValuePair> parameter = new ArrayList<NameValuePair>();
        parameter.add(new BasicNameValuePair("username", "vip"));
        parameter.add(new BasicNameValuePair("password", "secret"));

        //when
        MangooResponse response = MangooRequest.post("/form")
                .withContentType(ContentType.APPLICATION_X_WWW_FORM_URLENCODED)
                .withPostParameters(parameter)
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("vip;secret"));
    }
    
    @Test
    public void testFormEncoding() {
        //given
        List<NameValuePair> parameter = new ArrayList<NameValuePair>();
        parameter.add(new BasicNameValuePair("username", "süpöä"));
        parameter.add(new BasicNameValuePair("password", "#+ß§"));

        //when
        MangooResponse response = MangooRequest.post("/form")
                .withContentType(ContentType.APPLICATION_X_WWW_FORM_URLENCODED)
                .withPostParameters(parameter)
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("süpöä;#+ß§"));
    }

    @Test
    public void testInvalidFormValues() {
        //given
        List<NameValuePair> parameter = new ArrayList<NameValuePair>();
        parameter.add(new BasicNameValuePair("phone", "1234567890123"));
        parameter.add(new BasicNameValuePair("regex", "ABC"));

        //when
        MangooResponse response = MangooRequest.post("/validateform")
                .withContentType(ContentType.APPLICATION_X_WWW_FORM_URLENCODED)
                .withPostParameters(parameter)
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));

        String [] lines = response.getContent().split(System.getProperty("line.separator"));
        assertThat(lines[0], equalTo("name is required"));
        assertThat(lines[1], equalTo("email must be a valid eMail address"));
        assertThat(lines[2], equalTo("email2 must match email2confirm"));
        assertThat(lines[3], equalTo("password must exactly match passwordconfirm"));
        assertThat(lines[4], equalTo("ipv4 must be a valid IPv4 address"));
        assertThat(lines[5], equalTo("ipv6 must be a valid IPv6 address"));
        assertThat(lines[6], equalTo("phone must have a size of max 12"));
        assertThat(lines[7], equalTo("fax must have a least a size of 11"));
        assertThat(lines[8], equalTo("regex is invalid"));
    }

    @Test
    public void testValidFormValues() {
        //given
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
        parameter.add(new BasicNameValuePair("regex", "a"));

        //when
        MangooResponse response = MangooRequest.post("/validateform")
                .withContentType(ContentType.APPLICATION_X_WWW_FORM_URLENCODED)
                .withPostParameters(parameter)
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("Fancy that!"));
    }
}