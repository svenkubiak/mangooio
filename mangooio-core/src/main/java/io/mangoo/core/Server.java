package io.mangoo.core;

import io.mangoo.constants.Header;
import io.mangoo.constants.NotNull;
import io.undertow.util.HttpString;
import org.apache.logging.log4j.util.Strings;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class Server {
    private static Map<HttpString, String> headers = Map.of(
                Header.X_CONTENT_TYPE_OPTIONS, "nosniff",
                Header.X_FRAME_OPTIONS, "DENY",
                Header.X_XSS_PROTECTION, "1",
                Header.REFERER_POLICY, "no-referrer",
                Header.FEATURE_POLICY, Strings.EMPTY,
                Header.CONTENT_SECURITY_POLICY, Strings.EMPTY,
                Header.SERVER, "Undertow"
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