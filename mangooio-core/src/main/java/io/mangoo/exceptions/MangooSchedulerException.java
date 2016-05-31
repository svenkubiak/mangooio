package io.mangoo.exceptions;

/**
 * 
 * @author svenkubiak
 *
 */
public class MangooSchedulerException extends Exception {
    private static final long serialVersionUID = 8991735383215886040L;

    public MangooSchedulerException(Exception e) {
        super(e);
    }
}