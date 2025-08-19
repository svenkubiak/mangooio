package io.mangoo.exceptions;

import java.io.Serial;

public class MangooJwtException extends Exception {
    @Serial
    private static final long serialVersionUID = 4005271906316201971L;

    public MangooJwtException(Exception e) {
        super(e);
    }

    public MangooJwtException(String message) {
    }
}