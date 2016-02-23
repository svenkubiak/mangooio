package io.mangoo.exceptions;

/**
 * 
 * @author svenkubiak
 *
 */
public class MangooAuthenticationException extends RuntimeException {
    private static final long serialVersionUID = 1149390455629471268L;

    public MangooAuthenticationException(String message, Exception e){
        super(message, e);
    }
}