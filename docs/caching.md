# Caching

Repeated expensive lookups, such as database reads, external API calls, or computed aggregates, benefit from an in-process cache. mangoo I/O wraps **Caffeine** and exposes an injectable **`Cache`** for the default application cache, plus named caches for when you need separate eviction policies or key spaces.

Entries expire **after write** (30 days by default). You can attach a **fallback** function so a `get` on a missing key loads and stores it automatically instead of returning null. Counter helpers cover rate limiting or metrics-style increments without needing a separate store.

Inject `io.mangoo.cache.Cache` for the application cache:

```java
@Inject
private Cache cache;
```

```java
cache.put("foo", "bar");
String value = cache.get("foo");
cache.remove("foo");
cache.clear();
```

Entries in the application cache expire **30 days after write** (not after last access), and the cache holds at most 50 000 keys.

## TTL and fallback

```java
cache.put("foo", "bar", 10, ChronoUnit.MINUTES);
cache.put("foo", "bar", LocalDateTime.now().plusHours(1));

String value = cache.get("foo", key -> loadFromDatabase(key));
String timed = cache.get("foo", 5, ChronoUnit.MINUTES, key -> loadFromDatabase(key));
```

The fallback is a `Function<String, Object>`, and its result is stored under the same key so the next `get` hits the cache instead of the fallback. If the fallback returns `null`, for example because the record does not exist, `get` returns `null` and nothing is cached, so the next call asks the fallback again. Putting `null` removes a cached value.

```java
cache.putAll(Map.of("a", 1, "b", 2));
Map<String, Object> batch = cache.getAll("a", "b");
```

## Counters

```java
long logins = cache.increment("logins");   // new value, a missing counter starts at 0
cache.decrement("logins");
cache.getCounter("logins");                // 0 if the counter does not exist
cache.resetCounter("logins");
```

`increment` and `decrement` are atomic, concurrent calls are never lost, also not for a counter that does not exist yet. A counter is a `Long` value under its key; calling them on a key that holds another value throws an `IllegalStateException`. Like every value, a counter expires with the cache, or earlier if it was put with an expiry.

## Named caches

The application cache (`CacheName.APPLICATION`) is the default `Cache` injection; it evicts 30 days after write and holds 50 000 keys. Failed login attempts, one-time claims and token revocations are not kept in a cache but in the `AttemptStore`, `OneTimeStore` and `TokenBlacklist`, which are shared through MongoDB when persistence is enabled, see [Operating](operating.md#running-several-instances).

The cache is local to each instance. If entries must be shared between several instances, bind your own `Cache` implementation in `app.Module`, for example backed by Redis; the counters map to atomic operations such as `INCRBY`. mangoo I/O ships no shared cache on purpose: a cache in the same MongoDB as the data saves no round trip, and a value of any type can not be read back from it without knowing its class.

```java
@Inject
public MyService(CacheProvider cacheProvider) {
    Cache cache = cacheProvider.getCache(CacheName.APPLICATION);
}
```

Register extra caches with `cacheProvider.addCache(name, cache)`, and they show up on the [admin](administration.md) dashboard alongside the built-in ones.
