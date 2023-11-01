package io.mangoo.exceptions;

import java.io.Serial;

public class MangooEventBusException extends Exception {
    @Serial
    private static final long serialVersionUID = 8997983614444596732L;

    public MangooEventBusException(Exception e) {
        super(e);
    }
}