package io.mangoo.persistence;

import com.google.inject.Inject;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import dev.morphia.DeleteOptions;
import dev.morphia.Morphia;
import dev.morphia.query.experimental.filters.Filters;
import io.mangoo.core.Application;
import io.mangoo.core.Config;
import io.mangoo.enums.Default;
import io.mangoo.enums.Required;
import io.mangoo.persistence.events.DeleteEvent;
import io.mangoo.persistence.events.SaveEvent;
import io.mangoo.services.EventBusService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Objects;

/**
 * 
 * @author svenkubiak
 *
 */
public class DatastoreImpl implements Datastore {
    private static final Logger LOG = LogManager.getLogger(DatastoreImpl.class);
    private dev.morphia.Datastore datastore; //NOSONAR
    private MongoClient mongoClient;
    private EventBusService eventBus;
    private Config config;
    private String prefix = Default.PERSISTENCE_PREFIX.toString();
    
    @Inject
    public DatastoreImpl(Config config, EventBusService eventBus) {
        this.config = Objects.requireNonNull(config, Required.CONFIG.toString());
        this.eventBus = Objects.requireNonNull(eventBus, Required.EVENT_BUS_SERVICE.toString());
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
        return datastore;
    }
    
    @Override
    public dev.morphia.Datastore query() {
        return datastore;
    }

    @Override
    public MongoClient getMongoClient() {
        return mongoClient;
    }

    private void connect() {
       mongoClient = MongoClients.create(getConnectionString());
       datastore = Morphia.createDatastore(mongoClient, config.getMongoDbName(prefix));
       datastore.getMapper().mapPackage(config.getMongoPackage(prefix));
 
       LOG.info("Created MongoClient connected to {}:{} with credentials = {}",
               config.getMongoHost(prefix),
               config.getMongoPort(prefix),
               config.isMongoAuth(prefix));
       
       LOG.info("Mapped Morphia models of package '{}' and created Morphia Datastore conntected to database '{}'",
               config.getMongoPackage(prefix),
               config.getMongoDbName(prefix));
    }
    
    private String getConnectionString() {
        var buffer = new StringBuilder();
        buffer.append("mongodb://");
        
        if (config.isMongoAuth(prefix)) {
            buffer
                .append(config.getMongoUsername(prefix))
                .append(':')
                .append(config.getMongoPassword(prefix))
                .append('@');
        }
        
        buffer
            .append(config.getMongoHost(prefix))
            .append(':')
            .append(config.getMongoPort(prefix));
        
        if (config.isMongoAuth(prefix)) {
            buffer
                .append("/?authSource=")
                .append(config.getMongoAuthDB(prefix));
        }
        
        return buffer.toString();
    }

    @Override
    public void ensureIndexes() {
        datastore.ensureIndexes();
    }

    @Override
    public void ensureCaps() {
        datastore.ensureCaps();
    }

    @Override
    public <T> T findById(String id, Class<T> clazz) {
        Objects.requireNonNull(clazz, "Tryed to find an object by id, but given class is null");
        Objects.requireNonNull(id, "Tryed to find an object by id, but given id is null");
        
        return datastore.find(clazz).filter(Filters.eq("_id", new ObjectId(id))).first();
    }

    @Override
    public <T> List<T> findAll(Class<T> clazz) {
        Objects.requireNonNull(clazz, "Tryed to get all morphia objects of a given object, but given object is null");
        
        return datastore.find(clazz).iterator().toList();
    }

    @Override
    public <T> long countAll(Class<T> clazz) {
        Objects.requireNonNull(clazz, "Tryed to count all a morphia objects of a given object, but given object is null");

        return datastore.find(clazz).count();
    }

    @Override
    public void save(Object object) {
        Objects.requireNonNull(object, "Tryed to save a morphia object, but a given object is null");

        datastore.save(object);
    }
    
    @Override
    public void saveAsync(Object object) {
        Objects.requireNonNull(object, "Tryed to save a morphia object, but a given object is null");
        
        eventBus.publish(Application.getInstance(SaveEvent.class).withObject(object));
    }

    @Override
    public void delete(Object object) {
        Objects.requireNonNull(object, "Tryed to delete a morphia object, but given object is null");

        datastore.delete(object);
    }
    
    @Override
    public void deleteAsync(Object object) {
        Objects.requireNonNull(object, "Tryed to delete a morphia object, but given object is null");

        eventBus.publish(Application.getInstance(DeleteEvent.class).withObject(object));
    }

    @Override
    public <T> void deleteAll(Class<T> clazz) {
        Objects.requireNonNull(clazz, "Tryed to delete list of mapped morphia objects, but given class is null");

        datastore.find(clazz).delete(new DeleteOptions().multi(true));
    }

    @Override
    public void dropDatabase() {
        datastore.getDatabase().drop();
    }
}