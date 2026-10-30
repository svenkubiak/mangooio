package io.mangoo.persistence;

import com.mongodb.MongoException;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import io.mangoo.constants.Default;
import io.mangoo.constants.Required;
import io.mangoo.core.Config;
import io.mangoo.interfaces.TokenBlacklist;
import io.mangoo.interfaces.persistence.Datastore;
import io.mangoo.utils.Argument;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * TokenBlacklist shared by all instances in the collection mangooio_revocations; both checks of a request are a single
 * lookup on the primary key. Every entry lives as long as the revoked token could still be accepted, including the JWT
 * clock skew, so a document the TTL monitor has not yet removed only outlives tokens that are rejected anyway.
 * Fails closed: if MongoDB is unavailable, a token counts as revoked.
 */
@Singleton
public class MongoTokenBlacklist extends MongoStore implements TokenBlacklist {
    private static final Logger LOG = LogManager.getLogger(MongoTokenBlacklist.class);
    private static final String COLLECTION = "mangooio_revocations";
    private static final String TOKEN = "jti:";
    private static final String SUBJECT = "sub:";
    private static final String SINCE = "since";
    private final Duration maxTokenLifetime;
    private final Clock clock;

    @Inject
    public MongoTokenBlacklist(Datastore datastore, Config config) {
        this(datastore, maxTokenLifetime(Objects.requireNonNull(config, Required.CONFIG)), Clock.systemUTC());
    }

    MongoTokenBlacklist(Datastore datastore, Duration maxTokenLifetime, Clock clock) {
        super(datastore, COLLECTION);
        this.maxTokenLifetime = Objects.requireNonNull(maxTokenLifetime, "maxTokenLifetime can not be null");
        this.clock = Objects.requireNonNull(clock, "clock can not be null");
    }

    @Override
    public void revoke(String jwtId, Instant expiresAt) {
        Argument.requireNonBlank(jwtId, Required.ID);
        Objects.requireNonNull(expiresAt, "expiresAt can not be null");

        Instant until = withSkew(expiresAt);
        if (!until.isAfter(clock.instant())) {
            return;
        }

        try {
            collection().updateOne(Filters.eq(ID, TOKEN + jwtId), Updates.max(EXPIRE_AT, Date.from(until)), new UpdateOptions().upsert(true));
        } catch (MongoException | IllegalStateException e) {
            LOG.error("Failed to revoke a token", e);
        }
    }

    @Override
    public void revokeSubject(String subject, Instant since) {
        Argument.requireNonBlank(subject, Required.SUBJECT);
        Objects.requireNonNull(since, "since can not be null");

        try {
            collection().updateOne(Filters.eq(ID, SUBJECT + subject),
                    Updates.combine(
                            Updates.max(SINCE, Date.from(since)),
                            Updates.max(EXPIRE_AT, Date.from(withSkew(since.plus(maxTokenLifetime))))),
                    new UpdateOptions().upsert(true));
        } catch (MongoException | IllegalStateException e) {
            LOG.error("Failed to revoke the tokens of a subject", e);
        }
    }

    @Override
    public boolean isRevoked(String jwtId, String subject, Instant issuedAt) {
        List<String> ids = new ArrayList<>(2);
        if (StringUtils.isNotBlank(jwtId)) {
            ids.add(TOKEN + jwtId);
        }
        if (StringUtils.isNotBlank(subject) && issuedAt != null) {
            ids.add(SUBJECT + subject);
        }
        if (ids.isEmpty()) {
            return false;
        }

        try {
            for (Document document : collection().find(Filters.in(ID, ids))) {
                if (document.getString(ID).startsWith(TOKEN)) {
                    return true;
                }

                // iat has second precision, so a token issued within the second of the revocation (e.g. a new login) stays valid
                Date since = document.getDate(SINCE);
                if (since != null && issuedAt.isBefore(since.toInstant().truncatedTo(ChronoUnit.SECONDS))) {
                    return true;
                }
            }

            return false;
        } catch (MongoException | IllegalStateException e) {
            LOG.error("Failed to read revocations, the token counts as revoked", e);
            return true;
        }
    }

    private static Instant withSkew(Instant instant) {
        return instant.plusSeconds(Default.JWT_CLOCK_SKEW);
    }

    private static Duration maxTokenLifetime(Config config) {
        return Duration.ofSeconds(Math.max(config.getAuthenticationCookieRememberExpires(), config.getAuthenticationCookieTokenExpires()));
    }
}
