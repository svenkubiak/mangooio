package io.mangoo.models;

/**
 *
 * @author svenkubiak
 *
 */
public class OAuthUser {
    private String oAuthResponse;
    private String username;
    private String picture;

    public OAuthUser(String oAuthResponse, String username, String picture) {
        this.oAuthResponse = oAuthResponse;
        this.picture = picture;
        this.username = username;
    }

    public String getOAuthResponse() {
        return this.oAuthResponse;
    }

    public String getUsername(){
        return this.username;
    }

    public String getPicture() {
        return this.picture;
    }
}