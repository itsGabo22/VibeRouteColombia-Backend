package com.routeoptimizer.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configuración oficial de WebSockets (STOMP) para VibeRoute Colombia.
 * Este "túnel" permitirá hablar con el celular del repartidor instantáneamente.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(@NonNull MessageBrokerRegistry config) {
        // /topic se usará para mandar los "rayos de información" (alertas de vibración) al celular
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(@NonNull StompEndpointRegistry registry) {
        // El frontend React / Vite se conectará a "wss://tuservidor/ws-alertas"
        registry.addEndpoint("/ws-alertas")
                .setAllowedOriginPatterns("*") // Mantenemos el CORS abierto para que Vite se conecte fácil
                .withSockJS(); // Fallback para celulares viejos
    }
}
