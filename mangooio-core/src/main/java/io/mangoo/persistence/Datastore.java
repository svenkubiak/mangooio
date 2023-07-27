package io.mangoo.persistence;

import java.util.List;

import com.mongodb.client.MongoClient;

public interface Datastore {

    dev.morphia.Datastore getDatastore();

    dev.morphia.Datastore query();

    MongoClient getMongoClient();

    /**
     * Ensures (creating if necessary) the indexes found during class mapping (using @Indexed, @Indexes)
     * @deprecated Will be removed with mangoo I/O 8.0.0 as Morphia 3.0 is using a file based config
     */
    @Deprecated(forRemoval = true, since = "7.16.0")
    void ensureIndexes();

    /**
     * Ensure capped DBCollections for Entity(s)
     * @deprecated Will be removed with mangoo I/O 8.0.0 as Morphia 3.0 is using a file based config
     */
    @Deprecated(forRemoval = true, since = "7.16.0")
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
    <T> void deleteAll(Class<T> clazz);

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

    /**
     * Saves a list of Morphia objects to MongoDB
     * 
     * @param <T> Type
     * @param objects The list of objects
     */
    <T> void saveAll(List<T> objects);

    /**
     * Saves a list of Morphia objects to MongoDB
     * 
     * @param <T> Type
     * @param objects The list of objects
     */
	<T> void saveAllAsync(List<T> objects);

	/**
	 * Deletes a list of Morphia objects to MongoDB
	 * 
	 * @param <T> Type
	 * @param clazz The list of classes
	 */
	<T> void deleteAllAsync(List<Class<T>> clazz);
}