package io.mangoo.persistence;

import com.mongodb.MongoException;
import com.mongodb.MongoServerException;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import io.mangoo.constants.Required;
import io.mangoo.interfaces.AttemptStore;
import io.mangoo.persistence.interfaces.Datastore;
import io.mangoo.utils.Argument;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * AttemptStore shared by all instances in the collection mangooio_attempts. Every call is a single atomic operation and
 * computes the time with $$NOW, so clock drift between instances does not affect locks. Fails closed: if MongoDB is
 * unavailable, an attempt is refused and a key counts as locked.
 */
@Singleton
public class MongoAttemptStore extends MongoStore implements AttemptStore {
    private static final Logger LOG = LogManager.getLogger(MongoAttemptStore.class);
    private static final String COLLECTION = "mangooio_attempts";
    private static final Duration RETENTION = Duration.ofMinutes(60);
    private static final String NOW = "$$NOW";
    private static final String ATTEMPTS = "attempts";
    private static final String LOCKED_UNTIL = "lockedUntil";
    private static final String GRANTED = "granted";
    private static final String EXPIRED = "expired";

    @Inject
    public MongoAttemptStore(Datastore datastore) {
        super(datastore, COLLECTION);
    }

    @Override
    public boolean tryAcquire(String key, int maxAttempts, Duration lockDuration) {
        Argument.requireNonBlank(key, Required.KEY);
        Objects.requireNonNull(lockDuration, Required.DURATION);

        var options = new FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER);
        var pipeline = acquirePipeline(maxAttempts, lockDuration);
        try {
            Document document;
            try {
                document = collection().findOneAndUpdate(Filters.eq(ID, key), pipeline, options);
            } catch (MongoServerException e) {
                // Two concurrent first attempts on a new key collide on _id; the second one finds the document on retry
                if (!isDuplicateKey(e)) {
                    throw e;
                }
                document = collection().findOneAndUpdate(Filters.eq(ID, key), pipeline, options);
            }

            return document != null && Boolean.TRUE.equals(document.getBoolean(GRANTED));
        } catch (MongoException | IllegalStateException e) {
            LOG.error("Failed to acquire an attempt, the attempt is refused", e);
            return false;
        }
    }

    // "Locked by this attempt" is granted and "already locked" is refused, but both leave the same attempts and
    // lockedUntil, hence the granted field
    static List<Bson> acquirePipeline(int maxAttempts, Duration lockDuration) {
        long retention = Math.max(RETENTION.toMillis(), lockDuration.toMillis());

        return List.of(
                set(new Document(ATTEMPTS, ifNull("$" + ATTEMPTS, 0))
                        .append(LOCKED_UNTIL, ifNull("$" + LOCKED_UNTIL, null))),
                set(new Document(EXPIRED, new Document("$and", List.of(
                        new Document("$ne", Arrays.asList("$" + LOCKED_UNTIL, null)),
                        new Document("$lte", List.of("$" + LOCKED_UNTIL, NOW)))))),
                set(new Document(ATTEMPTS, cond("$" + EXPIRED, 0, "$" + ATTEMPTS))
                        .append(LOCKED_UNTIL, cond("$" + EXPIRED, null, "$" + LOCKED_UNTIL))),
                set(new Document(GRANTED, new Document("$eq", Arrays.asList("$" + LOCKED_UNTIL, null)))),
                set(new Document(ATTEMPTS, cond("$" + GRANTED, new Document("$add", List.of("$" + ATTEMPTS, 1)), "$" + ATTEMPTS))),
                set(new Document(LOCKED_UNTIL, cond(
                        new Document("$and", List.of("$" + GRANTED, new Document("$gte", List.of("$" + ATTEMPTS, maxAttempts)))),
                        new Document("$add", List.of(NOW, lockDuration.toMillis())),
                        "$" + LOCKED_UNTIL))),
                set(new Document(EXPIRE_AT, new Document("$max", List.of(
                        "$" + LOCKED_UNTIL, new Document("$add", List.of(NOW, retention)))))),
                new Document("$unset", EXPIRED));
    }

    @Override
    public void release(String key, int maxAttempts) {
        Argument.requireNonBlank(key, Required.KEY);

        try {
            collection().updateOne(Filters.eq(ID, key), List.of(
                    set(new Document(ATTEMPTS, new Document("$max", List.of(
                            new Document("$subtract", List.of(ifNull("$" + ATTEMPTS, 0), 1)), 0)))),
                    set(new Document(LOCKED_UNTIL, cond(
                            new Document("$lt", List.of("$" + ATTEMPTS, maxAttempts)), null, "$" + LOCKED_UNTIL)))));
        } catch (MongoException | IllegalStateException e) {
            LOG.error("Failed to release an attempt", e);
        }
    }

    @Override
    public void reset(String key) {
        Argument.requireNonBlank(key, Required.KEY);

        try {
            collection().deleteOne(Filters.eq(ID, key));
        } catch (MongoException | IllegalStateException e) {
            LOG.error("Failed to reset attempts", e);
        }
    }

    @Override
    public boolean isLocked(String key) {
        Argument.requireNonBlank(key, Required.KEY);

        try {
            return collection().find(Filters.and(
                            Filters.eq(ID, key),
                            Filters.expr(new Document("$gt", List.of("$" + LOCKED_UNTIL, NOW)))))
                    .first() != null;
        } catch (MongoException | IllegalStateException e) {
            LOG.error("Failed to read the lock, the key counts as locked", e);
            return true;
        }
    }

    private static Document set(Document fields) {
        return new Document("$set", fields);
    }

    private static Document ifNull(String field, Object replacement) {
        return new Document("$ifNull", Arrays.asList(field, replacement));
    }

    private static Document cond(Object condition, Object then, Object otherwise) {
        return new Document("$cond", Arrays.asList(condition, then, otherwise));
    }
}
