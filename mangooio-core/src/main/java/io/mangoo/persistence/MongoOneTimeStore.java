package io.mangoo.persistence;

import com.mongodb.MongoException;
import com.mongodb.MongoServerException;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import io.mangoo.constants.Required;
import io.mangoo.interfaces.OneTimeStore;
import io.mangoo.interfaces.persistence.Datastore;
import io.mangoo.utils.Argument;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;

import java.time.Instant;
import java.util.Date;
import java.util.Objects;

/**
 * OneTimeStore shared by all instances in the collection mangooio_claims. A document that outlives expiresAt until the
 * TTL monitor removes it only makes a claim fail longer, never succeed twice. Fails closed: if MongoDB is unavailable,
 * every claim is refused.
 */
@Singleton
public class MongoOneTimeStore extends MongoStore implements OneTimeStore {
    private static final Logger LOG = LogManager.getLogger(MongoOneTimeStore.class);
    private static final String COLLECTION = "mangooio_claims";
    private static final String VALUE = "value";

    @Inject
    public MongoOneTimeStore(Datastore datastore) {
        super(datastore, COLLECTION);
    }

    @Override
    public boolean claim(String key, Instant expiresAt) {
        Argument.requireNonBlank(key, Required.KEY);
        Objects.requireNonNull(expiresAt, "expiresAt can not be null");

        try {
            collection().insertOne(new Document(ID, key).append(EXPIRE_AT, Date.from(expiresAt)));
            return true;
        } catch (MongoServerException e) {
            if (!isDuplicateKey(e)) {
                LOG.error("Failed to claim a one-time key, the claim is refused", e);
            }
            return false;
        } catch (MongoException | IllegalStateException e) {
            LOG.error("Failed to claim a one-time key, the claim is refused", e);
            return false;
        }
    }

    // If the stored value is not smaller, the filter does not match and the upsert collides with the existing _id
    @Override
    public boolean advance(String key, long value, Instant expiresAt) {
        Argument.requireNonBlank(key, Required.KEY);
        Objects.requireNonNull(expiresAt, "expiresAt can not be null");

        try {
            collection().updateOne(
                    Filters.and(Filters.eq(ID, key), Filters.lt(VALUE, value)),
                    Updates.combine(Updates.set(VALUE, value), Updates.set(EXPIRE_AT, Date.from(expiresAt))),
                    new UpdateOptions().upsert(true));
            return true;
        } catch (MongoServerException e) {
            if (!isDuplicateKey(e)) {
                LOG.error("Failed to advance a one-time value, the value is refused", e);
            }
            return false;
        } catch (MongoException | IllegalStateException e) {
            LOG.error("Failed to advance a one-time value, the value is refused", e);
            return false;
        }
    }
}
