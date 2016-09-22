package io.mangoo.enums;

/**
 * 
 * @author svenkubiak
 *
 */
public enum Required {
    KEY("key can not be null"),
    MAP("map can not be null"),
    USERNAME("username can not be null"),
    PASSWORD("password can not be null"),
    CACHE_PROVIDER("cacheProvider can not be null"),
    CONFIG("config can not be null"),
    EXPIRES("expires can not be null"),
    HASH("hash can not be null"),
    DATE("date can not be null"),
    LOCAL_DATE_TIME("localDateTime can not be null"),
    LOCAL_DATE("localDate can not be null"),
    HTTP_HANDLER("httpHandler can not be null"),
    AUTHENTICITY("authenticity can not be null"),
    VALUES("values can not be null"),
    HTTP_SERVER_EXCHANGE("httpServerExchange can not be null"),
    MESSAGES("messages can not be null");

    private final String value;

    Required (String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}