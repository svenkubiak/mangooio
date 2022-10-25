package io.mangoo.exceptions;

/**
 *
 * @author svenkubiak
 *
 */
public class MangooTokenException extends Exception {
    private static final long serialVersionUID = 3207062008795009800L;

    public MangooTokenException(Exception e) {
        super(e);
    }
}