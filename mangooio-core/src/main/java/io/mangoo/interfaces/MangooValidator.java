package io.mangoo.interfaces;

import io.mangoo.routing.bindings.Validator;

/**
 *
 * @author svenkubiak
 *
 */
public interface MangooValidator {
    /**
     * Returns the validator object for validation methods
     * @return A Validator object
     */
    public Validator validation();

    /**
     * Returns the errors message of specific field
     *
     * @param name The field to check
     * @return The errors message or null if no error
     */
    public String getError(String name);
}