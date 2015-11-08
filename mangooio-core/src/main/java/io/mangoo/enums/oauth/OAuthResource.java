package io.mangoo.enums.oauth;

/**
 * List of OAuthResources
 *
 * @author skubiak
 *
 */
public enum OAuthResource {
    TWITTER("https://api.twitter.com/1.1/account/verify_credentials.json"),
    GOOGLE("https://www.googleapis.com/oauth2/v2/userinfo?alt=json"),
    FACEBOOK("https://graph.facebook.com/me?fields=id,name,picture");

    private final String value;

    OAuthResource (String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}