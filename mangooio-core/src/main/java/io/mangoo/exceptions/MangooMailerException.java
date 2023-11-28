package io.mangoo.exceptions;

import java.io.Serial;

public class MangooMailerException extends Exception {
    @Serial
    private static final long serialVersionUID = 3207062008795009800L;

    public MangooMailerException(Exception e) {
        super(e);
    }
}