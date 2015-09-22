package io.mangoo.models;

import java.util.Map;

import org.boon.json.JsonFactory;

/**
 *
 * @author svenkubiak
 *
 */
@SuppressWarnings("unchecked")
public class OAuthUser {
    private String oAuthResponse;
    private String username;

    public OAuthUser(String oAuthResponse) {
        this.oAuthResponse = oAuthResponse;
        Map<String, Object> json = JsonFactory.create().readValue(this.oAuthResponse, Map.class);
        this.username = (String) json.get("screen_name");
    }

    public String getOAuthResponse() {
        return this.oAuthResponse;
    }

    public String getUsername(){
        return this.username;
    }
}