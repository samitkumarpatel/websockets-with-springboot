# websocket-with-springboot
This is a simple example of how to deal with websocket with spring boot.


### Documentation
[Websocket with Spring Boot MVC](https://docs.spring.io/spring-framework/reference/web/websocket.html)

[Websocket with Spring Boot WebFlux](https://docs.spring.io/spring-framework/reference/web-reactive.html)

## WebSocket
WebSocket is a communication protocol that provides a full-duplex channel over a single, long-lived TCP connection. It allows a client (typically a web browser) and a server to exchange messages in real time with minimal overhead, making it ideal for interactive applications like chat apps, live notifications, gaming, and financial data feeds.

## HTTP vs WebSocket
| Feature| HTTP|WebSocket|
|--------|-----|---------|
|Nature	| Request-Response (unidirectional)  | 	Full-duplex (bidirectional)|
Connection	| Short-lived; one request per connection	| Persistent; single connection for multiple messages |
Latency| 	High (requires new connection setup)	| Low (no need to re-establish connection)           |
Overhead| 	Higher (repeated headers per request)	| Lower (single connection with minimal headers)     |
Use Case| 	Static or less interactive content	| Real-time, interactive applications                |

## SockJs
SockJS is a JavaScript library that provides a WebSocket-like API but with automatic fallback mechanisms for environments where WebSockets are not supported. It ensures real-time communication between clients and servers, even when WebSockets are blocked by firewalls, proxies, or outdated browsers.

- Acts as a WebSocket wrapper, providing a consistent API.
- If WebSockets aren’t available, it automatically switches to alternative transport methods like:
   - XHR Streaming
   - XHR Polling
   - JSONP Polling
   - EventSource (for servers supporting Server-Sent Events)
   - Helps establish a persistent connection across various network conditions.

## STOMP
- STOMP = Simple Text Oriented Messaging Protocol
- STOMP is a lightweight, simple text-based protocol designed for communication between clients and message brokers like RabbitMQ, ActiveMQ, etc.
- STOMP over WebSockets provides several advantages, particularly for real-time, message-driven applications like:
  - Structured Messaging Over WebSockets (STOMP frames)
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