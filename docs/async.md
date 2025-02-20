# EventBus

mangoo I/O supports a simple [EventBus based on Google Guava](https://github.com/google/guava/wiki/EventBusExplained). The EventBus functionality is wrapped around the BusManager. Start using the EventBus by injecting the BusManager.

```java
@Inject
private EventBusService eventBus;
```

The BusManager provides convenient functions, such as register, unregister and publish, to work with the underlying EventBus. See the following example:

```java
MyListener myListener = new MyListener();
eventBus.register(myListener);
eventBus.publish("This is a test");
eventBus.unregister(myListener);

```

You also find some useful statistic functions in the BusManager class.

# WebSockets

To use WebSockets in mangoo I/O you have to extend the MangooWebSocket class in your WebSocket controller. Extending this class offers you the entry point for using WebSockets specific methods.

```java
package controllers;

import io.undertow.websockets.core.BufferedBinaryMessage;
import io.undertow.websockets.core.BufferedTextMessage;
import io.undertow.websockets.core.CloseMessage;
import io.undertow.websockets.core.WebSocketChannel;
import io.mangoo.interfaces.MangooWebSocket;

public class WebSocketController extends MangooWebSocket {
    @Override
    protected void onFullTextMessage(WebSocketChannel channel, BufferedTextMessage message) {
        //Do nothing for now
    }

    @Override
    protected void onFullBinaryMessage(WebSocketChannel channel, BufferedBinaryMessage message) {
        //Do nothing for now
    }

    @Override
    protected void onFullPongMessage(WebSocketChannel channel, BufferedBinaryMessage message) {
        //Do nothing for now
    }

    @Override
    protected void onCloseMessage(CloseMessage closeMessage,  WebSocketChannel channel) {
        //Do nothing for now
    }
}
```

See Routing for [Server-Sent Events / WebSockets](https://docs.mangoo.io/server-sent-events--websockets.html) on how the setup the URL mappings. 

As WebSockets are a bi-directional protocol, and the above descripted how to deal with incoming event, you can also sent outgoing events by using the WebSocketManager.

```java
@Inject
private WebSocketManager webSocketManager;

public void sentEvent() {
    webSocketManager.getChannels("/websocket").forEach(channel -> {...});
}
```

The above example enables you to access all clients which have an open WebSocket channel to the URL /websocket.

# Server-Sent Events

As Server-Sent events are an uni-directional protocol you can only send data to connected clients.

See Routing for [Server-Sent Events / WebSockets](https://docs.mangoo.io/server-sent-events--websockets.html) on how the setup the URL mappings for Server-Sent Events. 

To send outgoing Server-Sent Event data, you can use the ServerEventManager.

```java
@Inject
private ServerEventManager serverEventManager;

public void sentEvent() {
    serverEventManager.getConnections("/serversentevent").forEach(connection -> {connection.send("foo");});
}
```

The above example will send the data to all clients which have an open Servet-Sent Event connection to the URL /serversentevent.

# Concurrency

Although mangoo I/O is a web framework, there may be situations where you need to postpone a unit of work in the background and wait for it to finish.

For this purpose mangoo I/O offers the ExecutionManager, which is just a simple wrapper around the default Java ExecutorService.

```java
@Inject
private ExecutionManager executionManager;

public void doSomething() {
    Future future = executionManager.submit(new MyCallable());
}
```

The manager offers some convenient methods for postpone tasks into the background of your application. The ExecutorService works with a fixed Thread-Pool size with a default value of 10. You can change this value via the application.yaml file. See [default values](https://docs.mangoo.io/default-values.html) on how to change the Thead-Pool.

