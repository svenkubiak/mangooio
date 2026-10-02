package io.mangoo.persistence.interfaces;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.result.DeleteResult;
import org.bson.conversions.Bson;

import java.util.List;

public interface Datastore {

    /**
     * Sends a ping to the admin database.
     */
    boolean isHealthy();

    /**
     * Returns null if not found.
     */
    <T> T find(Class<T> clazz, Bson query);

    /**
     * Returns null if not found.
     */
    <T> T findFirst(Class<T> clazz, Bson sort);

    <T> List<T> findAll(Class<T> clazz, Bson query, Bson sort);

    <T> List<T> findAll(Class<T> clazz, Bson query, Bson sort, int limit);

    <T> List<T> findAll(Class<T> clazz);

    <T> List<T> findAll(Class<T> clazz, Bson sort);

    /**
     * Returns -1 if the count failed.
     */
    <T> long countAll(Class<T> clazz, Bson query);

    /**
     * Returns -1 if the count failed.
     */
    <T> long countAll(Class<T> clazz);

    /**
     * Inserts the object or replaces the document with its id, also if that id was set by the application
     * and no document exists yet. Returns the objectId of the stored entity or an empty string if the save failed.
     */
    String save(Object object);

    <T> void saveAll(List<T> objects);

    <T> MongoCollection<T> query(Class<T> clazz);

    @SuppressWarnings("rawtypes")
    MongoCollection query(String collection);

    <T> MongoCollection<T> query(String collection, Class<T> clazz);

    DeleteResult delete(Object object);

    void deleteAll(List<Object> objects);

    void dropDatabase();

    <T> void dropCollection(Class<T> clazz);

    <T> void addIndex(Class<T> clazz, Bson... indexes);

    <T> void addIndex(Class<T> clazz, Bson index, IndexOptions indexOptions);

    <T> void dropIndex(Class<T> clazz, Bson... indexes);

    void dropAllIndexes();

    MongoDatabase getMongoDatabase();

    MongoClient getMongoClient();
}