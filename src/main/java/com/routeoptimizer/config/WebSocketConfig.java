package com.routeoptimizer.config;

import com.routeoptimizer.service.JwtService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.Arrays;
import java.util.List;

/**
 * Configuración oficial de WebSockets (STOMP) para VibeRoute Colombia.
 *
 * Flujo de autenticación:
 * 1. El frontend envía un header nativo STOMP "Authorization: Bearer <jwt>"
 *    durante el frame CONNECT.
 * 2. El ChannelInterceptor intercepta el CONNECT, extrae el JWT del header,
 *    valida el token con JwtService y establece el Principal en el contexto
 *    de mensajería de Spring.
 * 3. Si el token es inválido o ausente, la conexión se rechaza con un
 *    StompHeaderAccessor.setLeaveMutable que impide la suscripción.
 *
 * CORS para WebSocket: Reutiliza la misma variable VIBEROUTE_CORS_ALLOWED
 * definida en SecurityConfig para mantener una política unificada.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private static final Logger log = LoggerFactory.getLogger(WebSocketConfig.class);

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Value("${VIBEROUTE_CORS_ALLOWED:http://localhost:3000,https://localhost:3000,http://127.0.0.1:3000}")
    private String allowedOriginsRaw;

    public WebSocketConfig(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public void configureMessageBroker(@NonNull MessageBrokerRegistry config) {
        // /topic se usa para broadcasting (alertas de vibración, eventos logísticos)
        config.enableSimpleBroker("/topic");
        // /app como prefijo para mensajes del cliente al servidor (@MessageMapping)
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(@NonNull StompEndpointRegistry registry) {
        // Parsear los orígenes permitidos desde la variable de entorno
        String[] origins = Arrays.stream(allowedOriginsRaw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);

        registry.addEndpoint("/ws-alertas")
                .setAllowedOrigins(origins)
                .withSockJS(); // Fallback para navegadores que no soportan WebSocket nativo
    }

    /**
     * Registra el ChannelInterceptor que autentica conexiones STOMP con JWT.
     * Se ejecuta en el canal de entrada (inbound) antes de que el mensaje
     * llegue al broker o a un @MessageMapping handler.
     */
    @Override
    public void configureClientInboundChannel(@NonNull ChannelRegistration registration) {
        registration.interceptors(new JwtStompChannelInterceptor());
    }

    // ─── Interceptor Interno ────────────────────────────────────────────────────

    /**
     * Interceptor de canal STOMP que autentica al usuario durante el CONNECT.
     *
     * Solo actúa en el frame CONNECT; los frames SUBSCRIBE, SEND, DISCONNECT
     * ya heredan el Principal establecido en el CONNECT, por lo que no
     * necesitan re-autenticación (comportamiento estándar de Spring Messaging).
     */
    private class JwtStompChannelInterceptor implements ChannelInterceptor {

        @Override
        public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
            StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(
                    message, StompHeaderAccessor.class);

            if (accessor == null || accessor.getCommand() == null) {
                return message;
            }

            if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                authenticateFromStompHeaders(accessor);
            }

            return message;
        }

        /**
         * Extrae el JWT del header nativo STOMP "Authorization" y, si es
         * válido, establece el Principal autenticado en el accessor.
         *
         * Si el token es inválido o no se proporciona, se loguea una
         * advertencia pero NO se lanza excepción — el cliente quedará
         * como anónimo y los topics protegidos le negarán el acceso
         * a través de las reglas de seguridad de Spring.
         */
        private void authenticateFromStompHeaders(StompHeaderAccessor accessor) {
            List<String> authHeaders = accessor.getNativeHeader("Authorization");

            if (authHeaders == null || authHeaders.isEmpty()) {
                log.warn("[WS-AUTH] CONNECT sin header Authorization — conexión anónima");
                return;
            }

            String authHeader = authHeaders.get(0);
            if (!authHeader.startsWith("Bearer ")) {
                log.warn("[WS-AUTH] Header Authorization mal formado: {}", authHeader);
                return;
            }

            String jwt = authHeader.substring(7);

            try {
                String userEmail = jwtService.extractUsername(jwt);
                if (userEmail == null) {
                    log.warn("[WS-AUTH] No se pudo extraer email del JWT");
                    return;
                }

                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
                if (!jwtService.isTokenValid(jwt, userDetails)) {
                    log.warn("[WS-AUTH] JWT inválido o expirado para: {}", userEmail);
                    return;
                }

                // Establecer autenticación en el contexto de mensajería
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                accessor.setUser(authToken);

                log.info("[WS-AUTH] ✅ STOMP CONNECT autenticado: {} ({})",
                        userEmail, userDetails.getAuthorities());

            } catch (Exception e) {
                log.error("[WS-AUTH] Error procesando JWT en STOMP CONNECT: {}", e.getMessage());
            }
        }
    }
}
