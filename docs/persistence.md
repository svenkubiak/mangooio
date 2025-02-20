mangoo I/O comes with a ready-to-use integration into [MongoDB](https://www.mongodb.com/).

## Configuration

To setup a mongoDB connection use the follow configuration values

	[persistence]
	mongo.host = 127.0.0.1
	mongo.port = 27017
	mongo.username = myUsername
	mongo.password = myPassword
	mongo.dbname = myDBname  //DB to connect to
	mongo.authdb = myAuthDB  //AuthDB to use
	mongo.auth = true        //Enable authentication or not
	mongo.embedded = true    //Start In-Memory instance
	mongo.package = models   //Name of the packages where the Morphia models are stored
		
One important value is `mongo.embedded = true`. This will start an embedded mongoDB for local development and testing purposes. Make sure that this is set to false (default) on production environments.

## Entity configuration

To map a Class to the persistence datastore, simply add the @Collection annotation to your Pojo

```java
@Collection(name = "people")
public class Person extends Entity {
...
}

```

Extending Entity is optional, but provides you with method for getting the Id of the entity.

## Datastore

Once the configuration is done and your models are mapped, you can start working with the ready-to-use Datastore.

```java
private final Datastore datastore;

@Inject
public DataService(Datastore datastore) {
    this.datastore = Objects.requireNonNull(datastore, "datastore can not be null");
}
```

The datastore offers some useful methods for working with entites, e.g.

```java
findById(String id, Class<T> clazz);
findAll(Class<T> clazz);
countAll(Class<T> clazz);
save(Object object);
delete(Object object);
deleteAll(Class<T> clazz);
dropDatabase();
```

For specific queries you can use the fluent MongoDB API, using the query() method. See the following example:

```java
List<Booking> bookings = datastore
          .query(Booking.class)
          .find(and(gte("booked", fromDate), lte("booked", toDate)))
          .into(bookings);
```
             
Once using query() you can access all MongoDB Java client methods and functions to query the datastore.

## Multiple connections/users

If you want to setup multiple connection with e.g. multiple users, you need to slightly change the way you retrieve the datastore.

```java
@Inject
public DataService(DatastoreProvider datastoreProvider) {
    Objects.requireNonNull(datastoreProvider, "datastoreProvider can not be null");
    datastore = datastoreProvider.getDatastore("readonly");
}
```

The passed "readonly" string matches a prefix that you can setup in your configuration.

	[persistence]
	readonly.mongo.host = 127.0.0.1
	readonly.mongo.port = 27017
	readonly.mongo.username = myUsername
	readonly.mongo.password = myPassword
	readonly.mongo.dbname = myDBname  //DB to connect to
	readonly.mongo.authdb = myAuthDB  //AuthDB to use
	readonly.mongo.auth = true        //Enable authentication or not
	readonly.mongo.embedded = true    //Start In-Memory instance
	readonly.mongo.package = models   //Name of the packages where the Morphia models are stored