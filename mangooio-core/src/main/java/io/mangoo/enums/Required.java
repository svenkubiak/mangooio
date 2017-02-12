package io.mangoo.enums;

/**
 * 
 * @author svenkubiak
 * @author William Dunne
 *
 */
public enum Required {
    ACCOUNT_NAME("the account name cannot be null"),
    APPLICATION_SECRET("the application secret cannot be null"),
    AUTHENTICITY("the authenticity cannot be null"),
    BCC_RECIPIENT("the bcc recipient cannot be null"),
    BODY("the body cannot be null"),
    CACHE_PROVIDER("the cacheProvider cannot be null"),
    CC_RECIPIENT("the cc recipient cannot be null"),
    CHANNEL("the channel cannot be null"),
    CHARSET("the charset cannot be null"),
    CLASS("the class cannot be null"),
    COMPONENT_TYPE("the component type cannot be null"),
    CONFIG("the config cannot be null"),
    CONFIG_FILE("the config file cannot be null"),
    CONNECTION("the connection cannot be null"),
    CONTENT("the content cannot be null"),
    CONTENT_TYPE("the content type cannot be null"),
    CONTROLLER_CLASS("the controller class cannot be null"),
    CONTROLLER_INSTANCE("the controller instance cannot be null"),
    CONTROLLER_METHOD("the controller method cannot be null"),
    CONTROLLER_NAME("the controller name cannot be null"),
    COOKIE("the cookie cannot be null"),
    CRON("the cron cannot be null"),
    CRYPTO("the crypto cannot be null"),
    DATA("the data cannot be null"),
    DATE("the date cannot be null"),
    EHCACHE("the ehCache cannot be null"),
    ENCRYPTED_TEXT("the encrypted text cannot be null"),
    EVENT_CALLBACK("the event callback cannot be null"),
    EXPIRES("the expires cannot be null"),
    FILE("the file cannot be null"),
    FROM("the from cannot be null"),
    GROUP_NAME("the group name cannot be null"),
    HASH("the hash cannot be null"),
    HEADERS("the headers cannot be null"),
    HTTP_HANDLER("the httpHandler cannot be null"),
    HTTP_SERVER_EXCHANGE("the httpServerExchange cannot be null"),
    IDENTITY("the identity cannot be null"),
    JOB_DETAIL("the job detail cannot be null"),
    JSON("the json cannot be null"),
    JSON_OBJECT("the json object cannot be null"),
    KEY("the key cannot be null"),
    LOCAL_DATE("the localDate cannot be null"),
    LOCAL_DATE_TIME("the localDateTime cannot be null"),
    LOCALE("the locale cannot be null"),
    MAP("the map cannot be null"),
    MESSAGES("the messages cannot be null"),
    METHOD("the method cannot be null"),
    METHOD_PARAMETERS("the method parameters cannot be null"),
    MODE("the mode cannot be null"),
    NAME("the name cannot be null"),
    OAUTH_PROVIDER("the OAuthProvider cannot be null"),
    OBJECT("the object cannot be null"),
    PACKAGE_NAME("the package name"),
    PASSWORD("the password cannot be null"),
    PATH("the path cannot be null"),
    PLAIN_TEXT("the plan text cannot be null"),
    RECIPIENT("the recipient cannot be null"),
    REDIRECT_TO("the redirect to cannot be null"),
    REQUEST_PARAMETER("the request parameter cannot be null"),
    RESPONSE("the response cannot be null"),
    ROUTE("the route cannot be null"),
    ROUTE_TYPE("the route type cannot be null"),
    SALT("the salt cannot be null"),
    SCHEDULER("the scheduler cannot be null"),
    SECRET("the secret cannot be null"),
    SOURCE_PATH("the source path cannot be null"),
    STACK_TRACE_ELEMENT("the stack trace element cannot be null"),
    START("the start cannot be null"),
    SUBJECT("the subject cannot be null"),
    TEMPLATE("the template cannot be null"),
    TEMPLATE_ENGINE("the tempalte engine cannot be null"),
    TEMPLATE_NAME("the template name cannot be null"),
    TRIGGER("the trigger cannot be null"),
    TRIGGER_FIRE_BUNDLE("the trigger fire bundle cannot be null"),
    URI("the uri cannot be null"),
    URI_CONNECTIONS("the uri connections cannot be null"),
    URL("the url cannot be null"),
    USERNAME("the username cannot be null"),
    VALIDATOR("the validator cannot be null"),
    VALUE("the value cannot be null"),
    VALUES("the values cannot be null");

    private final String value;

    Required (String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}
