package io.mangoo.persistence;

import com.mongodb.CursorType;
import com.mongodb.MongoCommandException;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.CreateCollectionOptions;
import com.mongodb.client.model.Filters;
import io.mangoo.async.InMemoryMessageBroker;
import io.mangoo.constants.Required;
import io.mangoo.core.Application;
import io.mangoo.interfaces.MessageBroker;
import io.mangoo.persistence.interfaces.Datastore;
import io.mangoo.utils.Argument;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * MessageBroker shared by all instances through the capped collection mangooio_messages. A published message is
 * delivered to the local subscribers directly; one thread per instance tails the collection and delivers the messages
 * of the other instances. Works on a standalone MongoDB, change streams would require a replica set. Messages published
 * while the thread is disconnected are lost, there is no replay after a restart.
 */
@Singleton
public class MongoMessageBroker extends MongoStore implements MessageBroker {
    private static final Logger LOG = LogManager.getLogger(MongoMessageBroker.class);
    private static final String COLLECTION = "mangooio_messages";
    private static final long CAPPED_SIZE = 16L * 1024 * 1024;
    private static final long MIN_BACKOFF = 1000;
    private static final long MAX_BACKOFF = 30000;
    private static final int NAMESPACE_EXISTS = 48;
    private static final String SEED = "mangooio-seed";
    private static final String TOPIC = "topic";
    private static final String MESSAGE = "message";
    private static final String INSTANCE = "instance";
    private final Map<String, List<Consumer<String>>> subscribers = new ConcurrentHashMap<>();
    private final Datastore datastore;
    private final String name;
    private final String instanceId;
    private volatile boolean running;
    private volatile Object marker;
    private Thread thread;

    @Inject
    public MongoMessageBroker(Datastore datastore) {
        this(datastore, COLLECTION, Application.getInstanceId());
    }

    MongoMessageBroker(Datastore datastore, String name, String instanceId) {
        super(datastore, name);
        this.datastore = datastore;
        this.name = name;
        this.instanceId = Argument.requireNonBlank(instanceId, "instanceId can not be null or blank");
    }

    @Override
    public void publish(String topic, String message) {
        Argument.requireNonBlank(topic, Required.TOPIC);
        Objects.requireNonNull(message, Required.MESSAGE);

        InMemoryMessageBroker.deliver(subscribers, topic, message);
        try {
            collection().insertOne(new Document(TOPIC, topic).append(MESSAGE, message).append(INSTANCE, instanceId));
        } catch (MongoException | IllegalStateException e) {
            LOG.error("Failed to publish a message of topic '{}', only the subscribers of this instance received it", topic, e);
        }
    }

    @Override
    public void subscribe(String topic, Consumer<String> consumer) {
        Argument.requireNonBlank(topic, Required.TOPIC);
        Objects.requireNonNull(consumer, Required.CONSUMER);

        subscribers.computeIfAbsent(topic, key -> new CopyOnWriteArrayList<>()).add(consumer);
    }

    /** Creates the capped collection if it is missing; fails if a collection of that name exists that is not capped. */
    @Override
    public void prepare() {
        var database = datastore.getMongoDatabase();
        if (!exists()) {
            try {
                database.createCollection(name, new CreateCollectionOptions().capped(true).sizeInBytes(CAPPED_SIZE));
            } catch (MongoCommandException e) {
                // Another instance created it at the same time
                if (e.getErrorCode() != NAMESPACE_EXISTS) {
                    throw e;
                }
            }
        }

        Document options = database.listCollections().filter(Filters.eq("name", name)).first();
        boolean capped = options != null && Boolean.TRUE.equals(options.get("options", Document.class).getBoolean("capped"));
        if (!capped) {
            throw new IllegalStateException("Collection " + name + " exists but is not capped, drop it to let mangoo I/O create it");
        }

        // A tailable cursor on an empty capped collection is closed immediately
        if (collection().estimatedDocumentCount() == 0) {
            collection().insertOne(new Document(TOPIC, SEED).append(INSTANCE, instanceId));
        }
    }

    private boolean exists() {
        return datastore.getMongoDatabase().listCollectionNames().into(new ArrayList<>()).contains(name);
    }

    /** Starts tailing at the end of the collection, so messages published before the start are not delivered. */
    public synchronized void start() {
        if (running) {
            return;
        }

        Document last = collection().find().sort(new Document("$natural", -1)).limit(1).first();
        marker = last == null ? null : last.get(ID);
        running = true;
        thread = Thread.ofPlatform().daemon().name("mangooio-message-broker").start(this::tail);
    }

    public synchronized void stop() {
        running = false;
        if (thread != null) {
            thread.interrupt();
            try {
                thread.join(TimeUnit.SECONDS.toMillis(5));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            thread = null;
        }
    }

    // _id values of different processes are not ordered, so the position is found by skipping in natural order up to the
    // last seen document instead of filtering with $gt. A capped collection removes the oldest documents first, so if the
    // last seen one is gone (overwritten, or the collection was dropped and created again), every remaining one is newer.
    private void tail() {
        long backoff = MIN_BACKOFF;
        while (running) {
            boolean delivered = false;
            try (MongoCursor<Document> cursor = collection().find()
                    .cursorType(CursorType.TailableAwait)
                    .maxAwaitTime(1, TimeUnit.SECONDS)
                    .iterator()) {
                boolean positioned = !markerExists();
                while (running) {
                    Document document = cursor.tryNext();
                    if (document == null) {
                        if (!positioned) {
                            LOG.warn("Last seen message of {} was overwritten while tailing, messages may have been lost", name);
                            positioned = true;
                        }
                        if (cursor.getServerCursor() == null) {
                            break;
                        }
                        continue;
                    }

                    Object id = document.get(ID);
                    if (!positioned) {
                        positioned = id.equals(marker);
                        continue;
                    }

                    marker = id;
                    delivered = true;
                    if (!instanceId.equals(document.getString(INSTANCE)) && !SEED.equals(document.getString(TOPIC))) {
                        InMemoryMessageBroker.deliver(subscribers, document.getString(TOPIC), document.getString(MESSAGE));
                    }
                }
                backoff = delivered ? MIN_BACKOFF : backoff;
            } catch (MongoException | IllegalStateException e) {
                if (!running) {
                    break;
                }
                LOG.warn("Tailing {} failed, retrying in {} ms: {}", name, backoff, e.getMessage());
                LOG.debug("Tailing {} failed", name, e);
            }

            if (running) {
                pause(backoff);
                backoff = Math.min(backoff * 2, MAX_BACKOFF);
                reprepare();
            }
        }
    }

    private boolean markerExists() {
        return marker != null && collection().find(Filters.eq(ID, marker)).first() != null;
    }

    private void reprepare() {
        try {
            prepare();
        } catch (MongoException | IllegalStateException e) {
            LOG.warn("Failed to prepare {}: {}", name, e.getMessage());
        }
    }

    private void pause(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            running = false;
        }
    }

    boolean isRunning() {
        return running && thread != null && thread.isAlive();
    }
}
