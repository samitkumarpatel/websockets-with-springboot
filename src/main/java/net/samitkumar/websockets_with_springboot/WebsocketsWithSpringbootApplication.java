package net.samitkumar.websockets_with_springboot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.security.auth.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SpringBootApplication
@Slf4j
public class WebsocketsWithSpringbootApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebsocketsWithSpringbootApplication.class, args);
	}

	@Bean
	Map<String, WebSocketSession> sessions() {
		return new HashMap<>();
	}

}

record UserMessage(String id, String message) {}
record Message(String from, String to, String message) {}

// 1.) Need a Handler (WebSocketHandler, TextWebSocketHandler or BinaryWebSocketHandler for WebSocket session)
@Slf4j
@Component
@RequiredArgsConstructor
class TextMessageHandler extends TextWebSocketHandler {
	final Map<String, WebSocketSession> sessions;
	final ObjectMapper objectMapper;

	@Override
	@SneakyThrows
	protected void handleTextMessage(WebSocketSession session, TextMessage message) {
		log.info("Received message: SESSION:{} , MESSAGE: {}", session, message.getPayload());
		var userMessage = new UserMessage(session.getId(), message.getPayload());
		broadcast(session, userMessage);

	}

	@Override
	@SneakyThrows
	public void afterConnectionEstablished(WebSocketSession session) {

//		You can get the customise principal object from the session object like below
//		System.out.println(session.getPrincipal());

		log.info("Session established: {}", session);
		sessions.put(session.getId(), session);
		broadcast(session, new UserMessage("SYSTEM", "User %s Connected".formatted(session.getId())));
	}

	@Override
	@SneakyThrows
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
		log.info("Session closed: {}", session);
		sessions.remove(session.getId());
		broadcast(session, new UserMessage("SYSTEM", "User %s Disconnected".formatted(session.getId())));
	}

	@SneakyThrows
	void broadcast(WebSocketSession session, UserMessage message) {
		var outgoingMessage = objectMapper.writeValueAsString(
				new Message(message.id(), "ALL", message.message())
		);
		//send to all the users
		sessions.values().forEach(s -> {
			try {
				//if (session == s) return;
				s.sendMessage(new TextMessage(outgoingMessage));
			} catch (Exception e) {
				log.error("Error sending message to session: {}", s, e);
			}
		});
	}
}

// 2.) WebSocketConfiguration to register WebSocketHandler
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
class WebSocketConfiguration implements WebSocketConfigurer {
	final TextMessageHandler textMessageHandler;

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry
				.addHandler(textMessageHandler, "/text")
				.setAllowedOriginPatterns("*")
				.addInterceptors(new CustomHandshakeInterceptor())
				.setHandshakeHandler(new DefaultHandshakeHandler() {
					@Override
					protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
						var uuid = UUID.randomUUID().toString();
						attributes.put("UUID", uuid);
						return new UserPrincipal(uuid);
					}
				})
				//.withSockJS()
		;
	}
}

// 3.) Custom HandshakeInterceptor to add any useful attributes to WebSocketSession
@Slf4j
class CustomHandshakeInterceptor implements HandshakeInterceptor {

	@Override
	public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) {
		log.info("Handshake[Before]: PRINCIPAL {}, ATTRIBUTES {}", request.getPrincipal(), attributes);
		return true;

	}

	@Override
	public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
		log.info("Handshake[After]: PRINCIPAL {}, ATTRIBUTES {}", request.getPrincipal(), request.getAttributes());
	}
}