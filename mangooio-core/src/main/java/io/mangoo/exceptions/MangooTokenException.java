package io.mangoo.exceptions;

public class MangooTokenException extends Exception {
    private static final long serialVersionUID = 4005271906316201971L;

    public MangooTokenException(Exception e) {
        super(e);
    }
}