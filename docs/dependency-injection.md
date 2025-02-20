For dependency injections mangoo I/O uses [Google Guice](https://github.com/google/guice).

The simplest use case for dependency injection is to inject a class as a member variable.

```java
@Inject
private MyClass myClass;
```

You can also grap an instance of a class using the static function of the Application class.

```java
MyClass myClass = Application.getInstance(MyClass.class);
```

The most recommended approach is to use injection through the constructor.

```java
private Foo foo;

@Inject
public MyClass(Foo foo) {
    this.foo = Objects.requireNonNull(foo);
}
```

