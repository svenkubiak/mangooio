mangoo I/O ships with a pre-configured application Cache based on [Caffeine](https://github.com/ben-manes/caffeine). To use the cache in your application, simply inject the cache class.

```java
@Inject
private Cache cache;
```

The cache offers some convenient functions for adding and removing values from the cache.

To use the cache, simply add or remove an entry by a specific key.

```java
cache.put("foo", "bar);
```

```java
String value = cache.get("foo");
```

```java
cache.remove("foo");
```

The cache has a expiration of of 30 days after access. So, if you neither read or write a specific value of the cache it will be evicted after 30 days.

Besides population the cache as seen above, you can also use a get call incorporated by a fallback method

```java
String value = cache.get("foo", v -> getValue());
```

This call will try to lookup the value from key "foo" and if not found will call the method getValue(), return that value and populate the cache with the returned value.