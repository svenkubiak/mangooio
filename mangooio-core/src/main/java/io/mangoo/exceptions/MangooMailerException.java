package io.mangoo.exceptions;

/**
 *
 * @author svenkubiak
 *
 */
public class MangooMailerException extends Exception {
    private static final long serialVersionUID = 3207062008795009800L;

    public MangooMailerException(Exception e) {
        super(e);
    }
}