package io.mangoo.exceptions;

/**
 *
 * @author svenkubiak
 *
 */
public class MangooRequestException extends Exception {
    private static final long serialVersionUID = 7448433941075404307L;

    public MangooRequestException(Exception e){
        super(e);
    }
}