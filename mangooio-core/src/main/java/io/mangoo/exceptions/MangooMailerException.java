package io.mangoo.exceptions;

import java.io.Serial;

/**
 *
 * @author svenkubiak
 *
 */
public class MangooMailerException extends Exception {
    @Serial
    private static final long serialVersionUID = 8991735383215886040L;

    public MangooMailerException(Exception e) {
        super(e);
    }
}