package io.mangoo.interfaces;

import java.time.Instant;

/**
 * Revokes authentication tokens before their expiry; only consulted when authentication.blacklist is enabled.
 * The MongoDB implementation is bound when persistence is enabled and shares revocations between all instances, the
 * in-memory implementation otherwise; bind your own implementation in app.Module to replace it.
 */
public interface TokenBlacklist {

    /**
     * Revokes a single token; the revocation is no longer needed after expiresAt.
     */
    void revoke(String jwtId, Instant expiresAt);

    /**
     * Revokes all tokens of the subject issued before since; tokens issued afterwards (e.g. a new login) stay valid.
     */
    void revokeSubject(String subject, Instant since);

    default void revokeSubject(String subject) {
        revokeSubject(subject, Instant.now());
    }

    boolean isRevoked(String jwtId, String subject, Instant issuedAt);
}
