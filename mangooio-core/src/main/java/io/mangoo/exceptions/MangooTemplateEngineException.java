package io.mangoo.exceptions;

/**
 * 
 * @author sven.kubiak
 *
 */
public class MangooTemplateEngineException extends RuntimeException {
    private static final long serialVersionUID = -3362035234167593528L;

    public MangooTemplateEngineException(String message, Exception e){
        super(message, e);
    }
}