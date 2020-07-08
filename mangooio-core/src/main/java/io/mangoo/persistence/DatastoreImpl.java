package io.mangoo.persistence;

import java.util.List;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.types.ObjectId;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

import dev.morphia.DeleteOptions;
import dev.morphia.Morphia;
import dev.morphia.query.experimental.filters.Filters;
import io.mangoo.core.Config;
import io.mangoo.enums.Default;
import io.mangoo.enums.Required;

/**
 * 
 * @author svenkubiak
 *
 */
public class DatastoreImpl implements Datastore {
    private static final Logger LOG = LogManager.getLogger(DatastoreImpl.class);
    private dev.morphia.Datastore datastore; //NOSONAR
    private MongoClient mongoClient;
    private Config config;
    private String prefix = Default.PERSISTENCE_PREFIX.toString();
    
    @Inject
    public DatastoreImpl(Config config) {
        this.config = Objects.requireNonNull(config, Required.CONFIG.toString());
        connect();
    }

    public DatastoreImpl(String prefix) {
        this.config = new Config();
        this.prefix = Objects.requireNonNull(prefix, Required.PREFIX.toString());
        this.prefix = Default.PERSISTENCE_PREFIX.toString() + prefix + ".";
        connect();
    }

    @Override
    public dev.morphia.Datastore getDatastore() {
        return this.datastore;
    }
    
    @Override
    public dev.morphia.Datastore query() {
        return this.datastore;
    }

    @Override
    public MongoClient getMongoClient() {
        return this.mongoClient;
    }

    private void connect() {
       this.mongoClient = MongoClients.create(getConnectionString());
       this.datastore = Morphia.createDatastore(this.mongoClient, this.config.getMongoDbName(prefix));
       this.datastore.getMapper().mapPackage(this.config.getMongoPackage(prefix));
 
       LOG.info("Created MongoClient connected to {}:{} with credentials = {}",
               this.config.getMongoHost(prefix),
               this.config.getMongoPort(prefix),
               this.config.isMongoAuth(prefix));
       
       LOG.info("Mapped Morphia models of package '{}' and created Morphia Datastore conntected to database '{}'",
               this.config.getMongoPackage(prefix),
               this.config.getMongoDbName(prefix));
    }
    
    private String getConnectionString() {
        var buffer = new StringBuilder();
        buffer.append("mongodb://");
        
        if (this.config.isMongoAuth(prefix)) {
            buffer
                .append(this.config.getMongoUsername(prefix))
                .append(':')
                .append(this.config.getMongoPassword(prefix))
                .append('@');
        }
        
        buffer
            .append(this.config.getMongoHost(prefix))
            .append(':')
            .append(this.config.getMongoPort(prefix));
        
        if (this.config.isMongoAuth(prefix)) {
            buffer
                .append("/?authSource=")
                .append(this.config.getMongoAuthDB(prefix));
        }
        
        return buffer.toString();
    }

    @Override
    public void ensureIndexes() {
        this.datastore.ensureIndexes();
    }

    @Override
    public void ensureCaps() {
        this.datastore.ensureCaps();
    }

    @Override
    public <T extends Object> T findById(String id, Class<T> clazz) {
        Preconditions.checkNotNull(clazz, "Tryed to find an object by id, but given class is null");
        Preconditions.checkNotNull(id, "Tryed to find an object by id, but given id is null");
        
        return this.datastore.find(clazz).filter(Filters.eq("_id", new ObjectId(id))).first();
    }

    @Override
    public <T extends Object> List<T> findAll(Class<T> clazz) {
        Preconditions.checkNotNull(clazz, "Tryed to get all morphia objects of a given object, but given object is null");
        
        return this.datastore.find(clazz).iterator().toList();
    }

    @Override
    public <T extends Object> long countAll(Class<T> clazz) {
        Preconditions.checkNotNull(clazz, "Tryed to count all a morphia objects of a given object, but given object is null");

        return this.datastore.find(clazz).count();
    }

    @Override
    public void save(Object object) {
        Preconditions.checkNotNull(object, "Tryed to save a morphia object, but a given object is null");

        this.datastore.save(object);
    }

    @Override
    public void delete(Object object) {
        Preconditions.checkNotNull(object, "Tryed to delete a morphia object, but given object is null");

        this.datastore.delete(object);
    }

    @Override
    public <T extends Object> void deleteAll(Class<T> clazz) {
        Preconditions.checkNotNull(clazz, "Tryed to delete list of mapped morphia objects, but given class is null");

        this.datastore.find(clazz).delete(new DeleteOptions().multi(true));
    }

    @Override
    public void dropDatabase() {
        this.datastore.getDatabase().drop();
    }
}
