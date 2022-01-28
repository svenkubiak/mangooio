package io.mangoo.exceptions;

/**
 * 
 * @author svenkubiak
 *
 */
public class MangooEncryptionException extends Exception {
    private static final long serialVersionUID = -1019923813420183496L;

    public MangooEncryptionException(String message, Exception e) {
        super(message, e);
    }
}