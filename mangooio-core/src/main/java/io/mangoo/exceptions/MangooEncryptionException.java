package io.mangoo.exceptions;

import java.io.Serial;

public class MangooEncryptionException extends Exception {
    @Serial
    private static final long serialVersionUID = -1019923813420183496L;

    public MangooEncryptionException(String message, Exception e) {
        super(message, e);
    }
}