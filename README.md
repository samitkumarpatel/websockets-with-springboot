# websocket-with-springboot
This is a simple example of how to deal with websocket with spring boot.


### Documentation
[Websocket with Spring Boot MVC](https://docs.spring.io/spring-framework/reference/web/websocket.html)

[Websocket with Spring Boot WebFlux](https://docs.spring.io/spring-framework/reference/web-reactive.html)

## WebSocket
- WebSocket is a communication protocol that provides a full-duplex channel over a single, long-lived TCP connection.
- It allows a client (typically a web browser) and a server to exchange messages in real time with minimal overhead, making it ideal for interactive applications like chat apps, live notifications, gaming, and financial data feeds.

## HTTP vs WebSocket
| Feature| HTTP|WebSocket|
|--------|-----|---------|
|Nature	| Request-Response (unidirectional)  | 	Full-duplex (bidirectional)|
Connection	| Short-lived; one request per connection	| Persistent; single connection for multiple messages |
Latency| 	High (requires new connection setup)	| Low (no need to re-establish connection)           |
Overhead| 	Higher (repeated headers per request)	| Lower (single connection with minimal headers)     |
Use Case| 	Static or less interactive content	| Real-time, interactive applications                |

> Websocket protocol does not / might not work with all proxies, firewalls, and antivirus software. So, it is always good to have a fallback mechanism (like SockJs) or an alternative like long poling.

## SockJs
- SockJS is a JavaScript library that provides a WebSocket-like API but with automatic fallback mechanisms for environments where WebSockets are not supported.
- It ensures real-time communication between clients and servers, even when WebSockets are blocked by firewalls, proxies, or outdated browsers.
- Acts as a WebSocket wrapper, providing a consistent API.
- If WebSockets aren’t available, it automatically switches to alternative transport methods like:
  - XHR Streaming
  - XHR Polling
  - JSONP Polling
  - EventSource (for servers supporting Server-Sent Events)
  - Helps establish a persistent connection across various network conditions.
  - [SockJs JavaScript Library](https://github.com/sockjs/sockjs-client).

----------------------------------------------------------------------------------------------------------------------------

## STOMP
- STOMP = Simple Text Oriented Messaging Protocol , was originally created for scripting languages (such as Ruby, Python, and Perl) to connect to enterprise message brokers.
- STOMP is a lightweight, simple text-based protocol designed for communication between clients and message brokers like RabbitMQ, ActiveMQ, etc.
- STOMP can be used over any reliable two-way streaming network protocol, such as TCP and WebSocket.
- Although STOMP is a text-oriented protocol, message payloads can be either text or binary.
  - STOMP over WebSockets provides several advantages, particularly for real-time, message-driven applications like:
    - Structured Messaging Over WebSockets (STOMP frames)
      ```sh
      COMMAND [SEND, SUBSCRIBE, SEND, MESSAGE, etc.]
      header1:value1
      header2:value2
    
      Body^@
      ```
    - Pub-Sub (Publish-Subscribe) Model Support
    - Integration with Message Brokers (RabbitMQ, ActiveMQ, etc.)
    - Better Decoupling Between Frontend & Backend
    - Acknowledgment & Message Delivery Guarantees
    - When Should You Use WebSocket with STOMP?
      - Real-time chat applications
      - Live notifications
      - Multiplayer games
      - Financial data feeds
      - Collaborative editing tools
      - IoT (Internet of Things) applications

# Benefits by Using STOMP over websocker/SockJs with springboot

- No need to invent a custom messaging protocol and message format.

- STOMP clients, including a Java client in the Spring Framework, are available.

- You can (optionally) use message brokers (such as RabbitMQ, ActiveMQ, and others) to manage subscriptions and broadcast messages.

- Application logic can be organized in any number of @Controller instances and messages can be routed to them based on the STOMP destination header versus handling raw WebSocket messages with a single WebSocketHandler for a given connection.

- You can use Spring Security to secure messages based on STOMP destinations and message types.


## Implementation

### MVC

We need to have these dependency in our `pom.xml` file.

```xml
    <dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-websocket</artifactId>
</dependency>
```
1.) Need a Handler (WebSocketHandler, TextWebSocketHandler or BinaryWebSocketHandler for WebSocket session)
2.) WebSocketConfiguration to register WebSocketHandler
3.) Custom HandshakeInterceptor to add attributes to WebSocketSession

Extend this with SockJs to support fallback options for browsers that don't support WebSocket.

### WebFlux

We need to have these dependency in our `pom.xml` file.

```xml
    <dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
```