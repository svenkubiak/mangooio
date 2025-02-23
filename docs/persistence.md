# Persistence

Mangoo I/O provides a ready-to-use integration with [MongoDB](https://www.mongodb.com/) using the [Java Sync Driver](https://www.mongodb.com/docs/drivers/java/sync/current/quick-start/).

## Configuration

To set up a MongoDB connection, use the following configuration values:

```yaml
persistence:
  mongo:
    host: 127.0.0.1
    port: 27017
    username: myUsername
    password: myPassword
    dbname: myDBname  # Database to connect to
    authdb: myAuthDB  # Authentication database
    auth: true        # Enable authentication (true/false)
    embedded: true    # Start in-memory instance
    package: models   # Package containing Morphia models
```

!!! note
    Setting `mongo.embedded = true` starts an embedded MongoDB instance for local development and testing. Ensure this is set to `false` in production environments.

## Entity Configuration

To map a class to the persistence datastore, add the `@Collection` annotation to your POJO:

```java
@Collection(name = "people")
public class Person extends Entity {
    // Class implementation
}
```

Extending `Entity` is optional but provides methods to retrieve the entity ID.

## Datastore

Once configured and models are mapped, the ready-to-use datastore can be injected:

```java
private final Datastore datastore;

@Inject
public DataService(Datastore datastore) {
    this.datastore = Objects.requireNonNull(datastore, "datastore cannot be null");
}
```

The datastore provides essential methods for working with entities:

```java
findById(String id, Class<T> clazz);
findAll(Class<T> clazz);
countAll(Class<T> clazz);
save(Object object);
delete(Object object);
deleteAll(Class<T> clazz);
dropDatabase();
```

For specific queries, use the fluent MongoDB API with the `query()` method:

```java
List<Booking> bookings = datastore
    .query(Booking.class)
    .find(and(gte("booked", fromDate), lte("booked", toDate)))
    .into(bookings);
```

Using `query()` grants access to all MongoDB Java client methods.

## Multiple Connections and Users

For multiple connections with different users, adjust how you retrieve the datastore:

```java
@Inject
public DataService(DatastoreProvider datastoreProvider) {
    Objects.requireNonNull(datastoreProvider, "datastoreProvider cannot be null");
    datastore = datastoreProvider.getDatastore("readonly");
}
```

The `"readonly"` string corresponds to a prefix set in the configuration:

```yaml
persistence:
  mongo:
    readonly:
      host: 127.0.0.1
      port: 27017
      username: myUsername
      password: myPassword
      dbname: myDBname  # Database to connect to
      authdb: myAuthDB  # Authentication database
      auth: true        # Enable authentication (true/false)
      embedded: true    # Start in-memory instance
      package: models   # Package containing Morphia models
```

This configuration enables separate connections for different user roles.
