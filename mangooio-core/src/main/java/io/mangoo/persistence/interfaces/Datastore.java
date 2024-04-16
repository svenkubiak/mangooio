package io.mangoo.persistence.interfaces;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.result.DeleteResult;
import org.bson.conversions.Bson;

import java.util.List;

public interface Datastore {

    /**
     * Retrieves exactly one MongoDB entity matching the given query from
     * the provided key and value pair
     *
     * @param key The key for the query
     * @param value The value for the query
     * @param clazz The corresponding class
     * @param <T> Type
     *
     * @return The requested object from MongoDB or null if not found
     */
    <T> T findBy(String key, String value, Class<T> clazz);

    /**
     * Retrieves exactly one MongoDB entity from the database queried
     * by the given Bson query
     *
     * @param query The bson query (e.g. eq(key, value))
     * @param clazz The corresponding class
     * @param <T> Type
     *
     * @return The requested object from MongoDB or null if not found
     */
    <T> T findBy(Bson query, Class<T> clazz);

    /**
     * Retrieves multiple MongoDB entities matching the given query from
     * the provided key and value pair
     *
     * @param key The key for the query
     * @param value The value for the query
     * @param clazz The corresponding class
     * @param <T> Type
     *
     * @return The requested object from MongoDB or null if not found
     */
    <T> List<T> findAllBy(String key, String value, Class<T> clazz);

    /**
     * Retrieves multiple MongoDB entities matching the given query from
     * the provided bson query
     *
     * @param query The bson query (e.g. eq(key, value))
     * @param clazz The corresponding class
     * @param <T> Type
     *
     * @return The requested object from MongoDB or null if not found
     */
    <T> List<T> findAllBy(Bson query, Class<T> clazz);

    /**
     * Retrieves a MongoDB entity from the database
     *
     * @param id The id of the object
     * @param clazz The corresponding class
     * @param <T> Type
     *
     * @return The requested object from MongoDB or null if not found
     */
    <T> T findById(String id, Class<T> clazz);

    /**
     * Retrieves the first MongoDB entity from the database sorted
     * by the given Bson sort
     *
     * @param clazz The corresponding class
     * @param sort The sort to use
     * @param <T> Type
     *
     * @return The requested object from MongoDB or null if not found
     */
    <T> T findFirst(Class<T> clazz, Bson sort);

    /**
     * Retrieves MongoDB entities from the database
     *
     * @param clazz The corresponding class
     * @param <T> Type
     * 
     * @return A list of MongoDB objects or an empty list if none found
     */
    <T> List<T> findAll(Class<T> clazz);

    /**
     * Retrieves a MongoDB entity from the database
     *
     * @param clazz The corresponding class
     * @param sort The sort to use
     * @param <T> Type
     *
     * @return A list of MongoDB objects or an empty list if none found
     */
    <T> List<T> findAll(Class<T> clazz, Bson sort);

    /**
     * Counts all objected of a MongoDB entity based on the given query
     *
     * @param clazz The corresponding class
     * @param <T> Type
     *
     * @return The number of objects in MongoDB or -1 if count failed
     */
    <T> long count(Class<T> clazz, Bson query);

    /**
     * Counts all objected of a MongoDB entity
     *
     * @param clazz The corresponding class
     * @param <T> Type
     *      
     * @return The number of objects in MongoDB or -1 if count failed
     */
    <T> long countAll(Class<T> clazz);

    /**
     * Saves am entity to MongoDB
     *
     * @param object The object to save
     * @return The objectId of the stored entity or null if save failed
     */
    String save(Object object);

    /**
     * Returns a collection to execute a query against the MongoDB database
     *
     * @param collection The name of the collection
     * @return MongoCollection
     */
    @SuppressWarnings("rawtypes")
    MongoCollection query(String collection);

    /**
     * Deletes one object from the database
     *
     * @param object The object to delete
     * @return The DeleteResult
     */
    DeleteResult delete(Object object);

    /**
     * Deletes all given objects from the database
     *
     * @param objects The objects to delete
     */
    void deleteAll(List<Object> objects);

    /**
     * Drops all data in MongoDB on the connected database
     */
    void dropDatabase();

    /**
     * Saves a list of MongoDB entitiesB
     * 
     * @param objects The list of objects
     * @param <T> Type
     */
    <T> void saveAll(List<T> objects);

    /**
     * Returns a collection to execute a query against the MongoDB database
     * 
     * @param clazz The POJO entity class to query against
     * @param <T> Type
     * @return MongoCollection
     */
    @SuppressWarnings("rawtypes")
    <T> MongoCollection query(Class<T> clazz);

    /**
     * Drops a specific collection specified by a given class
     * 
     * @param clazz The class corresponding with the collection
     */
    <T> void dropCollection(Class<T> clazz);

    /**
     * Adds an index to the collection in the MongoDB database
     * 
     * @param <T> Type
     * @param clazz The class corresponding with the collection
     * @param indexes One or multiple Indexes (e.g. Indexes.ascending("foo")) to add
     */
    <T> void addIndex(Class<T> clazz, Bson... indexes);
    
    /**
     * Adds an index to the collection in the MongoDB database
     * 
     * @param <T> Type
     * @param clazz The class corresponding with the collection
     * @param index The Index to set
     * @param indexOptions The IndexOptions to set
     */
    <T> void addIndex(Class<T> clazz, Bson index, IndexOptions indexOptions);

    /**
     * Removes an index from the collection in the MongoDB database
     *
     * @param <T> Type
     * @param clazz The class corresponding with the collection
     * @param indexes One or multiple Indexes (e.g. Indexes.ascending("foo")) to remove
     */
    <T> void dropIndex(Class<T> clazz, Bson... indexes);
}