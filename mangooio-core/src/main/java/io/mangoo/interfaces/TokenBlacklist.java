package io.mangoo.interfaces;

import java.time.Instant;

public interface TokenBlacklist {
    void revoke(String jwtId, Instant expiresAt);
    void revokeSubject(String subject, Instant since);
    default void revokeSubject(String subject) {
        revokeSubject(subject, Instant.now());
    }
    boolean isRevoked(String jwtId, String subject, Instant issuedAt);
}
