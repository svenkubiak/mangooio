package io.mangoo.persistence;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Collation;
import com.mongodb.client.model.IndexOptions;
import org.bson.BsonDocument;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/** Checks the indexes that the startup creates with persistence.mongo.schema create, for persistence.mongo.schema validate. */
public final class MongoSchema {
    public static final String CREATE = "create";
    public static final String VALIDATE = "validate";

    private MongoSchema() {
    }

    /** True if the collection has an index with these keys, the same uniqueness, collation and expiry. */
    public static boolean hasIndex(MongoCollection<?> collection, Bson keys, IndexOptions options) {
        Objects.requireNonNull(collection, "collection can not be null");
        Objects.requireNonNull(keys, "keys can not be null");
        Objects.requireNonNull(options, "options can not be null");

        Map<String, Integer> expected = keys(keys.toBsonDocument());
        for (Document index : collection.listIndexes()) {
            if (expected.equals(keys(index.get("key", Document.class).toBsonDocument()))
                    && options.isUnique() == Boolean.TRUE.equals(index.getBoolean("unique"))
                    && Objects.equals(collation(options.getCollation()), collation(index.get("collation", Document.class)))
                    && Objects.equals(options.getExpireAfter(TimeUnit.SECONDS), expireAfter(index))) {
                return true;
            }
        }

        return false;
    }

    /** The mongosh command that creates the index. */
    public static String createIndexCommand(String collection, Bson keys, IndexOptions options) {
        var document = new Document();
        if (options.isUnique()) {
            document.append("unique", true);
        }
        if (options.getCollation() != null) {
            document.append("collation", new Document("locale", options.getCollation().getLocale())
                    .append("strength", options.getCollation().getStrength().getIntRepresentation()));
        }
        if (options.getExpireAfter(TimeUnit.SECONDS) != null) {
            document.append("expireAfterSeconds", options.getExpireAfter(TimeUnit.SECONDS));
        }

        return "db.getCollection(\"" + collection + "\").createIndex(" + keys.toBsonDocument().toJson() + ", " + document.toJson() + ")";
    }

    // mongosh may store 1 as a double, so the direction is compared as a number
    private static Map<String, Integer> keys(BsonDocument keys) {
        Map<String, Integer> result = new LinkedHashMap<>();
        for (Map.Entry<String, BsonValue> entry : keys.entrySet()) {
            BsonValue value = entry.getValue();
            result.put(entry.getKey(), value.isNumber() ? value.asNumber().intValue() : null);
        }

        return result;
    }

    private static String collation(Collation collation) {
        if (collation == null) {
            return null;
        }

        // The server stores the tertiary strength when a collation is created without one
        int strength = collation.getStrength() == null ? 3 : collation.getStrength().getIntRepresentation();
        return collation.getLocale() + "/" + strength;
    }

    private static String collation(Document collation) {
        return collation == null ? null : collation.getString("locale") + "/" + ((Number) collation.get("strength")).intValue();
    }

    private static Long expireAfter(Document index) {
        Object value = index.get("expireAfterSeconds");
        return value instanceof Number number ? number.longValue() : null;
    }
}
