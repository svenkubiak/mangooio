package io.mangoo.persistence;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import io.mangoo.constants.Default;
import io.mangoo.constants.NotNull;
import io.mangoo.core.Config;
import io.mangoo.persistence.interfaces.BaseEntity;
import io.mangoo.persistence.interfaces.Datastore;
import io.mangoo.utils.PersistenceUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static com.mongodb.client.model.Filters.eq;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import static org.bson.codecs.pojo.Conventions.ANNOTATION_CONVENTION;

public class DatastoreImpl implements Datastore {
    private static final Logger LOG = LogManager.getLogger(DatastoreImpl.class);
    private final Config config;
    private MongoDatabase mongoDatabase;
    private String prefix = Default.PERSISTENCE_PREFIX;
    
    @Inject
    public DatastoreImpl(Config config) {
        this.config = Objects.requireNonNull(config, NotNull.CONFIG);
        connect();
    }

    public DatastoreImpl(String prefix) {
        this.config = new Config();
        this.prefix = Objects.requireNonNull(prefix, NotNull.PREFIX);
        this.prefix = Default.PERSISTENCE_PREFIX + prefix + ".";
        connect();
    }

    private void connect() {
       if (config.isPersistenceEnabled()) {
           var codecRegistry = MongoClientSettings.getDefaultCodecRegistry();
           var pojoCodecProvider = PojoCodecProvider.builder()
                   .conventions(List.of(ANNOTATION_CONVENTION))
                   .automatic(true)
                   .build();

           MongoClientSettings settings = MongoClientSettings.builder()
                   .applyConnectionString(new ConnectionString(getConnectionString()))
                   .codecRegistry(fromRegistries(codecRegistry, fromProviders(pojoCodecProvider)))
                   .build();

           mongoDatabase = MongoClients
                   .create(settings) //NOSONAR
                   .getDatabase(config.getMongoDbName(prefix));

           LOG.info("Created MongoClient connected to {}:{} with credentials = {} on database '{}'",
                   config.getMongoHost(prefix),
                   config.getMongoPort(prefix),
                   config.isMongoAuth(prefix),
                   config.getMongoDbName(prefix));
       }
    }
    
    private String getConnectionString() {
        var buffer = new StringBuilder();
        buffer.append("mongodb://");
        
        if (Boolean.TRUE.equals(config.isMongoAuth(prefix))) {
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
        
        if (Boolean.TRUE.equals(config.isMongoAuth(prefix))) {
            buffer
                .append("/?authSource=")
                .append(config.getMongoAuthDB(prefix));
        }
        
        return buffer.toString();
    }

    @Override
    @SuppressWarnings({"unchecked" })
    public <T> T find(Class<T> clazz, Bson query) {
        Objects.requireNonNull(clazz, NotNull.CLASS);
        Objects.requireNonNull(query, NotNull.KEY);

        return (T) query(clazz)
                .find(query)
                .first();
    }

    @Override
    @SuppressWarnings({"unchecked" })
    public <T> T findFirst(Class<T> clazz, Bson sort) {
        Objects.requireNonNull(clazz, NotNull.CLASS);
        Objects.requireNonNull(sort, NotNull.SORT);

        return (T) query(clazz)
                .find()
                .sort(sort)
                .first();
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes", "DataFlowIssue"})
    public <T> List<T> findAll(Class<T> clazz, Bson query, Bson sort) {
        Objects.requireNonNull(clazz, NotNull.CLASS);
        Objects.requireNonNull(query, NotNull.KEY);
        Objects.requireNonNull(sort, NotNull.SORT);

        List<Object> result = new ArrayList<>();
        MongoCollection collection = getCollection(clazz).orElseGet(null); //NOSONAR
        if (collection != null) {
            collection.find(query).sort(sort).forEach(result::add);
        }

        return (List<T>) result;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes", "DataFlowIssue"})
    public <T> List<T> findAll(Class<T> clazz, Bson query, Bson sort, int limit) {
        Objects.requireNonNull(clazz, NotNull.CLASS);
        Objects.requireNonNull(query, NotNull.KEY);
        Objects.requireNonNull(sort, NotNull.SORT);
        Preconditions.checkArgument(limit > 0, "limit must be greater than 0");

        List<Object> result = new ArrayList<>();
        MongoCollection collection = getCollection(clazz).orElseGet(null); //NOSONAR
        if (collection != null) {
            collection.find(query).sort(sort).limit(limit).forEach(result::add);
        }

        return (List<T>) result;
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked", "DataFlowIssue"})
    public <T> List<T> findAll(Class<T> clazz) {
        Objects.requireNonNull(clazz, NotNull.CLASS);
        
        List<Object> result = new ArrayList<>();
        MongoCollection collection = getCollection(clazz).orElseGet(null); //NOSONAR
        if (collection != null) {
            collection.find().forEach(result::add);
        }
        
        return (List<T>) result;
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked", "DataFlowIssue"})
    public <T> List<T> findAll(Class<T> clazz, Bson sort) {
        Objects.requireNonNull(clazz, NotNull.CLASS);
        Objects.requireNonNull(sort, NotNull.SORT);

        List<Object> result = new ArrayList<>();
        MongoCollection collection = getCollection(clazz).orElseGet(null); //NOSONAR
        if (collection != null) {
            collection.find().sort(sort).forEach(result::add);
        }

        return (List<T>) result;
    }

    @Override
    @SuppressWarnings({"rawtypes", "DataFlowIssue"})
    public <T> long countAll(Class<T> clazz, Bson query) {
        Objects.requireNonNull(clazz, NotNull.CLASS);
        Objects.requireNonNull(clazz, NotNull.QUERY);
        
        long count = -1;
        MongoCollection collection = getCollection(clazz).orElseGet(null); //NOSONAR
        if (collection != null) {
            count = collection.countDocuments(query);
        }
        
        return count;
    }

    @Override
    @SuppressWarnings({"rawtypes", "DataFlowIssue"})
    public <T> long countAll(Class<T> clazz) {
        Objects.requireNonNull(clazz, NotNull.CLASS);

        long count = -1;
        MongoCollection collection = getCollection(clazz).orElseGet(null); //NOSONAR
        if (collection != null) {
            count = collection.countDocuments();
        }

        return count;
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public String save(Object object) {
        Objects.requireNonNull(object, NotNull.OBJECT);
        
        MongoCollection collection = getCollection(object.getClass()).orElse(null);
        if (collection != null) {
            var baseEntity = (BaseEntity) object;
            ObjectId id = baseEntity.getId();
            if (id == null) {
                InsertOneResult insertResult = collection.insertOne(object);
                if (insertResult.getInsertedId() != null) {
                    return insertResult.getInsertedId().asObjectId().getValue().toString();
                }
            } else {
                collection.replaceOne(eq("_id", id), object);
                return id.toString();
            }
        }
        
        return "";
    }
    
    @Override
    public <T> void saveAll(List<T> objects) {
        Objects.requireNonNull(objects, NotNull.OBJECTS);

        objects.forEach(this::save);
    }
    
    @Override
    @SuppressWarnings("rawtypes")
    public <T> MongoCollection query(Class<T> clazz) {
        Objects.requireNonNull(clazz, NotNull.CLASS);
        
        return getCollection(clazz).orElse(null);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public MongoCollection query(String collection) {
        Objects.requireNonNull(collection, NotNull.COLLECTION);

        return mongoDatabase.getCollection(collection);
    }

    @Override
    public DeleteResult delete(Object object) {
        Objects.requireNonNull(object, NotNull.OBJECT);
        var baseEntity = (BaseEntity) object;

        return query(object.getClass()).deleteOne(eq("_id", baseEntity.getId()));
    }

    @Override
    public void deleteAll(List<Object> objects) {
        Objects.requireNonNull(objects, NotNull.OBJECTS);
        objects.forEach(this::delete);
    }

    @Override
    public void dropDatabase() {
        mongoDatabase.drop();
    }
    
    @Override
    public <T> void dropCollection(Class<T> clazz) {
        Objects.requireNonNull(clazz, NotNull.CLASS);
        
        getCollection(clazz).ifPresent(MongoCollection::drop);
    }

    @Override
    public <T> void addIndex(Class<T> clazz, Bson... indexes) {
        Objects.requireNonNull(clazz, NotNull.CLASS);
        Objects.requireNonNull(indexes, NotNull.INDEXES);
        
        getCollection(clazz).ifPresent(collection -> Stream.of(indexes).forEach(collection::createIndex));
    }

    @Override
    public <T> void addIndex(Class<T> clazz, Bson index, IndexOptions indexOptions) {
        Objects.requireNonNull(clazz, NotNull.CLASS);
        Objects.requireNonNull(index, NotNull.INDEX);
        Objects.requireNonNull(indexOptions, NotNull.INDEX_OPTIONS);
        
        getCollection(clazz).ifPresent(collection -> collection.createIndex(index, indexOptions));
    }

    @Override
    public <T> void dropIndex(Class<T> clazz, Bson... indexes) {
        Objects.requireNonNull(clazz, NotNull.CLASS);
        Objects.requireNonNull(indexes, NotNull.INDEXES);

        getCollection(clazz).ifPresent(collection -> Stream.of(indexes).forEach(collection::dropIndex));
    }

    @Override
    public void dropAllIndexes() {
        mongoDatabase.listCollectionNames().forEach(c -> {
            mongoDatabase.getCollection(c).dropIndexes();
        });
    }

    @SuppressWarnings("rawtypes")
    private <T> Optional<MongoCollection> getCollection(Class<T> clazz) {
        Objects.requireNonNull(clazz, NotNull.CLASS);

        MongoCollection mongoCollection = null;
        String name = PersistenceUtils.getCollectionName(clazz);
        if (StringUtils.isNotBlank(name)) {
            mongoCollection = mongoDatabase.getCollection(name, clazz);
        }

        return Optional.ofNullable(mongoCollection);
    }
}