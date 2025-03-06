# Cache

mangoo I/O comes with a pre-configured application cache powered by [Caffeine](https://github.com/ben-manes/caffeine). To utilize caching in your application, inject the `Cache` class:

```java
@Inject
private Cache cache;
```

## Cache Operations

The `Cache` class provides essential functions for managing cached values, including adding, retrieving, and removing entries.

### Adding a Value to the Cache
```java
cache.put("foo", "bar");
```

### Retrieving a Cached Value
```java
String value = cache.get("foo");
```

### Removing a Value from the Cache
```java
cache.remove("foo");
```

## Cache Expiration

Cached entries expire **30 days after the last access**. If a value is neither read nor updated within this period, it will be automatically evicted.

## Using a Fallback Method

In addition to standard cache operations, you can use a **get** call with a fallback method:

```java
String value = cache.get("foo", v -> getValue());
```

If the key `"foo"` exists in the cache, its value is returned. If not, the fallback method `getValue()` is invoked, its return value is cached under the key `"foo"`, and then returned.
