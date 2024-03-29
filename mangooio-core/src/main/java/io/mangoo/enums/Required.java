package io.mangoo.enums;

public enum Required {
    ACCOUNT_NAME("account name can not be null"),
    ALGORITHM("algorithm can not be null"),
    BCCS("bccs can not be null"),
    BYTES("bytes can not be null"),
    CACHE("cache can not be null"),
    CACHE_PROVIDER("cacheProvider can not be null"),
    CCS("ccs can not be null"),
    CHARSET("charset can not be null"),
    CLAIM_KEY("claim key can not be null"),
    CLASS("class can not be null"),
    CONFIG("config can not be null"),
    CONNECTION("connection can not be null"),
    CONTENT("content can not be null"),
    CONTENT_TYPE("content type can not be null"),
    CONTROLLER_CLASS("controller class can not be null"),
    CONTROLLER_INSTANCE("controller instance can not be null"),
    CONTROLLER_METHOD("controller method can not be null"),
    CONTROLLER_NAME("controller name can not be null"),
    COOKIE("cookie can not be null"),
    COOKIE_VALUE("cookievalue can not be null"),
    COLLECTION("collection can not be null"),
    CRON("cron can no be null"),
    CRYPTO("crypto can not be null"),
    DATA("data can not be null"),
    DATASTORE("datastore can not be null"),
    DIGITS("digits can not be null"),
    ENCRYPTED_TEXT("encrypted text can not be null"),
    EVENT("event can not be null"),
    EVENT_LISTENER("eventListener can not be null"),
    EXPIRES("expires can not be null"),
    FALLBACK("fallback can not be null"),
    FILE("file can not be null"),
    FROM("from can not be null"),
    HASH("hash can not be null"),
    HEADER("header can not be null"),
    HEADERS("headers can not be null"),
    HTTP_HANDLER("httpHandler can not be null"),
    HTTP_METHOD("HTTP method can not be null"),
    HTTP_SERVER_EXCHANGE("httpServerExchange can not be null"),
    ID("id can not be null"),
    INDEX("index can not be null"),
    INDEX_OPTIONS("indexOptions can not be null"),
    INDEXES("indexes van not be null"),
    ISSUER("issuer can not be null"),
    JSON("json can not be null"),
    JSON_OBJECT("json object can not be null"),
    KEY("key can not be null"),
    LOCAL_DATE("localDate can not be null"),
    LOCAL_DATE_TIME("localDateTime can not be null"),
    LOCALE("locale can not be null"),
    MAIL("mail can not be null"),
    MAP("map can not be null"),
    MESSAGE("message can not be null"),
    MESSAGES("messages can not be null"),
    METHOD("method can not be null"),
    METHOD_PARAMETERS("method parameters can not be null"),
    MIME_MESSAGE("mimeMessage can not be null"),
    MODE("mode can not be null"),
    NAME("name can not be null"),
    OBJECT("object can not be null"),
    OBJECTS("objects can not be null"),
    PART("part can not be null"),
    PASETO("paseto can not be null"),
    PASSWORD("password can not be null"),
    PATH("path can not be null"),
    PERIOD("period can not be null"),
    PLAIN_TEXT("plain text can not be null"),
    PREFIX("prefix can not be null"),
    PRIORITY("Priority of 1 through 5 are acceptable, with 1 being the highest priority, 3 = normal and 5 = lowest priority"),
    PRIVATE_KEY("private key can not be null"),
    PUBLIC_KEY("public key can not be null"),
    QUEUE("queue can not be null"),
    REDIRECT_TO("redirect to can not be null"),
    REPLY_TO("replyTo can not be null"),
    REQUEST_PARAMETER("request parameter can not be null"),
    RESOURCE("resource can not be null"),
    RESPONSE("response can not be null"),
    ROUTE("route can not be null"),
    SALT("salt can not be null"),
    SECRET("secret can not be null"),
    SHARED_SECRET("sharedCecret can not be null"),
    SOURCE_PATH("source path can not be null"),
    STACK_TRACE_ELEMENT("stack trace element can not be null"),
    START("start can not be null"),
    STRING("string can not be null"),
    SUBJECT("subject can not be null"),
    TEMPLATE("template can not be null"),
    TEMPLATE_ENGINE("template engine can not be null"),
    TEMPLATE_NAME("template name can not be null"),
    TEMPORAL_UNIT("temporalunit can not be null"),
    TOS("tos can not be null"),
    TOTP("totp can not be null"),
    UNIT("unit can not be null"),
    URI("uri can not be null"),
    URL("url can not be null"),
    USERNAME("username can not be null"),
    VALUE("value can not be null"),
    VALUES("values can not be null");
    
    private final String value;

    Required (String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}