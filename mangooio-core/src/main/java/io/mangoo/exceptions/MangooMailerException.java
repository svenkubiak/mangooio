package io.mangoo.exceptions;

/**
 *
 * @author svenkubiak
 *
 */
public class MangooMailerException extends Exception {
    private static final long serialVersionUID = 8991735383215886040L;

    public MangooMailerException(Exception e) {
        super(e);
    }
}