package io.mangoo.exceptions;

import java.io.Serial;

public class MangooJwtExeption extends Exception {
    @Serial
    private static final long serialVersionUID = 4005271906316201971L;

    public MangooJwtExeption(Exception e) {
        super(e);
    }
}