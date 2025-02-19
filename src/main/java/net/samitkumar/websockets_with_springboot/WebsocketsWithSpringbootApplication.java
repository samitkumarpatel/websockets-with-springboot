package net.samitkumar.websockets_with_springboot;

import com.sun.security.auth.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@SpringBootApplication
@Slf4j
public class WebsocketsWithSpringbootApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebsocketsWithSpringbootApplication.class, args);
	}

	@Bean
	Map<String, String> users() {
		return new HashMap<>();
	}

	@EventListener
	public void onSessionConnect(SessionConnectEvent event) {
		log.info("SessionConnectEvent: {}", event);
		var uuid = Objects.requireNonNull(event.getUser()).getName();
		var sessionId = (String) event.getMessage().getHeaders().get("simpSessionId");
		users().put(uuid, sessionId);
	}

	@EventListener
	public void onSessionDisconnect(SessionDisconnectEvent event) {
		log.info("SessionDisconnectEvent: {}", event);
		var uuid = Objects.requireNonNull(event.getUser()).getName();
		users().remove(uuid);
	}

}

record UserMessage(String from, String to, String message) {}


// 1.) Enable WebSocketMessageBroker
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
@Slf4j
class WebSocketConfiguration implements WebSocketMessageBrokerConfigurer {

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/stomp-endpoint")
				.setHandshakeHandler(new DefaultHandshakeHandler() {
					@Override
					protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
						var uuid = UUID.randomUUID().toString();
						attributes.put("UUID", uuid);
						log.info("UUID: {}", uuid);
						return new UserPrincipal(uuid);
					}
				})
				.setAllowedOriginPatterns("*")
				.withSockJS()
				.setHeartbeatTime(60_000);
	}

	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		registry.enableSimpleBroker("/queue/", "/topic/");
		// STOMP messages whose destination header begins with /app are routed to @MessageMapping methods in @Controller classes
		registry.setApplicationDestinationPrefixes("/app");
		registry.setUserDestinationPrefix("/user");
	}
}

// 2.) Controller to handle WebSocket messages
@Controller
@Slf4j
@RequiredArgsConstructor
class WebSocketController {

	final Map<String, String> users;
	final SimpMessagingTemplate messagingTemplate;
	final SimpMessageSendingOperations messagingSendingTemplate;

	@MessageMapping("/private")
	public void sendMessageToUser(@Payload UserMessage userMessage, @Headers Map<Object, Object> headers, Principal principal) {
		var sendTo = users.get(userMessage.to());
		log.info("sendMessageToUser sendTo: {} userMessage: {} Headers: {}",sendTo, userMessage, headers);

		//messagingTemplate.convertAndSendToUser(sendTo, "/queue/private", userMessage.message());
		messagingSendingTemplate.convertAndSendToUser( sendTo, "/queue/private", userMessage.message());
	}

	@MessageMapping("/public")
	@SendTo("/topic/public")
	public UserMessage sendGreetingToUser(@Payload UserMessage message, @Headers Map<Object, Object> headers, Principal principal) throws InterruptedException {
		log.info("Public Message Principle {} Headers: {}, {}", principal, headers, message);
		return message;
	}

	@GetMapping("/users")
	@CrossOrigin(originPatterns = "*")
	@ResponseBody
	public List<Map<String, String>> allUsers() {
		return users
				.entrySet()
				.stream()
				.map(entry -> Map.of("name", entry.getKey()))
				.toList();
	}
}