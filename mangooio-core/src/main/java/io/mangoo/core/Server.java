package io.mangoo.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import io.mangoo.enums.Default;
import io.mangoo.enums.Header;
import io.mangoo.enums.Required;
import io.undertow.util.HttpString;

/**
 * 
 * @author svenkubiak
 *
 */
public final class Server {
    private static Map<HttpString, String> headers = Map.of(
                Header.X_CONTENT_TYPE_OPTIONS.toHttpString(), Default.APPLICATION_HEADERS_XCONTENTTYPEOPTIONS.toString(),
                Header.X_FRAME_OPTIONS.toHttpString(), Default.APPLICATION_HEADERS_XFRAMEOPTIONS.toString(),
                Header.X_XSS_PPROTECTION.toHttpString(), Default.APPLICATION_HEADERS_XSSPROTECTION.toString(),
                Header.REFERER_POLICY.toHttpString(), Default.APPLICATION_HEADERS_REFERERPOLICY.toString(),
                Header.FEATURE_POLICY.toHttpString(), Default.APPLICATION_HEADERS_FEATUREPOLICY.toString(),
                Header.CONTENT_SECURITY_POLICY.toHttpString(), Default.APPLICATION_HEADERS_CONTENTSECURITYPOLICY.toString(),
                Header.SERVER.toHttpString(), Default.APPLICATION_HEADERS_SERVER.toString()
            );
    
    private Server() {
    }
    
    public static Map<HttpString, String> headers() {
        return headers;
    }
    
    /**
     * Sets a custom header that is used globally on server responses
     * 
     * @param name The name of the header
     * @param value The value of the header
     */
    public static void header(Header name, String value) {
        Objects.requireNonNull(name, Required.NAME.toString());
        
        Map<HttpString, String> newHeaders = new HashMap<>(headers);
        newHeaders.put(name.toHttpString(), value);
        headers = newHeaders;
    }
}