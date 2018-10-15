package io.mangoo.exceptions;

/**
 * 
 * @author svenkubiak
 *
 */
public class MangooEncryptionException extends Exception {
    private static final long serialVersionUID = 538069728936315775L;

    public MangooEncryptionException(String message, Exception e) {
        super(message, e);
    }

    public MangooEncryptionException(String message) {
        super(message);
    }
}