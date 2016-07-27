package io.mangoo.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import org.junit.Test;

import io.mangoo.test.utils.WebRequest;
import io.mangoo.test.utils.WebResponse;
import io.undertow.util.StatusCodes;

/**
 * 
 * @author svenkubiak
 *
 */
public class JwtsControllerTest {
    private static final String VALID_BEARER = "tVPJmD9ZgYJKt0p8yPGGbKFHaDekhcKiv70ISFsc2TYex7d2lbpzzgjIpU4sSd7KC+rJGGLshpcZj4wNTfadKJQ3YRZ2Tp4B3lXKduKc1J8uLjB4vI9xRKReqWkzjtVg4WnJQdYAuCc9SyFqrhtDa/7t8ksAu4bd9X6xxxgARWM=";
    private static final String INVALID_BEARER = "UDpJn6svraIVYklop7L3txHn+GGr9LoU5vdnmHVXcPSsddIYt+798EToaiclhO2Pnh8yA4Pp99nWltL1WQ9/Ty9sdJlGXd6HDHDJziMRrmlxMbleBD51MXIwtwHE27uK5grDiEh1NNULLiHG3ETQACQ1/FHNGSEfsPVavPmwrRSUtzgILuMUXrUKNYf+l/KYUnP5wIdwSGRZsR6oJHQkTg==";
    
    @Test
    public void testNoBearerToken() {
        //given
        WebResponse response = WebRequest.get("/jwts/validate")
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.BAD_REQUEST));
    }
    
    @Test
    public void testInvalidBearerToken() {
        //given
        WebResponse response = WebRequest.get("/jwts/validate")
                .withHeader("Authorization", "Bearer " + INVALID_BEARER)
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.UNAUTHORIZED));
    }
    
    @Test
    public void testValidBearerToken() {
        //given
        WebResponse response = WebRequest.get("/jwts/validate")
                .withHeader("Authorization", "Bearer " + VALID_BEARER)
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
    }
    
    @Test
    public void testBearerTokenInRequest() {
        //given
        WebResponse response = WebRequest.get("/jwts/retrieve")
                .withHeader("Authorization", "Bearer " + VALID_BEARER)
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), containsString("true"));
    }
}