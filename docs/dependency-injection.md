# Dependency injection

mangoo I/O uses [Google Guice](https://github.com/google/guice). Bindings belong in `app.Module`. The framework already binds `Config`, `Vault`, `Cache`, and `Datastore`, and publishes every `config.yaml` entry as `@Named` strings.

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
