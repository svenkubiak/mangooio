package io.mangoo.exceptions;

import java.io.Serial;

/**
 * 
 * @author sven.kubiak
 *
 */
public class MangooTemplateEngineException extends Exception {
    @Serial
    private static final long serialVersionUID = -3362035234167593528L;
    
    public MangooTemplateEngineException(String message, Exception e) {
        super(message, e);
    }
}