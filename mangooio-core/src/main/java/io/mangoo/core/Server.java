package io.mangoo.core;

import java.util.Map;

import io.mangoo.enums.Default;
import io.mangoo.enums.Header;
import io.undertow.util.HttpString;

/**
 * 
 * @author svenkubiak
 *
 */
public class Server {
    private static Map<HttpString, String> headers = Map.of(
                Header.X_CONTENT_TYPE_OPTIONS.toHttpString(), Default.APPLICATION_HEADERS_XCONTENTTYPEOPTIONS.toString(),
                Header.X_FRAME_OPTIONS.toHttpString(), Default.APPLICATION_HEADERS_XFRAMEOPTIONS.toString(),
                Header.X_XSS_PPROTECTION.toHttpString(), Default.APPLICATION_HEADERS_XSSPROTECTION.toString(),
                Header.REFERER_POLICY.toHttpString(), Default.APPLICATION_HEADERS_REFERERPOLICY.toString(),
                Header.SERVER.toHttpString(), Default.APPLICATION_HEADERS_SERVER.toString(),
                Header.FEATURE_POLICY.toHttpString(), Default.APPLICATION_HEADERS_FEATUREPOLICY.toString(),
                Header.CONTENT_SECURITY_POLICY.toHttpString(), Default.APPLICATION_HEADERS_CONTENTSECURITYPOLICY.toString()
            );
    
    private Server() {
    }
    
    public static Map<HttpString, String> headers() {
        return headers;
    }
}