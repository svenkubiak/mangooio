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

The fallback is a `Function<String, Object>`. A non-null result is stored, so the next `get` skips the function. A `null` result is not stored.

`get(key)` and `get(key, amount, unit, fallback)` honour a per-entry expiry from `put(key, value, amount, unit)`. `get(key, fallback)` does not. It reads Caffeine directly and can return a value past that timestamp, until the cache-wide 30-day expiry. Use the timed overload when the fallback must respect the short TTL. Putting `null` removes a cached value.

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

`increment` and `decrement` are atomic. A missing counter starts from the delta, so the first `increment` stores `1` and the first `decrement` stores `-1`. A key that holds something other than a `Long` throws `IllegalStateException`. A counter expires with the cache, or earlier if it was put with an expiry.

## Named caches

The application cache (`CacheName.APPLICATION`) is the default `Cache` injection; it evicts 30 days after write and holds 50 000 keys. Failed login attempts, one-time claims and token revocations are not kept in a cache but in the `AttemptStore`, `OneTimeStore` and `TokenBlacklist`, which are shared through MongoDB when persistence is enabled, see [Operating](operating.md#running-several-instances).

The cache is local to each instance. Bind your own `Cache` in `app.Module` when entries have to be shared. `getCache(name)` returns `null` for a name that was never added. `addCache` refuses the application cache name, ignoring case. `CacheImpl.getStats()` exists for the dashboard. The `Cache` interface does not expose it.

```java
@Inject
public MyService(CacheProvider cacheProvider) {
    Cache cache = cacheProvider.getCache(CacheName.APPLICATION);
}
```

Register extra caches with `cacheProvider.addCache(name, cache)`; caches of type `CacheImpl` show up on the [admin](administration.md) dashboard alongside the application cache.
