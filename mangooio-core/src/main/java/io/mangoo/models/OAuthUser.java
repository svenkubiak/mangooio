package io.mangoo.models;

/**
 * Base class for an OAuthUser
 *
 * @author svenkubiak
 *
 */
public class OAuthUser {
    private final String id;
    private final String oAuthResponse;
    private final String username;
    private final String picture;

    public OAuthUser(String id, String oAuthResponse, String username, String picture) {
        this.id = id;
        this.oAuthResponse = oAuthResponse;
        this.picture = picture;
        this.username = username;
    }

    public String getId() {
        return this.id;
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