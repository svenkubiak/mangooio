package io.mangoo.exceptions;

import java.io.Serial;

public class MangooHashingException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 4013204817720683051L;

    public MangooHashingException(String message) {
        super(message);
    }

    public MangooHashingException(String message, Exception e) {
        super(message, e);
    }
}
