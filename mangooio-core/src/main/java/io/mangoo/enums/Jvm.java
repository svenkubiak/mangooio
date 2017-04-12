package io.mangoo.enums;

/**
 * Key strings for reading JVM properties
 *
 * @author svenkubiak
 *
 */
public enum Jvm {
    APPLICATION_CONFIG("application.config"),
    APPLICATION_MODE("application.mode"),
    APPLICATION_LOG("application.log"),
    APPLICATION_MASTERKEY("application.masterkey"),   
    
    @Deprecated /** Will be removed in 5.0.0 **/
    HTTP_HOST("http.host"),
    @Deprecated /** Will be removed in 5.0.0 **/
    HTTP_PORT("http.port"),
    @Deprecated /** Will be removed in 5.0.0 **/
    AJP_HOST("ajp.host"),
    @Deprecated /** Will be removed in 5.0.0 **/
    AJP_PORT("ajp.port"),;

    private final String value;

    Jvm (String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}