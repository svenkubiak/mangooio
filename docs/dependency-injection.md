# Dependency injection

mangoo I/O uses [Google Guice](https://github.com/google/guice) for dependency injection, plain and unmodified. Controllers, filters, bootstrap classes, subscribers, and your own services are all created by the injector. You register your own bindings in `app.Module`, while the framework module already wires core types such as `Config`, `Vault`, `Cache`, and `Datastore` for you.

Every flattened key from `config.yaml` is also available as a `@Named("dotted.key")` string binding, which comes in handy for feature flags or URLs you don't want to wrap in a dedicated class.

Prefer **constructor injection** so dependencies are required and immutable, and so a test can just call `new` with mocks instead of reaching for a Guice-aware test harness. Field injection works too, but is harder to test in isolation. For the rare static context where neither applies, `Application.getInstance(Class)` reaches into the same injector.

## Field injection

```java
@Inject
private PersonService personService;
```

## Constructor injection (preferred)

```java
private final PersonService personService;

@Inject
public AccountController(PersonService personService) {
    this.personService = Objects.requireNonNull(personService);
}
```

## Manual lookup

```java
PersonService service = Application.getInstance(PersonService.class);
```

Reach for this only from static contexts; jobs and subscribers already go through Guice on their own. In application code, prefer constructor injection instead.

## Application module

```java
package app;

import com.google.inject.AbstractModule;
import io.mangoo.interfaces.MangooBootstrap;

public class Module extends AbstractModule {
    @Override
    protected void configure() {
        bind(MangooBootstrap.class).to(Bootstrap.class);
        bind(PersonRepository.class).to(MongoPersonRepository.class);
    }
}
```

See [Bootstrap](bootstrap.md) for lifecycle hooks and [Configuration](configuration.md) for `@Named` config values.
