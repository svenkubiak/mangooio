package io.mangoo.exceptions;

import org.apache.commons.mail.EmailException;

/**
 *
 * @author svenkubiak
 *
 */
public class MangooMailerException extends Exception {
    private static final long serialVersionUID = 8991735383215886040L;

    public MangooMailerException(EmailException e) {
        super(e);
    }
}