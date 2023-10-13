package io.mangoo.persistence;

import java.util.List;

import org.bson.conversions.Bson;

import com.mongodb.client.MongoCollection;

public interface Datastore {

    /**
     * Retrieves a mapped Morphia object from MongoDB.
     *
     * @param id The mongodb id of the object
     * @param clazz The mapped Morphia class
     * @param <T> JavaDoc requires this - please ignore
     *
     * @return The requested class from MongoDB or null if none found
     */
    <T> T findById(String id, Class<T> clazz);

    /**
     * Retrieves a list of mapped Morphia objects from MongoDB
     *
     * @param clazz The mapped Morphia class
     * @param <T> JavaDoc requires this - please ignore
     * 
     * @return A list of mapped Morphia objects or an empty list if none found
     */
    <T> List<T> findAll(Class<T> clazz);

    /**
     * Counts all objected of a mapped Morphia class
     *
     * @param clazz The mapped Morphia class
     * @param <T> JavaDoc requires this - please ignore
     *      
     * @return The number of objects in MongoDB
     */
    <T> long countAll(Class<T> clazz);

    /**
     * Saves a mapped Morphia object to MongoDB
     *
     * @param object The object to save
     */
    String save(Object object);

    /**
     * Drops all data in MongoDB on the connected database
     */
    void dropDatabase();

    /**
     * Saves a list of Morphia objects to MongoDB
     * 
     * @param <T> Type
     * @param objects The list of objects
     */
    <T> void saveAll(List<T> objects);

    /**
     * Adds a collection to the datastore
     * 
     * @param key The key of the collection
     * @param value The value/name of the collection
     */
    void addCollection(String key, String value);

    /**
     * Returns a collection to execute a query against the MongoDB database
     * 
     * @param <T> Type
     * @param clazz The clazz to query against
     * @return MongoCollection
     */
    @SuppressWarnings("rawtypes")
    <T> MongoCollection query(Class<T> clazz);

    /**
     * Adds a collection to the MongoDB datastore
     * 
     * @param <T> Type
     * @param clazz The clazz to query against
     * @return MongoCollection
     */
    @SuppressWarnings("rawtypes")
    <T> MongoCollection getCollection(Class<T> clazz);

    /**
     * Drops a collection specified by a given class
     * 
     * @param clazz The class corresponding with the collection
     */
    <T> void dropCollection(Class<T> clazz);

    /**
     * Adds an index to the collection in the MondoDB database
     * @param <T> Type
     * @param clazz The class corresponding with the collection
     * @param indexes One or multiple Indexes (e.g. Indexes.ascedning("foo"))
     */
    <T> void addIndex(Class<T> clazz, Bson... indexes);
}