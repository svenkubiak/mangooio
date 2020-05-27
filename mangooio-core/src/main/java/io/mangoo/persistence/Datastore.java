package io.mangoo.persistence;

import java.util.List;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.types.ObjectId;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

import dev.morphia.DeleteOptions;
import dev.morphia.Morphia;
import dev.morphia.query.experimental.filters.Filters;
import io.mangoo.core.Config;
import io.mangoo.enums.Required;

@Singleton
public class Datastore {
    private static final Logger LOG = LogManager.getLogger(Datastore.class);
    private dev.morphia.Datastore datastore; //NOSONAR
    private MongoClient mongoClient;
    private Config config;

    @Inject
    public Datastore(Config config) {
        this.config = Objects.requireNonNull(config, Required.CONFIG.toString());
        connect();
    }

    public dev.morphia.Datastore getDatastore() {
        return this.datastore;
    }
    
    public dev.morphia.Datastore query() {
        return this.datastore;
    }

    public MongoClient getMongoClient() {
        return this.mongoClient;
    }

    private void connect() {
       this.mongoClient = MongoClients.create(getConnectionString());
       this.datastore = Morphia.createDatastore(this.mongoClient, this.config.getMongoDbName());
       this.datastore.getMapper().mapPackage(this.config.getMongoPackage());
 
       LOG.info("Created MongoClient connected to {}:{} with credentials = {}", this.config.getMongoHost(), this.config.getMongoPort(), this.config.isMongoAuth());
       LOG.info("Mapped Morphia models of package '{}' and created Morphia Datastore conntected to database '{}'", this.config.getMongoPackage(), this.config.getMongoDbName());
    }

    private String getConnectionString() {
        var buffer = new StringBuilder();
        buffer.append("mongodb://");
        
        if (this.config.isMongoAuth()) {
            buffer
                .append(this.config.getMongoUsername())
                .append(':')
                .append(this.config.getMongoPassword())
                .append('@');
        }
        
        buffer
            .append(this.config.getMongoHost())
            .append(':')
            .append(this.config.getMongoPort());
        
        if (this.config.isMongoAuth()) {
            buffer
                .append("/?authSource=")
                .append(this.config.getMongoAuthDB());
        }
        
        return buffer.toString();
    }

    /**
     * Ensures (creating if necessary) the indexes found during class mapping (using @Indexed, @Indexes)
     */
    public void ensureIndexes() {
        this.datastore.ensureIndexes();
    }

    /**
     * Ensure capped DBCollections for Entity(s)
     */
    public void ensureCaps() {
        this.datastore.ensureCaps();
    }

    /**
     * Retrieves a mapped Morphia object from MongoDB. If the id is not of
     * type ObjectId, it will be converted to ObjectId
     *
     * @param id The id of the object
     * @param clazz The mapped Morphia class
     * @param <T> JavaDoc requires this - please ignore
     *
     * @return The requested class from MongoDB or null if none found
     */
    public <T extends Object> T findById(String id, Class<T> clazz) {
        Preconditions.checkNotNull(clazz, "Tryed to find an object by id, but given class is null");
        Preconditions.checkNotNull(id, "Tryed to find an object by id, but given id is null");
        
        return this.datastore.find(clazz).filter(Filters.eq("_id", new ObjectId(id))).first();
    }

    /**
     * Retrieves a list of mapped Morphia objects from MongoDB
     *
     * @param clazz The mapped Morphia class
     * @param <T> JavaDoc requires this - please ignore
     * 
     * @return A list of mapped Morphia objects or an empty list if none found
     */
    public <T extends Object> List<T> findAll(Class<T> clazz) {
        Preconditions.checkNotNull(clazz, "Tryed to get all morphia objects of a given object, but given object is null");
        
        return this.datastore.find(clazz).iterator().toList();
    }

    /**
     * Counts all objected of a mapped Morphia class
     *
     * @param clazz The mapped Morphia class
     * @param <T> JavaDoc requires this - please ignore
     *      
     * @return The number of objects in MongoDB
     */
    public <T extends Object> long countAll(Class<T> clazz) {
        Preconditions.checkNotNull(clazz, "Tryed to count all a morphia objects of a given object, but given object is null");

        return this.datastore.find(clazz).count();
    }

    /**
     * Saves a mapped Morphia object to MongoDB
     *
     * @param object The object to save
     */
    public void save(Object object) {
        Preconditions.checkNotNull(object, "Tryed to save a morphia object, but a given object is null");

        this.datastore.save(object);
    }

    /**
     * Deletes a mapped Morphia object in MongoDB
     *
     * @param object The object to delete
     */
    public void delete(Object object) {
        Preconditions.checkNotNull(object, "Tryed to delete a morphia object, but given object is null");

        this.datastore.delete(object);
    }

    /**
     * Deletes all mapped Morphia objects of a given class

     * @param <T> JavaDoc requires this - please ignore
     * @param clazz The mapped Morphia class
     */
    public <T extends Object> void deleteAll(Class<T> clazz) {
        Preconditions.checkNotNull(clazz, "Tryed to delete list of mapped morphia objects, but given class is null");

        this.datastore.find(clazz).delete(new DeleteOptions().multi(true));
    }

    /**
     * Drops all data in MongoDB on the connected database
     */
    public void dropDatabase() {
        this.datastore.getDatabase().drop();
    }
}
