Debugging in mangoo I/O can be done in different ways. Probably the best use case is to connect via jpda. By default, mangoo I/O opens a jpda connection at Port 8000 which you can connect via your IDE of choice. Please note, that you require the mangooio-maven-plugin to use this feature.

```properties
<plugin>
    <groupId>io.mangoo</groupId>
    <artifactId>mangooio-maven-plugin</artifactId>
    <version>${mangooio.version}</version>
</plugin>
```

You can customize the jpda port by setting and additional attribute


```properties
<plugin>
    <groupId>io.mangoo</groupId>
    <artifactId>mangooio-maven-plugin</artifactId>
    <version>${mangooio.version}</version>
    <configuration>
    	<jpdaPort>8090</jpdaPort>
    </configuration>
</plugin>
```

As an alternative you could create a Class with a simple Main method and start your application as follows in debug in your IDE of choice.

```java
package main;

import io.mangoo.core.Application;
import io.mangoo.enums.Mode;

public final class Main {

    private Main(){
    }

    public static void main(String... args) {
        System.setProperty("application.mode", Mode.DEV.toString());
        Application.main("");
    }
}

```



