package io.mangoo.models;

/**
 *
 * @author svenkubiak
 *
 */
public class Source {
    private String content;
    private boolean cause;
    private int line;

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