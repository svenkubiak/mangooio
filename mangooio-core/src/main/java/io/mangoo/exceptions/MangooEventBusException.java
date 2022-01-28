package io.mangoo.exceptions;

/**
 *
 * @author svenkubiak
 *
 */
public class MangooEventBusException extends Exception {
    private static final long serialVersionUID = 8997983614444596732L;

    public MangooEventBusException(Exception e) {
        super(e);
    }
}