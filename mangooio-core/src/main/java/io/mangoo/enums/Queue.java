package io.mangoo.enums;

public enum Queue {
    MAIL("mangooio-mail"),
    SSE_CONNECTED("mangooio-sse-connected"),
    SSE_DISCONNECTED("mangooio-sse-disconnected");

    private final String value;

    Queue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}