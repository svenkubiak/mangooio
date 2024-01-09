package io.mangoo.enums;

public enum Queue {
    MAIL("mangooio-maillistener"),
    SSE("mangooio-sse");

    private final String value;

    Queue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}