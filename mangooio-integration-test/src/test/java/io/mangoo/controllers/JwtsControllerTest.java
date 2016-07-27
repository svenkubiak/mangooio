package io.mangoo.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.containsString;

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
    private static final String VALID_BEARER = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJKb2UifQ.uTpP-beHoL-yQ6Sq32LJBRnf3DUT2L5d2VOGMg6dReEfTSFTThHLHoFwslVOqXycrDHiNzkMGA1aMirJjkT2dw";
    private static final String INVALID_BEARER = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWV9.TJVA95OrM7E2cBab30RMHrHDcEfxjoYZgeFONFh7HgQ";
    
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