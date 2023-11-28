package io.mangoo.persistence;

import static com.mongodb.client.model.Filters.eq;
import static java.util.Arrays.asList;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import static org.bson.codecs.pojo.Conventions.ANNOTATION_CONVENTION;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import com.mongodb.client.result.UpdateResult;
import io.mangoo.utils.PersistenceUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.google.inject.Inject;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.result.InsertOneResult;

import io.mangoo.core.Config;
import io.mangoo.enums.Default;
import io.mangoo.enums.Required;

public class DatastoreImpl implements Datastore {
    private static final Logger LOG = LogManager.getLogger(DatastoreImpl.class);
    private Config config;
    private MongoDatabase mongoDatabase;
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

    private void connect() {
       CodecRegistry codecRegistry = MongoClientSettings.getDefaultCodecRegistry();
       PojoCodecProvider pojoCodecProvider = PojoCodecProvider.builder()
            .conventions(asList(ANNOTATION_CONVENTION))
            .automatic(true)
            .build();
                
       MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(getConnectionString()))
                .codecRegistry(fromRegistries(codecRegistry, fromProviders(pojoCodecProvider)))
                .build();
        
       mongoDatabase = MongoClients.create(settings)
               .getDatabase(config.getMongoDbName(prefix));
       
       LOG.info("Created MongoClient connected to {}:{} with credentials = {} on database '{}'",
               config.getMongoHost(prefix),
               config.getMongoPort(prefix),
               config.isMongoAuth(prefix),
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
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public <T> T findById(String id, Class<T> clazz) {
        Objects.requireNonNull(clazz, Required.ID.toString());
        Objects.requireNonNull(id, Required.CLASS.toString());
        
        Object object = null;
        MongoCollection collection = getCollection(clazz).orElse(null);
        if (collection != null) {
            object = collection.find(eq("_id", new ObjectId(id))).first();
        }

        return (T) object;
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public <T> List<T> findAll(Class<T> clazz) {
        Objects.requireNonNull(clazz, Required.CLASS.toString());
        
        List<Object> result = new ArrayList<>();
        MongoCollection collection = getCollection(clazz).orElseGet(null);
        if (collection != null) {
            collection.find().forEach(result::add);
        }
        
        return (List<T>) result;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public <T> long countAll(Class<T> clazz) {
        Objects.requireNonNull(clazz, Required.CLASS.toString());
        
        long count = -1;
        MongoCollection collection = getCollection(clazz).orElseGet(null);
        if (collection != null) {
            count = collection.countDocuments();
        }
        
        return count;
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public String save(Object object) {
        Objects.requireNonNull(object, Required.OBJECT.toString());
        
        MongoCollection collection = getCollection(object.getClass()).orElse(null);
        if (collection != null) {
            BaseEntity baseEntity = (BaseEntity) object;
            ObjectId id = baseEntity.getId();
            if (id == null) {
                InsertOneResult insertResult = collection.insertOne(object);
                return insertResult.getInsertedId().asObjectId().getValue().toString();
            } else {
                UpdateResult updateResult = collection.replaceOne(eq("_id", id), object);
                return id.toString();
            }
        }
        
        return null;
    }
    
    @Override
    public <T> void saveAll(List<T> objects) {
        Objects.requireNonNull(objects, Required.OBJECTS.toString());

        objects.forEach(this::save);
    }
    
    @Override
    @SuppressWarnings("rawtypes")
    public <T> MongoCollection query(Class<T> clazz) {
        Objects.requireNonNull(clazz, Required.CLASS.toString());
        
        return getCollection(clazz).orElse(null);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public MongoCollection query(String collection) {
        Objects.requireNonNull(collection, Required.COLLECTION.toString());

        return mongoDatabase.getCollection(collection);
    }

    @Override
    public void dropDatabase() {
        mongoDatabase.drop();
    }
    
    @Override
    public <T> void dropCollection(Class<T> clazz) {
        Objects.requireNonNull(clazz, Required.CLASS.toString());
        
        getCollection(clazz).ifPresent(MongoCollection::drop);
    }

    @Override
    public <T> void addIndex(Class<T> clazz, Bson... indexes) {
        Objects.requireNonNull(clazz, Required.CLASS.toString());
        Objects.requireNonNull(indexes, Required.INDEXES.toString());
        
        getCollection(clazz).ifPresent(collection -> Stream.of(indexes).forEach(collection::createIndex));
    }

    @Override
    public <T> void addIndex(Class<T> clazz, Bson index, IndexOptions indexOptions) {
        Objects.requireNonNull(clazz, Required.CLASS.toString());
        Objects.requireNonNull(index, Required.INDEX.toString());
        Objects.requireNonNull(indexOptions, Required.INDEX_OPTIONS.toString());
        
        getCollection(clazz).ifPresent(collection -> collection.createIndex(index, indexOptions));
    }

    @Override
    public <T> void dropIndex(Class<T> clazz, Bson... indexes) {
        Objects.requireNonNull(clazz, Required.CLASS.toString());
        Objects.requireNonNull(indexes, Required.INDEXES.toString());

        getCollection(clazz).ifPresent(collection -> Stream.of(indexes).forEach(collection::dropIndex));
    }

    @SuppressWarnings("rawtypes")
    private <T> Optional<MongoCollection> getCollection(Class<T> clazz) {
        Objects.requireNonNull(clazz, Required.CLASS.toString());

        MongoCollection mongoCollection = null;
        String name = PersistenceUtils.getCollectionName(clazz);
        if (StringUtils.isNotBlank(name)) {
            mongoCollection = mongoDatabase.getCollection(name, clazz);
        }

        return Optional.ofNullable(mongoCollection);
    }
}