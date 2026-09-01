# Caching

Repeated expensive lookups—database reads, external API calls, computed aggregates—benefit from an in-process cache. mangoo I/O wraps **Caffeine** and exposes a injectable **`Cache`** for the default application cache, plus named caches when you need separate eviction policies or key spaces.

Entries expire **after write** (default 30 days). You can attach a **fallback** function so `get` loads missing keys automatically. Counter helpers support rate limiting or metrics-style increments without a separate store.

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

Entries in the application cache expire **30 days after write** (not last access) and the cache holds at most 50 000 keys.

## TTL and fallback

```java
cache.put("foo", "bar", 10, ChronoUnit.MINUTES);
cache.put("foo", "bar", LocalDateTime.now().plusHours(1));

String value = cache.get("foo", key -> loadFromDatabase(key));
String timed = cache.get("foo", 5, ChronoUnit.MINUTES, key -> loadFromDatabase(key));
```

The fallback is a `Function<String, Object>`. The result is stored under the same key.

```java
cache.putAll(Map.of("a", 1, "b", 2));
Map<String, Object> batch = cache.getAll("a", "b");
```

## Counters

```java
cache.getAndIncrementCounter("logins");
cache.getAndDecrementCounter("logins");
cache.getCounter("logins");
cache.resetCounter("logins");
```

## Named caches

The application cache is the default. Two more caches exist:

| Name (`CacheName`) | Use | Eviction |
|---|---|---|
| `APPLICATION` | Default `Cache` injection | 30 days after write, 50 000 keys |
| `AUTH` | Login lockout counters | 60 minutes after write |
| `BLACKLIST` | Created only if `authentication.blacklist` is true | 60 minutes after write |

```java
@Inject
public MyService(CacheProvider cacheProvider) {
    Cache auth = cacheProvider.getCache(CacheName.AUTH);
}
```

Register extra caches with `cacheProvider.addCache(name, cache)` so they appear on the [admin](administration.md) dashboard.
