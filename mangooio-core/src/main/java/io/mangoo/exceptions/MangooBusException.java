package io.mangoo.exceptions;

/**
 *
 * @author svenkubiak
 *
 */
public class MangooBusException extends Exception {
    private static final long serialVersionUID = 8991735383215886040L;

    public MangooBusException(Exception e) {
        super(e);
    }
}