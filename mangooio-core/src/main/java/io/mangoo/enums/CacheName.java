package io.mangoo.enums;

/**
 * Contains the names of the used caches
 * 
 * @author svenkubiak
 *
 */
public enum CacheName {
    WSS("mangooio-wss"),
    SSE("mangooio-sse"),
    AUTH("mangooio-auth"),
    REQUEST("mangooio-request"),
    APPLICATION("mangooio-application");

    private final String value;

    CacheName (String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}