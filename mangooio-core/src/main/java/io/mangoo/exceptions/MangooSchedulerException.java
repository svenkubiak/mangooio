package io.mangoo.exceptions;

/**
 *
 * @author svenkubiak
 *
 */
public class MangooSchedulerException extends RuntimeException {
    private static final long serialVersionUID = -3567790195895714327L;

    public MangooSchedulerException(String message, Exception e){
        super(message, e);
    }
}