package com.fregence.fregence.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-notifications")
                // VACİB: Netlify linkini və local ünvanları bura mütləq əlavə etməlisən
                .setAllowedOriginPatterns(
                    "http://localhost:5173", 
                    "http://127.0.0.1:5173", 
                    "https://miparfume.netlify.app"
                )
                .withSockJS();
    }
}