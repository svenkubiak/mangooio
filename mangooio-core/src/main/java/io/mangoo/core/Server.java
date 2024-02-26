package io.mangoo.core;

import io.mangoo.enums.Default;
import io.mangoo.enums.Header;
import io.mangoo.enums.Required;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public final class Server {
    private static Map<Header, String> headers = Map.of(
                Header.X_CONTENT_TYPE_OPTIONS, Default.APPLICATION_HEADERS_X_CONTENT_TYPE_OPTIONS.toString(),
                Header.X_FRAME_OPTIONS, Default.APPLICATION_HEADERS_X_FRAME_OPTIONS.toString(),
                Header.X_XSS_PROTECTION, Default.APPLICATION_HEADERS_XSS_PROTECTION.toString(),
                Header.REFERER_POLICY, Default.APPLICATION_HEADERS_REFERER_POLICY.toString(),
                Header.FEATURE_POLICY, Default.APPLICATION_HEADERS_FEATURE_POLICY.toString(),
                Header.CONTENT_SECURITY_POLICY, Default.APPLICATION_HEADERS_CONTENT_SECURITY_POLICY.toString(),
                Header.SERVER, Default.APPLICATION_HEADERS_SERVER.toString()
            );
    
    private Server() {
    }
    
    public static Map<Header, String> headers() {
        return headers;
    }
    
    /**
     * Sets a custom header that is used globally on server responses
     * 
     * @param header The name of the header
     * @param value The value of the header
     */
    public static void header(Header header, String value) {
        Objects.requireNonNull(header, Required.HEADER.toString());
        
        Map<Header, String> newHeaders = new EnumMap<>(headers);
        newHeaders.put(header, value);
        headers = newHeaders;
    }
}