package io.mangoo.exceptions;

/**
 * 
 * @author svenkubiak
 *
 */
public class MangooCacheException extends RuntimeException {
    private static final long serialVersionUID = -4928845472170479321L;

    public MangooCacheException(String message, Exception e){
        super(message, e);
    }
}