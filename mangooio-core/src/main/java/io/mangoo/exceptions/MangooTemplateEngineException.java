package io.mangoo.exceptions;

import java.io.Serial;

public class MangooTemplateEngineException extends Exception {
    @Serial
    private static final long serialVersionUID = -5025696177592253242L;

    public MangooTemplateEngineException(String message, Exception e) {
        super(message, e);
    }
}