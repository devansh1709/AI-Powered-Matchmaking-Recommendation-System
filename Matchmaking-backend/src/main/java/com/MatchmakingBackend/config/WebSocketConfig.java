package com.MatchmakingBackend.config;

import com.MatchmakingBackend.security.StompAuthChannelInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
	private final StompAuthChannelInterceptor stompAuthChannelInterceptor;
	private final String[] allowedOrigins;

	public WebSocketConfig(StompAuthChannelInterceptor stompAuthChannelInterceptor,
						   @Value("${app.cors.allowed-origins}") String allowedOrigins) {
		this.stompAuthChannelInterceptor = stompAuthChannelInterceptor;
		this.allowedOrigins = allowedOrigins.split(",");
	}

	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		registry.enableSimpleBroker("/topic");        // server -> client broadcasts, e.g. /topic/conversations/5
		registry.setApplicationDestinationPrefixes("/app"); // client -> server sends (if you add @MessageMapping later)
	}

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/ws")
				.setAllowedOrigins(allowedOrigins)
				.withSockJS(); // fallback for browsers/proxies that block raw WebSocket
	}

	@Override
	public void configureClientInboundChannel(ChannelRegistration registration) {
		registration.interceptors(stompAuthChannelInterceptor);
	}
}