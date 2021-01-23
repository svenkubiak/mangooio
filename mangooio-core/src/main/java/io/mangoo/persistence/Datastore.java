package io.mangoo.persistence;

import java.util.List;

import com.mongodb.client.MongoClient;

/**
 * 
 * @author svenkubiak
 *
 */
public interface Datastore {

    dev.morphia.Datastore getDatastore();

    dev.morphia.Datastore query();

    MongoClient getMongoClient();

    /**
     * Ensures (creating if necessary) the indexes found during class mapping (using @Indexed, @Indexes)
     */
    void ensureIndexes();

    /**
     * Ensure capped DBCollections for Entity(s)
     */
    void ensureCaps();

    /**
     * Retrieves a mapped Morphia object from MongoDB.
     *
     * @param id The mongodb id of the object
     * @param clazz The mapped Morphia class
     * @param <T> JavaDoc requires this - please ignore
     *
     * @return The requested class from MongoDB or null if none found
     */
    <T extends Object> T findById(String id, Class<T> clazz);

    /**
     * Retrieves a list of mapped Morphia objects from MongoDB
     *
     * @param clazz The mapped Morphia class
     * @param <T> JavaDoc requires this - please ignore
     * 
     * @return A list of mapped Morphia objects or an empty list if none found
     */
    <T extends Object> List<T> findAll(Class<T> clazz);

    /**
     * Counts all objected of a mapped Morphia class
     *
     * @param clazz The mapped Morphia class
     * @param <T> JavaDoc requires this - please ignore
     *      
     * @return The number of objects in MongoDB
     */
    <T extends Object> long countAll(Class<T> clazz);

    /**
     * Saves a mapped Morphia object to MongoDB
     *
     * @param object The object to save
     */
    void save(Object object);

    /**
     * Deletes a mapped Morphia object in MongoDB
     *
     * @param object The object to delete
     */
    void delete(Object object);

    /**
     * Deletes all mapped Morphia objects of a given class
    
     * @param <T> JavaDoc requires this - please ignore
     * @param clazz The mapped Morphia class
     */
    <T extends Object> void deleteAll(Class<T> clazz);

    /**
     * Drops all data in MongoDB on the connected database
     */
    void dropDatabase();
    
    /**
     * Saves a mapped Morphia object to MongoDB asynchronously
     *
     * @param object The object to save
     */
    void saveAsync(Object object);

    /**
     * Deletes a mapped Morphia object to MongoDB asynchronously
     *
     * @param object The object to delete
     */
    void deleteAsync(Object object);
}