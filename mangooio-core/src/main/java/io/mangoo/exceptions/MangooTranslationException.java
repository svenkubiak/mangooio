package io.mangoo.exceptions;

import java.io.Serial;

public class MangooTranslationException extends Exception {
    @Serial
    private static final long serialVersionUID = -83219832178922L;

    public MangooTranslationException(Exception e) {
        super(e);
    }

    public MangooTranslationException(String message) {
        super(message);
    }
}