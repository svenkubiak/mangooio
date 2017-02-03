package io.mangoo.enums;

/**
 * Contains the names of the used caches
 * 
 * @author svenkubiak
 *
 */
public enum CacheName {
    APPLICATION("mangooio-application"),
    AUTH("mangooio-auth"),
    REQUEST("mangooio-request"),
    SSE("mangooio-sse"),
    WSS("mangooio-wss");

    private final String value;

    CacheName (String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}