package io.mangoo.persistence;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import io.mangoo.TestExtension;
import io.mangoo.core.Application;
import io.mangoo.core.Config;
import io.mangoo.interfaces.AttemptStore;
import io.mangoo.interfaces.OneTimeStore;
import io.mangoo.interfaces.TokenBlacklist;
import io.mangoo.interfaces.persistence.Datastore;
import io.mangoo.utils.CommonUtils;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

// Threads stand in for several instances, as the atomicity is in the database
@ExtendWith({TestExtension.class})
class MongoStoresTest {
    private static final Duration LOCK = Duration.ofMinutes(60);
    private static final Duration LIFETIME = Duration.ofDays(30);

    private static String key() {
        return "test:" + CommonUtils.uuidV6();
    }

    private static Datastore datastore() {
        return Application.getInstance(Datastore.class);
    }

    private static MongoCollection<Document> collection(String name) {
        return datastore().getMongoDatabase().getCollection(name);
    }

    private static Document document(String collection, String id) {
        return collection(collection).find(Filters.eq("_id", id)).first();
    }

    private static int concurrently(int times, Callable<Boolean> task) throws Exception {
        var start = new CountDownLatch(1);
        var successes = new AtomicInteger();
        try (var executor = Executors.newFixedThreadPool(times)) {
            var futures = new java.util.ArrayList<Future<?>>();
            for (var i = 0; i < times; i++) {
                futures.add(executor.submit(() -> {
                    start.await();
                    if (task.call()) {
                        successes.incrementAndGet();
                    }
                    return null;
                }));
            }
            start.countDown();
            for (Future<?> future : futures) {
                future.get();
            }
        }

        return successes.get();
    }

    private static DatastoreImpl closedDatastore() {
        var datastore = new DatastoreImpl(Application.getInstance(Config.class));
        datastore.close();
        return datastore;
    }

    @Test
    void testBindingsUseMongoDBWithPersistence() {
        //then
        assertThat(Application.getInstance(AttemptStore.class), instanceOf(MongoAttemptStore.class));
        assertThat(Application.getInstance(OneTimeStore.class), instanceOf(MongoOneTimeStore.class));
        assertThat(Application.getInstance(TokenBlacklist.class), instanceOf(MongoTokenBlacklist.class));
    }

    // DatastoreTest drops the database, which also removes the indexes created at startup
    @Test
    void testPrepareCreatesTtlIndexes() {
        //given
        for (Class<?> store : new Class<?>[] {AttemptStore.class, OneTimeStore.class, TokenBlacklist.class}) {
            ((MongoStore) Application.getInstance(store)).prepare();
        }

        for (String name : new String[] {"mangooio_attempts", "mangooio_claims", "mangooio_revocations"}) {
            //when
            boolean ttl = false;
            for (Document index : collection(name).listIndexes()) {
                if (index.get("key", Document.class).containsKey("expireAt")) {
                    ttl = Long.valueOf(0).equals(((Number) index.get("expireAfterSeconds")).longValue());
                }
            }

            //then
            assertThat(name, ttl, equalTo(true));
        }
    }

    @Test
    void testAttemptThatUsesUpTheBudgetIsGranted() {
        //given
        var store = Application.getInstance(AttemptStore.class);
        String key = key();

        //when
        boolean first = store.tryAcquire(key, 3, LOCK);
        boolean second = store.tryAcquire(key, 3, LOCK);
        boolean lockedBefore = store.isLocked(key);
        boolean third = store.tryAcquire(key, 3, LOCK);

        //then
        assertThat(first && second && third, equalTo(true));
        assertThat(lockedBefore, equalTo(false));
        assertThat(store.isLocked(key), equalTo(true));
        assertThat(store.tryAcquire(key, 3, LOCK), equalTo(false));
    }

    @Test
    void testLockIsSetFromDatabaseTimeAndNotExtended() {
        //given
        var store = Application.getInstance(AttemptStore.class);
        String key = key();
        store.tryAcquire(key, 1, LOCK);
        Date lockedUntil = document("mangooio_attempts", key).getDate("lockedUntil");

        //when
        for (var i = 0; i < 5; i++) {
            store.tryAcquire(key, 1, LOCK);
        }

        //then
        Document document = document("mangooio_attempts", key);
        assertThat(document.getDate("lockedUntil"), equalTo(lockedUntil));
        assertThat(document.getInteger("attempts"), equalTo(1));
        assertThat(document.containsKey("expired"), equalTo(false));
        assertThat(Duration.between(Instant.now(), lockedUntil.toInstant()).toMinutes(), greaterThanOrEqualTo(58L));
        assertThat(document.getDate("expireAt"), greaterThanOrEqualTo(lockedUntil));
    }

    @Test
    void testRetentionOfAnUnlockedBudget() {
        //given
        var store = Application.getInstance(AttemptStore.class);
        String key = key();

        //when
        store.tryAcquire(key, 5, Duration.ofMinutes(10));

        //then the budget lives at least 60 minutes, even with a shorter lock
        Document document = document("mangooio_attempts", key);
        assertThat(document.getDate("lockedUntil"), nullValue());
        assertThat(Duration.between(Instant.now(), document.getDate("expireAt").toInstant()).toMinutes(), greaterThanOrEqualTo(58L));
    }

    @Test
    void testElapsedLockResetsTheBudget() {
        //given
        var store = Application.getInstance(AttemptStore.class);
        String key = key();
        store.tryAcquire(key, 1, LOCK);
        collection("mangooio_attempts").updateOne(Filters.eq("_id", key), Updates.set("lockedUntil", Date.from(Instant.now().minusSeconds(1))));

        //then
        assertThat(store.isLocked(key), equalTo(false));
        assertThat(store.tryAcquire(key, 2, LOCK), equalTo(true));
        assertThat(document("mangooio_attempts", key).getInteger("attempts"), equalTo(1));
        assertThat(store.isLocked(key), equalTo(false));
    }

    @Test
    void testReleaseAndReset() {
        //given
        var store = Application.getInstance(AttemptStore.class);
        String key = key();
        store.tryAcquire(key, 2, LOCK);
        store.tryAcquire(key, 2, LOCK);

        //when
        store.release(key, 2);

        //then
        assertThat(store.isLocked(key), equalTo(false));
        assertThat(document("mangooio_attempts", key).getInteger("attempts"), equalTo(1));

        //when
        store.reset(key);

        //then
        assertThat(document("mangooio_attempts", key), nullValue());
    }

    @Test
    void testConcurrentAttemptsDoNotExceedTheBudget() throws Exception {
        //given
        var store = Application.getInstance(AttemptStore.class);
        String key = key();

        //when
        int granted = concurrently(50, () -> store.tryAcquire(key, 10, LOCK));

        //then
        assertThat(granted, equalTo(10));
        assertThat(document("mangooio_attempts", key).getInteger("attempts"), equalTo(10));
        assertThat(store.isLocked(key), equalTo(true));
    }

    @Test
    void testAttemptStoreFailsClosed() {
        //given
        var store = new MongoAttemptStore(closedDatastore());

        //then
        assertThat(store.tryAcquire(key(), 10, LOCK), equalTo(false));
        assertThat(store.isLocked(key()), equalTo(true));
    }

    @Test
    void testClaimOnce() throws Exception {
        //given
        var store = Application.getInstance(OneTimeStore.class);
        String key = key();

        //when
        int claimed = concurrently(50, () -> store.claim(key, Instant.now().plusSeconds(60)));

        //then
        assertThat(claimed, equalTo(1));
        assertThat(store.claim(key, Instant.now().plusSeconds(60)), equalTo(false));
    }

    @Test
    void testAdvanceAcceptsOnlyGreaterValues() throws Exception {
        //given
        var store = Application.getInstance(OneTimeStore.class);
        String key = key();
        Instant expiresAt = Instant.now().plusSeconds(60);

        //when
        int advanced = concurrently(50, () -> store.advance(key, 7, expiresAt));

        //then
        assertThat(advanced, equalTo(1));
        assertThat(store.advance(key, 6, expiresAt), equalTo(false));
        assertThat(store.advance(key, 8, expiresAt), equalTo(true));
        assertThat(document("mangooio_claims", key).getLong("value"), equalTo(8L));
    }

    @Test
    void testOneTimeStoreFailsClosed() {
        //given
        var store = new MongoOneTimeStore(closedDatastore());

        //then
        assertThat(store.claim(key(), Instant.now().plusSeconds(60)), equalTo(false));
        assertThat(store.advance(key(), 1, Instant.now().plusSeconds(60)), equalTo(false));
    }

    @Test
    void testRevokedTokenOutlivesExpiryByClockSkew() {
        //given
        var blacklist = new MongoTokenBlacklist(datastore(), LIFETIME, Clock.systemUTC());
        String jwtId = CommonUtils.uuidV6();
        Instant expiresAt = Instant.now().plusSeconds(60).truncatedTo(ChronoUnit.MILLIS);

        //when
        blacklist.revoke(jwtId, expiresAt);

        //then
        assertThat(blacklist.isRevoked(jwtId, null, null), equalTo(true));
        assertThat(document("mangooio_revocations", "jti:" + jwtId).getDate("expireAt").toInstant(), equalTo(expiresAt.plusSeconds(30)));
    }

    @Test
    void testTokenExpiredWithinClockSkewIsRevoked() {
        //given
        var blacklist = new MongoTokenBlacklist(datastore(), LIFETIME, Clock.systemUTC());
        String expired = CommonUtils.uuidV6();
        String withinSkew = CommonUtils.uuidV6();

        //when
        blacklist.revoke(expired, Instant.now().minusSeconds(31));
        blacklist.revoke(withinSkew, Instant.now().minusSeconds(1));

        //then
        assertThat(blacklist.isRevoked(expired, null, null), equalTo(false));
        assertThat(blacklist.isRevoked(withinSkew, null, null), equalTo(true));
    }

    @Test
    void testRevokeSubject() {
        //given
        var blacklist = new MongoTokenBlacklist(datastore(), LIFETIME, Clock.systemUTC());
        String subject = CommonUtils.uuidV6();
        Instant second = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        Instant since = second.plusMillis(500);

        //when
        blacklist.revokeSubject(subject, since);
        blacklist.revokeSubject(subject, since.minusSeconds(120));

        //then the later revocation wins and a token of the same second stays valid
        assertThat(blacklist.isRevoked("other", subject, second.minusSeconds(1)), equalTo(true));
        assertThat(blacklist.isRevoked("other", subject, second), equalTo(false));
        assertThat(blacklist.isRevoked("other", "other-subject", second.minusSeconds(1)), equalTo(false));
        assertThat(document("mangooio_revocations", "sub:" + subject).getDate("expireAt").toInstant(),
                equalTo(since.truncatedTo(ChronoUnit.MILLIS).plus(LIFETIME).plusSeconds(30)));
    }

    @Test
    void testTokenBlacklistFailsClosed() {
        //given
        var blacklist = new MongoTokenBlacklist(closedDatastore(), LIFETIME, Clock.systemUTC());

        //then
        assertThat(blacklist.isRevoked("jti", "subject", Instant.now()), equalTo(true));
        assertThat(blacklist.isRevoked(null, null, null), equalTo(false));
    }
}
