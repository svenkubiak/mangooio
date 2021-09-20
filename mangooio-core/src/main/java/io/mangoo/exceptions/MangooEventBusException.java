package io.mangoo.exceptions;

import java.io.Serial;

/**
 *
 * @author svenkubiak
 *
 */
public class MangooEventBusException extends Exception {
    @Serial
    private static final long serialVersionUID = 1928378985326453406L;

    public MangooEventBusException(Exception e) {
        super(e);
    }
}