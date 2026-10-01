package io.mangoo.exceptions;

import java.io.Serial;

/**
 * Thrown when no Argon2 hashing slot became available within authentication.hashing.timeout.
 * Unchecked so that the signatures of CommonUtils#hashArgon2 and #matchArgon2 stay source compatible.
 */
public class MangooHashingException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 4013204817720683051L;

    public MangooHashingException(String message) {
        super(message);
    }

    public MangooHashingException(String message, Exception e) {
        super(message, e);
    }
}
