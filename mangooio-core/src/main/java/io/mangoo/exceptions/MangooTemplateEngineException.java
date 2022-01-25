package io.mangoo.exceptions;

/**
 * 
 * @author svenkubiak
 *
 */
public class MangooTemplateEngineException extends Exception {
    private static final long serialVersionUID = -5025696177592253242L;

    public MangooTemplateEngineException(String message, Exception e) {
        super(message, e);
    }
}