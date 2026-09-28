package io.mangoo.exceptions;

import java.io.Serial;

/**
 * Thrown when an Argon2 hash could not be computed because no slot became
 * available within authentication.hashing.timeout
 * <p>
 * This exception is unchecked on purpose. Hashing is performed by
 * {@link io.mangoo.utils.CommonUtils#hashArgon2(String, String)} and
 * {@link io.mangoo.utils.CommonUtils#matchArgon2(String, String, String)},
 * whose signatures must stay source compatible for existing callers
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
