package io.mangoo.exceptions;

import java.io.Serial;

/**
 * 
 * @author svenkubiak
 *
 */
public class MangooEncryptionException extends Exception {
    @Serial
    private static final long serialVersionUID = 538069728936315775L;

    public MangooEncryptionException(String message, Exception e) {
        super(message, e);
    }
}