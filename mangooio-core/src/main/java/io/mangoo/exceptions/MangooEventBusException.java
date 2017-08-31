package io.mangoo.exceptions;

/**
 *
 * @author svenkubiak
 *
 */
public class MangooEventBusException extends Exception {
    private static final long serialVersionUID = 1928378985326453406L;

    public MangooEventBusException(Exception e) {
        super(e);
    }
}