package io.mangoo.persistence;

import com.mongodb.client.model.*;
import io.mangoo.TestExtension;
import io.mangoo.core.Application;
import io.mangoo.interfaces.persistence.Datastore;
import io.mangoo.utils.CommonUtils;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@ExtendWith({TestExtension.class})
class MongoSchemaTest {

    private static Datastore datastore() {
        return Application.getInstance(Datastore.class);
    }

    private static String collection() {
        return "test_schema_" + CommonUtils.uuidV6().replace("-", "");
    }

    private static boolean exists(String collection) {
        return datastore().getMongoDatabase().listCollectionNames().into(new java.util.ArrayList<>()).contains(collection);
    }

    private static MongoStore store(String collection) {
        return new MongoStore(datastore(), collection) {
        };
    }

    @Test
    void testValidateListsAMissingTtlIndex() {
        //given
        String collection = collection();

        //when
        var missing = store(collection).validate();

        //then
        assertThat(missing, contains("db.getCollection(\"" + collection + "\").createIndex({\"expireAt\": 1}, {\"expireAfterSeconds\": 0})"));
        assertThat(exists(collection), equalTo(false));
    }

    @Test
    void testValidateAcceptsThePreparedTtlIndex() {
        //given
        var store = store(collection());

        //when
        store.prepare();

        //then
        assertThat(store.validate(), empty());
    }

    @Test
    void testValidateAcceptsATtlIndexCreatedWithADoubleDirection() {
        //given
        String collection = collection();
        datastore().getMongoDatabase().getCollection(collection)
                .createIndex(new Document("expireAt", 1.0), new IndexOptions().expireAfter(0L, TimeUnit.SECONDS));

        //then
        assertThat(store(collection).validate(), empty());
    }

    @Test
    void testValidateRejectsATtlIndexWithAnotherExpiry() {
        //given
        String collection = collection();
        datastore().getMongoDatabase().getCollection(collection)
                .createIndex(Indexes.ascending("expireAt"), new IndexOptions().expireAfter(60L, TimeUnit.SECONDS));

        //then
        assertThat(store(collection).validate(), hasSize(1));
    }

    @Test
    void testHasIndexComparesUniquenessAndCollation() {
        //given
        var collection = datastore().getMongoDatabase().getCollection(collection());
        var collation = Collation.builder().locale("en").collationStrength(CollationStrength.SECONDARY).build();
        collection.createIndex(Indexes.ascending("email"), new IndexOptions().unique(true).collation(collation));

        //then
        assertThat(MongoSchema.hasIndex(collection, Indexes.ascending("email"), new IndexOptions().unique(true).collation(collation)), equalTo(true));
        assertThat(MongoSchema.hasIndex(collection, Indexes.ascending("email"), new IndexOptions().unique(true)), equalTo(false));
        assertThat(MongoSchema.hasIndex(collection, Indexes.ascending("email"), new IndexOptions().collation(collation)), equalTo(false));
        assertThat(MongoSchema.hasIndex(collection, Indexes.descending("email"), new IndexOptions().unique(true).collation(collation)), equalTo(false));
    }

    @Test
    void testCreateIndexCommand() {
        //given
        var collation = Collation.builder().locale("en").collationStrength(CollationStrength.SECONDARY).build();

        //when
        String command = MongoSchema.createIndexCommand("people", Indexes.ascending("email"), new IndexOptions().unique(true).collation(collation));

        //then
        assertThat(command, equalTo("db.getCollection(\"people\").createIndex({\"email\": 1}, {\"unique\": true, \"collation\": {\"locale\": \"en\", \"strength\": 2}})"));
    }

    @Test
    void testBrokerValidateListsAMissingCollectionWithoutCreatingIt() {
        //given
        String collection = collection();

        //when
        var missing = new MongoMessageBroker(datastore(), collection, "first", false).validate();

        //then
        assertThat(missing, contains("db.createCollection(\"" + collection + "\", {\"capped\": true, \"size\": 16777216})"));
        assertThat(exists(collection), equalTo(false));
    }

    @Test
    void testBrokerValidateRejectsACollectionThatIsNotCapped() {
        //given
        String collection = collection();
        datastore().getMongoDatabase().createCollection(collection);

        //when
        var missing = new MongoMessageBroker(datastore(), collection, "first", false).validate();

        //then
        assertThat(missing, contains(containsString("not capped")));
    }

    @Test
    void testBrokerStartSeedsAnExistingCappedCollection() {
        //given
        String collection = collection();
        datastore().getMongoDatabase().createCollection(collection, new CreateCollectionOptions().capped(true).sizeInBytes(16L * 1024 * 1024));
        var broker = new MongoMessageBroker(datastore(), collection, "first", false);

        try {
            //when
            var missing = broker.validate();
            broker.start();

            //then
            assertThat(missing, empty());
            assertThat(datastore().getMongoDatabase().getCollection(collection).countDocuments(), equalTo(1L));
        } finally {
            broker.stop();
        }
    }
}
