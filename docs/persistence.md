# Persistence

mangoo I/O integrates [MongoDB](https://www.mongodb.com/) through the [Java Sync Driver](https://www.mongodb.com/docs/drivers/java/sync/current/quick-start/). POJOs are mapped with the driver codec (not Morphia).

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

Set `persistence.enable` to `false` to skip MongoDB entirely.

!!! note
    `persistence.mongo.embedded: true` starts MongoDB 7.0 in-process. Use it only for local development and tests.

There is no `package` setting. Annotate entity classes; Classgraph finds them.

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

`Entity` stores `_id` as a BSON `ObjectId`. Extending it is optional if you implement `BaseEntity` yourself.

`@Indexed` is applied at startup (`sort`, `unique`, `caseSensitive`).

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

For the full driver API, use `query()`:

```java
List<Booking> bookings = new ArrayList<>();
datastore
    .query(Booking.class)
    .find(Filters.and(
        Filters.gte("booked", fromDate),
        Filters.lte("booked", toDate)))
    .into(bookings);
```

Index helpers: `addIndex`, `dropIndex`, `dropAllIndexes`. `saveAll` inserts a list. `getMongoDatabase()` returns the native handle.

## Multiple connections

```java
@Inject
public PersonService(DatastoreProvider datastoreProvider) {
    this.readonly = datastoreProvider.getDatastore("readonly");
}
```

The prefix maps to `persistence.<prefix>.mongo.*`:

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
