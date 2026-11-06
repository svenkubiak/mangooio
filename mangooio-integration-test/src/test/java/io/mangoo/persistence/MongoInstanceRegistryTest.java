package io.mangoo.persistence;

import io.mangoo.TestExtension;
import io.mangoo.core.Application;
import io.mangoo.interfaces.InstanceRegistry;
import io.mangoo.interfaces.persistence.Datastore;
import io.mangoo.models.InstanceStatus;
import io.mangoo.utils.CommonUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@ExtendWith({TestExtension.class})
class MongoInstanceRegistryTest {
    private static final Instant NOW = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static Datastore datastore() {
        return Application.getInstance(Datastore.class);
    }

    private static String collection() {
        return "test_instances_" + CommonUtils.uuidV6().replace("-", "");
    }

    private static MongoInstanceRegistry registry(String collection, Instant now) {
        var registry = new MongoInstanceRegistry(datastore(), collection, Clock.fixed(now, ZoneOffset.UTC));
        registry.prepare();
        return registry;
    }

    private static InstanceStatus status(String id, Instant seen) {
        return new InstanceStatus(id, "11.0.0", NOW.minusSeconds(3600), seen, 100, 200, 3, 42, 2, 15, 1);
    }

    @Test
    void testBindingUsesMongoDBWithPersistence() {
        //then
        assertThat(Application.getInstance(InstanceRegistry.class), instanceOf(MongoInstanceRegistry.class));
    }

    @Test
    void testInstancesOfAllInstancesAreListedSortedById() {
        //given
        var registry = registry(collection(), NOW);

        //when
        registry.report(status("second", NOW));
        registry.report(status("first", NOW));

        //then
        assertThat(registry.getInstances(), contains(status("first", NOW), status("second", NOW)));
    }

    @Test
    void testReportReplacesThePreviousStatus() {
        //given
        var registry = registry(collection(), NOW);
        registry.report(status("first", NOW.minusSeconds(10)));

        //when
        registry.report(status("first", NOW));

        //then
        assertThat(registry.getInstances(), contains(status("first", NOW)));
    }

    @Test
    void testRemovedInstanceIsNotListed() {
        //given
        var registry = registry(collection(), NOW);
        registry.report(status("first", NOW));
        registry.report(status("second", NOW));

        //when
        registry.remove("first");

        //then
        assertThat(registry.getInstances(), contains(status("second", NOW)));
    }

    @Test
    void testInstanceWithoutHeartbeatForAMinuteIsNotListed() {
        //given
        String collection = collection();
        registry(collection, NOW).report(status("first", NOW));

        //when
        var later = registry(collection, NOW.plus(Duration.ofMinutes(1)).plusSeconds(1));

        //then
        assertThat(later.getInstances(), empty());
    }

    @Test
    void testValidateListsTheMissingTtlIndex() {
        //given
        String collection = collection();
        var registry = new MongoInstanceRegistry(datastore(), collection, Clock.systemUTC());

        //when
        var missing = registry.validate();

        //then
        assertThat(missing, contains(containsString("db.getCollection(\"" + collection + "\").createIndex({\"expireAt\": 1}")));
    }
}
