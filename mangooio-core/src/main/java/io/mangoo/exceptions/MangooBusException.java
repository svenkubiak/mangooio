package io.mangoo.exceptions;

/**
 *
 * @author svenkubiak
 *
 */
public class MangooBusException extends Exception {
    private static final long serialVersionUID = 1928378985326453406L;

    public MangooBusException(Exception e) {
        super(e);
    }
}