package io.mangoo.exceptions;

/**
 *
 * @author svenkubiak
 *
 */
public class MangooConfigurationException extends Exception {
    private static final long serialVersionUID = -4928845472170479321L;

    public MangooConfigurationException(String message, Exception e){
        super(message, e);
    }
}