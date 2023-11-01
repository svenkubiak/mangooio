package io.mangoo.exceptions;

import java.io.Serial;

public class MangooTokenException extends Exception {
    @Serial
    private static final long serialVersionUID = 4005271906316201971L;

    public MangooTokenException(Exception e) {
        super(e);
    }
}