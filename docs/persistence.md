# Persistence

mangoo I/O includes a thin integration layer over the [MongoDB Java Sync Driver](https://www.mongodb.com/docs/drivers/java/sync/current/quick-start/). You work with POJOs and BSON queries directly, there is no ORM query language on top and no Morphia (it was removed in 8.0).

At startup, the framework scans for classes annotated with `@Collection`, registers their collection names, and optionally creates indexes from `@Indexed` fields. Inject `Datastore` (or `DatastoreProvider` when you need multiple databases) and use familiar driver patterns: `save`, `find`, `findAll`, and `query()` for fluent, driver-level access.

For local development, the archetype can start **embedded MongoDB 7.0** when `persistence.mongo.embedded` is `true`, so you get a working database with zero setup. Turn that off in production and point `host` / `port` at your actual cluster instead. Passwords belong in the [vault](secrets.md) or the environment, never in source control.

## Configuration

```yaml
default:
  persistence:
    mongo:
      host: 127.0.0.1
      port: 27017
      username: myUsername
      password: vault{}
      dbname: myDBname
      authdb: myAuthDB
      auth: true
      embedded: false
```

Set `persistence.enable` to `false` if you want to skip MongoDB entirely.

!!! note
    `persistence.mongo.embedded: true` starts MongoDB 7.0 in-process. Use it only for local development and tests, never in production.

There is no `package` setting to configure. Just annotate your entity classes and Classgraph finds them on its own.

## Entities

```java
import io.mangoo.annotations.Collection;
import io.mangoo.annotations.Indexed;
import io.mangoo.enums.Sort;
import io.mangoo.persistence.Entity;

@Collection(name = "people")
public class Person extends Entity {
    @Indexed(sort = Sort.ASCENDING, unique = true)
    private String email;

    private String firstname;
    private String lastname;
}
```

`Entity` stores `_id` as a BSON `ObjectId`. Extending it is optional as long as you implement `BaseEntity` yourself instead.

`@Indexed` is applied at startup, and it understands the `sort`, `unique`, and `caseSensitive` attributes.

## Datastore

```java
import io.mangoo.persistence.interfaces.Datastore;
import jakarta.inject.Inject;

public class PersonService {
    private final Datastore datastore;

    @Inject
    public PersonService(Datastore datastore) {
        this.datastore = datastore;
    }
}
```

```java
String id = datastore.save(person);
Person found = datastore.find(Person.class, eq("_id", new ObjectId(id)));
List<Person> all = datastore.findAll(Person.class);
long count = datastore.countAll(Person.class);
datastore.delete(person);
datastore.dropCollection(Person.class);
datastore.isHealthy();
```

Query helpers:

```java
Person first = datastore.findFirst(Person.class, Sorts.descending("created"));
List<Person> page = datastore.findAll(Person.class, Filters.eq("active", true), Sorts.ascending("lastname"), 50);
```

When you need the full driver API rather than the convenience helpers, drop down to `query()`:

```java
List<Booking> bookings = new ArrayList<>();
datastore
    .query(Booking.class)
    .find(Filters.and(
        Filters.gte("booked", fromDate),
        Filters.lte("booked", toDate)))
    .into(bookings);
```

Index helpers cover `addIndex`, `dropIndex`, and `dropAllIndexes`. `saveAll` inserts an entire list at once, and `getMongoDatabase()` hands you the native driver handle for anything not covered above.

## Multiple connections

```java
@Inject
public PersonService(DatastoreProvider datastoreProvider) {
    this.readonly = datastoreProvider.getDatastore("readonly");
}
```

The prefix you pass to `getDatastore` maps directly to `persistence.<prefix>.mongo.*`:

```yaml
persistence:
  readonly:
    mongo:
      host: 127.0.0.1
      port: 27017
      username: reader
      password: vault{}
      dbname: myDBname
      authdb: myAuthDB
      auth: true
      embedded: false
```
