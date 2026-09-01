# Dependency injection

mangoo I/O uses [Google Guice](https://github.com/google/guice) for dependency injection. Controllers, filters, bootstrap classes, subscribers, and your own services are created by the injector. You register bindings in `app.Module`; the framework module already wires core types such as `Config`, `Vault`, `Cache`, and `Datastore`.

Every flattened key from `config.yaml` is also available as a `@Named("dotted.key")` string binding, which is useful for feature flags or URLs without wrapping them in a dedicated class.

Prefer **constructor injection** so dependencies are required and immutable. Field injection works but is harder to test in isolation. For static contexts (rare), `Application.getInstance(Class)` reaches the same injector.

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

Use this from static contexts (jobs, subscribers already go through Guice). Prefer constructor injection in application code.

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
