package io.mangoo.persistence;

import java.util.List;

import org.bson.conversions.Bson;

import com.mongodb.client.MongoCollection;

public interface Datastore {

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
     * Retrieves a MongoDB entity from the database
     *
     * @param clazz The corresponding class
     * @param <T> Type
     * 
     * @return A list of MongoDB objects or an empty list if none found
     */
    <T> List<T> findAll(Class<T> clazz);

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
     * Adds a collection to the datastore
     * 
     * @param key The key of the collection
     * @param value The value/name of the collection
     */
    void addCollection(String key, String value);

    /**
     * Returns a collection to execute a query against the MongoDB database
     * 
     * @param clazz The entity class to query against
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
     * Adds an index to the collection in the MondoDB database
     * 
     * @param <T> Type
     * @param clazz The class corresponding with the collection
     * @param indexes One or multiple Indexes (e.g. Indexes.ascedning("foo"))
     */
    <T> void addIndex(Class<T> clazz, Bson... indexes);
}