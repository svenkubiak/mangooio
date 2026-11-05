# Dependency injection

mangoo I/O uses [Google Guice](https://github.com/google/guice). Controllers, filters, the bootstrap class, subscribers, scheduled jobs, and your own services are created by the injector. The framework loads `app.Module` by that class name. It needs a public no-arg constructor. A binding of the same type replaces the core binding. The core module already provides `Config`, `Cache`, `Datastore`, `AttemptStore`, `OneTimeStore`, `TokenBlacklist`, `MessageBroker`, and `InstanceRegistry`.

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
