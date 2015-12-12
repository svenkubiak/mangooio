package io.mangoo.models;

/**
 * Base class for holding exception information
 *
 * @author svenkubiak
 *
 */
public class Source {
    private final String content;
    private final boolean cause;
    private final int line;

    public Source(boolean cause, int line, String content) {
        this.cause = cause;
        this.line = line;
        this.content = content;
    }

    public boolean isCause() {
        return cause;
    }

    public int getLine() {
        return line;
    }

    public String getContent() {
        return content;
    }
}