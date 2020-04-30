package io.mangoo.persistence;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.types.ObjectId;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

import dev.morphia.Morphia;
import io.mangoo.core.Config;

@Singleton
public class Datastore {
    private static final Logger LOG = LogManager.getLogger(Datastore.class);
    private dev.morphia.Datastore datastore; //NOSONAR
    private Morphia morphia;
    private MongoClient mongoClient;
    private Config config;

    @Inject
    public Datastore(Config config) {
        this.config = config;

        connect();
        morphify();
    }

    public dev.morphia.Datastore getDatastore() {
        return this.datastore;
    }
    
    public dev.morphia.Datastore query() {
        return this.datastore;
    }

    public Morphia getMorphia() {
        return this.morphia;
    }

    public MongoClient getMongoClient() {
        return this.mongoClient;
    }

    private void connect() {
        String host = this.config.getMongoHost();
        int port = this.config.getMongoPort();

        String username = this.config.getMongoUsername();
        String password = this.config.getMongoPassword();
        String authdb = this.config.getMongoAuthDB();

        if (this.config.isMongoAuth() && StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password) && StringUtils.isNotBlank(authdb)) {
            MongoClientOptions mongoClientOptions = MongoClientOptions.builder().build();
            MongoCredential mongoCredential = MongoCredential.createScramSha1Credential(username, authdb, password.toCharArray());
            
            this.mongoClient = new MongoClient(new ServerAddress(host, port), mongoCredential, mongoClientOptions);
            LOG.info("Successfully created MongoClient @ {}:{} with authentication", host, port);
        } else {
            this.mongoClient = new MongoClient(host, port);
            LOG.info("Successfully created MongoClient @ {}:{} ***without**** authentication", host, port);
        }
    }

    private void morphify() {
        String packageName = this.config.getMongoPackage();
        String dbName = this.config.getMongoDbName();

        this.morphia = new Morphia().mapPackage(packageName);
        this.datastore = this.morphia.createDatastore(this.mongoClient, dbName);

        LOG.info("Mapped Morphia models of package '" + packageName + "' and created Morphia Datastore with database '" + dbName + "'");
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
    public <T extends Object> T findById(Object id, Class<T> clazz) {
        Preconditions.checkNotNull(clazz, "Tryed to find an object by id, but given class is null");
        Preconditions.checkNotNull(id, "Tryed to find an object by id, but given id is null");
        
        return this.datastore.createQuery(clazz).field("_id").equal((id instanceof ObjectId) ? id : new ObjectId(String.valueOf(id))).first();
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
        
        return this.datastore.find(clazz).find().toList();
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

        this.datastore.delete(this.datastore.createQuery(clazz));
    }

    /**
     * Drops all data in MongoDB on the connected database
     */
    public void dropDatabase() {
        this.datastore.getDatabase().drop();
    }
}
