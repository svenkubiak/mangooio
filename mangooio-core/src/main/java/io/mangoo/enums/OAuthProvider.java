package io.mangoo.enums;

public enum OAuthProvider {
    TWITTER("https://api.twitter.com/1.1/account/verify_credentials.json"),
    GOOGLE("https://api.twitter.com/1.1/account/verify_credentials.json"),
    FACEBOOK("https://api.twitter.com/1.1/account/verify_credentials.json");

    private final String value;

    OAuthProvider (String value) {
        this.value = value;
    }

    public String getUrl() {
        return this.value;
    }
}