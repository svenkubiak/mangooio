package io.mangoo.exceptions;

/**
 * 
 * @author svenkubiak
 *
 */
public class MangooSchedulerException extends Exception {
    private static final long serialVersionUID = 1779894804567372681L;

    public MangooSchedulerException(Exception e) {
        super(e);
    }
}