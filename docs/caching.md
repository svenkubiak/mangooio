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

The fallback is a `Function<String, Object>`, and its result is stored under the same key so the next `get` hits the cache instead of the fallback.

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

The application cache is the default, but two more caches exist out of the box:

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

Register extra caches with `cacheProvider.addCache(name, cache)`, and they show up on the [admin](administration.md) dashboard alongside the built-in ones.
