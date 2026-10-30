package io.mangoo.persistence;

import com.mongodb.MongoException;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.Sorts;
import io.mangoo.constants.Required;
import io.mangoo.interfaces.InstanceRegistry;
import io.mangoo.interfaces.persistence.Datastore;
import io.mangoo.models.InstanceStatus;
import io.mangoo.utils.Argument;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * InstanceRegistry shared by all instances in the collection mangooio_instances. Every instance replaces its document on
 * each heartbeat; a document expires one minute after the last heartbeat, so an instance that stopped without removing
 * itself disappears on its own.
 */
@Singleton
public class MongoInstanceRegistry extends MongoStore implements InstanceRegistry {
    private static final Logger LOG = LogManager.getLogger(MongoInstanceRegistry.class);
    private static final String COLLECTION = "mangooio_instances";
    private static final Duration EXPIRES = Duration.ofMinutes(1);
    private static final String VERSION = "version";
    private static final String STARTED = "started";
    private static final String SEEN = "seen";
    private static final String HEAP_USED = "heapUsed";
    private static final String HEAP_MAX = "heapMax";
    private static final String CONNECTIONS = "connections";
    private static final String REQUESTS = "requests";
    private static final String ERRORS = "errors";
    private static final String AVG_REQUEST_TIME = "avgRequestTime";
    private static final String FAILED_EVENTS = "failedEvents";
    private final Clock clock;

    @Inject
    public MongoInstanceRegistry(Datastore datastore) {
        this(datastore, COLLECTION, Clock.systemUTC());
    }

    MongoInstanceRegistry(Datastore datastore, String name, Clock clock) {
        super(datastore, name);
        this.clock = Objects.requireNonNull(clock, Required.CLOCK);
    }

    @Override
    public void report(InstanceStatus status) {
        Objects.requireNonNull(status, "status can not be null");

        var document = new Document(ID, status.id())
                .append(VERSION, status.version())
                .append(STARTED, Date.from(status.started()))
                .append(SEEN, Date.from(status.seen()))
                .append(HEAP_USED, status.heapUsed())
                .append(HEAP_MAX, status.heapMax())
                .append(CONNECTIONS, status.connections())
                .append(REQUESTS, status.requests())
                .append(ERRORS, status.errors())
                .append(AVG_REQUEST_TIME, status.avgRequestTime())
                .append(FAILED_EVENTS, status.failedEvents())
                .append(EXPIRE_AT, Date.from(status.seen().plus(EXPIRES)));

        try {
            collection().replaceOne(Filters.eq(ID, status.id()), document, new ReplaceOptions().upsert(true));
        } catch (MongoException | IllegalStateException e) {
            LOG.error("Failed to report the status of instance '{}'", status.id(), e);
        }
    }

    @Override
    public void remove(String instanceId) {
        Argument.requireNonBlank(instanceId, Required.ID);

        try {
            collection().deleteOne(Filters.eq(ID, instanceId));
        } catch (MongoException | IllegalStateException e) {
            // Removing is called on shutdown only, without it the entry expires a minute after the last heartbeat
            LOG.warn("Failed to remove instance '{}', it expires a minute after the last heartbeat: {}", instanceId, e.getMessage());
        }
    }

    @Override
    public List<InstanceStatus> getInstances() {
        List<InstanceStatus> instances = new ArrayList<>();
        try {
            // The TTL monitor runs once a minute, so expired documents are filtered as well
            for (Document document : collection().find(Filters.gt(EXPIRE_AT, Date.from(clock.instant()))).sort(Sorts.ascending(ID))) {
                instances.add(new InstanceStatus(
                        String.valueOf(document.get(ID)),
                        document.get(VERSION) instanceof String version ? version : "",
                        instant(document, STARTED),
                        instant(document, SEEN),
                        number(document, HEAP_USED),
                        number(document, HEAP_MAX),
                        (int) number(document, CONNECTIONS),
                        number(document, REQUESTS),
                        number(document, ERRORS),
                        number(document, AVG_REQUEST_TIME),
                        number(document, FAILED_EVENTS)));
            }
        } catch (MongoException | IllegalStateException e) {
            LOG.error("Failed to read the instances", e);
        }

        return instances;
    }

    // Read tolerantly, so a document of another framework version does not hide the other instances
    private static long number(Document document, String key) {
        return document.get(key) instanceof Number number ? number.longValue() : 0;
    }

    private static Instant instant(Document document, String key) {
        return document.get(key) instanceof Date date ? date.toInstant() : Instant.EPOCH;
    }
}
