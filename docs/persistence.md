# Persistence

mangoo I/O includes a thin integration layer over the [MongoDB Java Sync Driver](https://www.mongodb.com/docs/drivers/java/sync/current/quick-start/). You work with POJOs and BSON queries directly, there is no ORM query language on top and no Morphia (it was removed in 8.0).

At startup, the framework scans for classes annotated with `@Collection`, registers their collection names, and optionally creates indexes from `@Indexed` fields. Inject `Datastore` (or `DatastoreProvider` when you need multiple databases) and use familiar driver patterns: `save`, `find`, `findAll`, and `query()` for fluent, driver-level access.

For local development, the archetype can start **embedded MongoDB 7.0** when `persistence.mongo.embedded` is `true`, so you get a working database with zero setup. Turn that off in production and point `host` / `port`, or a `url`, at your actual MongoDB server. Passwords belong in the environment or a [secret file](secrets.md), never in source control.

## Configuration

```yaml
default:
  persistence:
    mongo:
      host: 127.0.0.1
      port: 27017
      username: myUsername
      password: env{}
      dbname: myDBname
      authdb: myAuthDB
      auth: true
      embedded: false
```

Set `persistence.enable` to `false` if you want to skip MongoDB entirely; the framework then keeps its own state in memory, see [Framework collections](#framework-collections).

`host` and `port` address one server. A replica set, several mongos instances, or an SRV address such as MongoDB Atlas goes into `url`, which then replaces `host` and `port`:

```yaml
persistence:
  mongo:
    url: mongodb://mongo-1:27017,mongo-2:27017/?replicaSet=rs0
    username: myUsername
    password: env{}
    dbname: myDBname
    authdb: admin
    auth: true
```

`url` accepts any connection string the driver supports, including `mongodb+srv://cluster.example.mongodb.net`. Options in the query string, such as `replicaSet`, `tls`, `retryWrites` and `w`, are passed through. The database mangoo I/O uses is still `dbname`. If the URL contains a database or `authSource`, that value has to match `dbname` or `authdb`, and startup fails when it does not. An omitted `authdb` means `admin`. Credentials in the URL are rejected, so username, password and authdb stay in the keys above and special characters need no URL encoding. `url` cannot be combined with `embedded: true`, because the embedded server is started from `host` and `port` only.

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
import io.mangoo.interfaces.persistence.Datastore;
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

`save` inserts an object without an id and replaces the document of an object with an id. An object whose id was set by the application, e.g. to reference it before it is stored, is inserted under that id if no document exists yet. The generated id of an inserted object is returned, but not written back to the object, so load it again before changing and saving it a second time.

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

Index helpers cover `addIndex`, `dropIndex`, and `dropAllIndexes`. `saveAll` calls `save` for each element of a list (no bulk write, not atomic), and `getMongoDatabase()` hands you the native driver handle for anything not covered above.

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
      password: env{}
      dbname: myDBname
      authdb: myAuthDB
      auth: true
      embedded: false
```

## Framework collections

With persistence enabled, the framework keeps the state that all instances have to share in the default datastore, i.e. in the database of `persistence.mongo.dbname`, next to the collections of your application. Give every application its own `dbname`: two applications in one database would share these collections, so a lock of one user name would apply to both and server sent events would reach the clients of the other application.

| Collection | Store | Content |
|---|---|---|
| `mangooio_attempts` | `AttemptStore` | Failed login budgets and locks |
| `mangooio_claims` | `OneTimeStore` | Used TOTP codes and the pending second factor of the admin login |
| `mangooio_revocations` | `TokenBlacklist` | Revoked authentication cookies (`authentication.blacklist`) |
| `mangooio_messages` | `MessageBroker` | Server sent events and admin actions for the other instances, capped at 16 MB |
| `mangooio_instances` | `InstanceRegistry` | Heartbeat of every instance for the admin dashboard |

The startup creates them: a TTL index on `expireAt` for `mangooio_attempts`, `mangooio_claims`, `mangooio_revocations` and `mangooio_instances`, which removes expired entries, and the capped collection `mangooio_messages`. A `mangooio_messages` that exists but is not capped fails the startup. After the database was dropped, e.g. in tests, the indexes are created again on the next start. The database user needs the built-in role `readWrite` on `dbname`, which covers creating collections and indexes, see [Schema](#schema) for a user without that right. MongoDB 4.2 or later is required. Reads go to the primary and writes are acknowledged by the majority, whatever `url` sets; if MongoDB is unavailable, the first three fail closed and refuse logins, second factors and authenticated requests, see [Operating](operating.md#running-several-instances).

Without persistence the same state is kept in memory per process. It is lost on a restart, so a revoked cookie that has not yet expired is accepted again, and a lock or used TOTP code is forgotten. This is fine for a single instance that can accept it; several instances always need persistence. `persistence.enable` switches the datastore and the framework state together, there is no separate switch: an application that does not use the `Datastore` itself still gets the shared state with persistence enabled. To keep the state elsewhere, bind your own implementation of the store in `app.Module`.

## Schema

`persistence.mongo.schema` controls whether the startup creates the collections and indexes of the default datastore: the `@Indexed` indexes of your entities and the [framework collections](#framework-collections).

| Value | Behaviour |
|---|---|
| `create` | Creates missing collections and indexes (default) |
| `validate` | Creates nothing and checks that everything exists; the startup fails with a list of the missing parts as mongosh commands |

Use `validate` when the database user may not create collections or indexes, e.g. because a DBA manages the schema. Run the listed commands once with a user that may, then start again:

```
persistence.mongo.schema is validate, but the database lacks the following, create it with mongosh:
  db.getCollection("mangooio_attempts").createIndex({"expireAt": 1}, {"expireAfterSeconds": 0})
  db.createCollection("mangooio_messages", {"capped": true, "size": 16777216})
```

An index counts as present if its keys, uniqueness, collation and expiry match. Without the TTL indexes expired entries would never be removed, which is why a missing one fails the startup instead of logging a warning. With `validate`, a `mangooio_messages` that is dropped while the application runs is not created again; the server sent events of the other instances pause until it exists.

