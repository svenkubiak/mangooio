package io.mangoo.enums.oauth;

/**
 * List of OAuthProviders
 *
 * @author svenkubiak
 *
 */
public enum OAuthProvider {
    TWITTER("twitter"),
    GOOGLE("google"),
    FACEBOOK("facebook");

    private final String value;

    OAuthProvider (String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}