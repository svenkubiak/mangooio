package io.mangoo.core;

import io.mangoo.constants.Default;
import io.mangoo.constants.Header;
import io.mangoo.constants.NotNull;
import io.undertow.util.HttpString;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class Server {
    private static Map<HttpString, String> headers = Map.of(
                Header.X_CONTENT_TYPE_OPTIONS, Default.APPLICATION_HEADERS_X_CONTENT_TYPE_OPTIONS,
                Header.X_FRAME_OPTIONS, Default.APPLICATION_HEADERS_X_FRAME_OPTIONS,
                Header.X_XSS_PROTECTION, Default.APPLICATION_HEADERS_XSS_PROTECTION,
                Header.REFERER_POLICY, Default.APPLICATION_HEADERS_REFERER_POLICY,
                Header.FEATURE_POLICY, Default.APPLICATION_HEADERS_FEATURE_POLICY,
                Header.CONTENT_SECURITY_POLICY, Default.APPLICATION_HEADERS_CONTENT_SECURITY_POLICY,
                Header.SERVER, Default.APPLICATION_HEADERS_SERVER
            );
    
    private Server() {
    }
    
    public static Map<HttpString, String> headers() {
        return headers;
    }
    
    /**
     * Sets a custom header that is used globally on server responses
     * 
     * @param header The name of the header
     * @param value The value of the header
     */
    public static void header(HttpString header, String value) {
        Objects.requireNonNull(header, NotNull.HEADER);
        
        Map<HttpString, String> newHeaders = new HashMap<>(headers);
        newHeaders.put(header, value);
        headers = newHeaders;
    }
}