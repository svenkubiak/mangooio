package io.mangoo.persistence;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import com.google.inject.Inject;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;

import io.mangoo.core.Config;
import io.mangoo.enums.Default;
import io.mangoo.enums.Required;
import io.mangoo.utils.PersistenceUtils.OperationSubscriber;

public class DatastoreImpl implements Datastore {
    private static final Logger LOG = LogManager.getLogger(DatastoreImpl.class);
    private static final Map<String, String> COLLECTIONS = new ConcurrentHashMap<>(16, 0.9f, 1);
    private final Config config;
    private MongoDatabase database;
    private MongoClient mongoClient;
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
    public MongoClient getMongoClient() {
        return mongoClient;
    }

    private void connect() {
       CodecRegistry codecRegistry = MongoClientSettings.getDefaultCodecRegistry();
       PojoCodecProvider pojoCodecProvider = PojoCodecProvider.builder()
            .automatic(true)
            .build();
                
       MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(getConnectionString()))
                .codecRegistry(fromRegistries(codecRegistry, fromProviders(pojoCodecProvider)))
                .build();
        
       mongoClient = MongoClients.create(settings);
       database = mongoClient.getDatabase("mydatabase");
       
       LOG.info("Created MongoClient connected to {}:{} with credentials = {}",
               config.getMongoHost(prefix),
               config.getMongoPort(prefix),
               config.isMongoAuth(prefix));
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
    public <T> T findById(String id, Class<T> clazz) {
        Objects.requireNonNull(clazz, "Tried to find an object by id, but given class is null");
        Objects.requireNonNull(id, "Tried to find an object by id, but given id is null");
        
        OperationSubscriber s = new OperationSubscriber<>();
        try {
            getCollection(clazz).find().subscribe(s);
            s.await();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        
        return (T) s.getReceived().get(0);
    }

    @Override
    public <T> List<T> findAll(Class<T> clazz) {
        Objects.requireNonNull(clazz, "Tried to get all morphia objects of a given object, but given object is null");
        
        OperationSubscriber s = new OperationSubscriber<>();
        try {
            getCollection(clazz).find().subscribe(s);
            s.await();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        
        return s.getReceived();
    }

    @Override
    public <T> long countAll(Class<T> clazz) {
        Objects.requireNonNull(clazz, "Tried to count all a morphia objects of a given object, but given object is null");
        
        OperationSubscriber s = new OperationSubscriber<>();
        getCollection(clazz).countDocuments().subscribe(s);
        
        try {
            getCollection(clazz).find().subscribe(s);
            s.await();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        
        return (long) s.getReceived().get(0);
    }

    @Override
    public void save(Object object) {
        Objects.requireNonNull(object, "Tried to save a morphia object, but a given object is null");
        
        try {
            OperationSubscriber s = new OperationSubscriber<>();
            getCollection(object.getClass()).insertOne(object).subscribe(s);
            s.await();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public <T> void saveAll(List<T> objects) {
        Objects.requireNonNull(objects, "Tried to save multiple morphia objects, but a given objects are null");
        
        Object object = objects.get(0);
        
        try {
            OperationSubscriber s = new OperationSubscriber<>();
            getCollection(object.getClass()).insertOne(objects).subscribe(s);
            s.await();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(Object object) {
        Objects.requireNonNull(object, "Tried to delete a morphia object, but given object is null");
        //Implement me;
    }

    @Override
    public <T> void deleteAll(Class<T> clazz) {
        Objects.requireNonNull(clazz, "Tried to delete list of mapped morphia objects, but given class is null");
        //Implement me;
        
    }

    @Override
    public void dropDatabase() {
        try {
            OperationSubscriber s = new OperationSubscriber<>();
            database.drop().subscribe(s);
            s.await();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
    
    private <T> MongoCollection getCollection(Class<T> clazz) {
        Objects.requireNonNull(clazz, "clazz can not be null");

        String name = COLLECTIONS.get(clazz.getName());
        
        return database.getCollection(name, clazz);
    }

    @Override
    public void addCollection(String key, String value) {
        Objects.requireNonNull(key, "key of collection can not be null");
        Objects.requireNonNull(value, "value of collection can not be null");
        
        COLLECTIONS.put(key, value);
    }
}