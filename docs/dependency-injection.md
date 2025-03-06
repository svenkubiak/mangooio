# Dependency Injection

Mangoo I/O leverages [Google Guice](https://github.com/google/guice) for **dependency injection**.

## Injecting Dependencies

The simplest way to inject a dependency is by defining it as a **member variable** using the `@Inject` annotation:

```java
@Inject
private MyClass myClass;
```

## Retrieving an Instance Manually

You can also obtain an instance of a class using the static method `Application.getInstance()`:

```java
MyClass myClass = Application.getInstance(MyClass.class);
```

## Constructor Injection (Recommended)

The **preferred approach** for dependency injection is through the **constructor**, ensuring immutability and better testability:

```java
private final Foo foo;

@Inject
public MyClass(Foo foo) {
    this.foo = Objects.requireNonNull(foo);
}
```

Constructor injection enforces dependency availability and prevents accidental modification of injected dependencies.

---

This document is optimized for **MkDocs Material** with proper formatting and improved readability.

