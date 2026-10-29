package io.mangoo.persistence;

import com.mongodb.ErrorCategory;
import com.mongodb.MongoServerException;
import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import io.mangoo.constants.Required;
import io.mangoo.persistence.interfaces.Datastore;
import io.mangoo.utils.Argument;
import org.bson.Document;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Base of the MongoDB implementations of shared framework state in the default datastore. Reads go to the primary and
 * writes are acknowledged by the majority, whatever the connection string sets, so a lock or revocation is never read
 * from a lagging secondary and a claim is not lost on a failover. The implementations fail closed on a MongoException
 * and on the IllegalStateException of a closed client.
 */
public abstract class MongoStore {
    static final String EXPIRE_AT = "expireAt";
    static final String ID = "_id";
    private final MongoCollection<Document> collection;

    // Must not access the database, as the injector creates singletons eagerly before the datastore is checked
    protected MongoStore(Datastore datastore, String name) {
        Objects.requireNonNull(datastore, Required.DATASTORE);
        Argument.requireNonBlank(name, Required.NAME);

        this.collection = datastore.getMongoDatabase()
                .getCollection(name)
                .withReadPreference(ReadPreference.primary())
                .withWriteConcern(WriteConcern.MAJORITY);
    }

    protected MongoCollection<Document> collection() {
        return collection;
    }

    /** Creates the TTL index on expireAt; called once by the startup after the classpath scan. */
    public void prepare() {
        collection.createIndex(Indexes.ascending(EXPIRE_AT), new IndexOptions().expireAfter(0L, TimeUnit.SECONDS));
    }

    static boolean isDuplicateKey(MongoServerException e) {
        return ErrorCategory.fromErrorCode(e.getCode()) == ErrorCategory.DUPLICATE_KEY;
    }
}
