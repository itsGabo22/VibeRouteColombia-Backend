package com.routeoptimizer.config;

import com.routeoptimizer.security.JwtAuthenticationEntryPoint;
import com.routeoptimizer.security.JwtAuthenticationFilter;
import com.routeoptimizer.security.CustomAccessDeniedHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Configuración de seguridad de Spring Security.
 * Define la cadena de filtros, políticas de sesión y permisos por rol.
 *
 * CORS Dinámico: Lee orígenes permitidos desde la variable de entorno
 * VIBEROUTE_CORS_ALLOWED (separados por coma). Si no se define, usa
 * valores seguros de desarrollo local.
 *
 * Ejemplo de valor en producción:
 *   VIBEROUTE_CORS_ALLOWED=http://localhost:3000,http://192.168.1.50:3000,https://viberoute.com
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthFilter;
  private final AuthenticationProvider authenticationProvider;
  private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
  private final CustomAccessDeniedHandler customAccessDeniedHandler;

  /**
   * Orígenes CORS permitidos, inyectados desde la variable de entorno.
   * Valor por defecto: localhost:3000 en HTTP y HTTPS + loopback IPv4.
   * Para pruebas de campo desde móviles en red local, añadir la IP del
   * equipo de desarrollo (ej: http://192.168.1.100:3000).
   */
  @Value("${VIBEROUTE_CORS_ALLOWED:http://localhost:3000,https://localhost:3000,http://127.0.0.1:3000}")
  private String allowedOriginsRaw;

  public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter, 
                        AuthenticationProvider authenticationProvider,
                        JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
                        CustomAccessDeniedHandler customAccessDeniedHandler) {
    this.jwtAuthFilter = jwtAuthFilter;
    this.authenticationProvider = authenticationProvider;
    this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
    this.customAccessDeniedHandler = customAccessDeniedHandler;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(auth -> auth
            // ── Públicos ────────────────────────────────────────────────────
            .requestMatchers(
                "/api/v1/auth/login", "/api/v1/auth/reset-password",
                "/api/v1/ping", "/error",
                "/auth/login"
            ).permitAll()

            // ── WebSocket SockJS: Handshake + iframe transport ──────────────
            // Estos endpoints son HTTP puro (GET/POST) durante el upgrade;
            // deben pasar sin JWT porque SockJS los negocia antes del CONNECT.
            .requestMatchers("/ws-alertas/**").permitAll()

            // ── Registro restringido a roles administrativos ────────────────
            .requestMatchers("/api/v1/auth/register", "/auth/register")
                .hasAnyRole("SUPER_ADMIN", "ADMIN", "LOGISTICS")

            // ── Super Admin exclusivo ───────────────────────────────────────
            .requestMatchers("/api/v1/system/**", "/system/**")
                .hasRole("SUPER_ADMIN")
            
            // ── Acciones específicas del DRIVER ─────────────────────────────
            .requestMatchers(HttpMethod.PATCH, "/api/v1/drivers/*/status", "/drivers/*/status")
                .hasAnyRole("DRIVER", "ADMIN", "SUPER_ADMIN")
            .requestMatchers(HttpMethod.PATCH, "/api/v1/orders/*/status", "/orders/*/status")
                .hasAnyRole("DRIVER", "LOGISTICS", "ADMIN", "SUPER_ADMIN")
            
            // ── Gestión de conductores (no DRIVER) ──────────────────────────
            .requestMatchers("/api/v1/drivers", "/api/v1/drivers/**", "/drivers", "/drivers/**")
                .hasAnyRole("ADMIN", "LOGISTICS", "SUPER_ADMIN")
            
            // ── Recursos compartidos (pedidos, stats, reportes, lotes) ──────
            .requestMatchers(
                "/api/v1/orders", "/api/v1/orders/**", "/orders", "/orders/**",
                "/api/v1/stats", "/api/v1/stats/**", "/stats", "/stats/**",
                "/api/v1/reports", "/api/v1/reports/**", "/reports", "/reports/**",
                "/api/v1/batches", "/api/v1/batches/**", "/batches", "/batches/**"
            ).hasAnyRole("DRIVER", "LOGISTICS", "ADMIN", "SUPER_ADMIN")
            
            // ── Monitoreo operacional ───────────────────────────────────────
            .requestMatchers(
                "/api/v1/batches/**", "/batches/**",
                "/api/v1/routes/**", "/routes/**",
                "/api/v1/locations/**", "/locations/**"
            ).hasAnyRole("DRIVER", "LOGISTICS", "ADMIN", "SUPER_ADMIN")
            
            // ── IA: cualquier usuario autenticado ───────────────────────────
            .requestMatchers("/api/v1/ai/**").authenticated()
            .anyRequest().authenticated())
        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling(exceptions -> exceptions
            .authenticationEntryPoint(jwtAuthenticationEntryPoint)
            .accessDeniedHandler(customAccessDeniedHandler))
        .authenticationProvider(authenticationProvider)
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  /**
   * Fuente de configuración CORS dinámica.
   * Parsea VIBEROUTE_CORS_ALLOWED (separado por comas) y registra
   * los orígenes permitidos. Nunca usa wildcard "*".
   */
  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    List<String> origins = Arrays.stream(allowedOriginsRaw.split(","))
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .toList();

    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(origins);
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(List.of(
        "Authorization", "Content-Type", "X-Requested-With",
        "Accept", "Origin", "Cache-Control"
    ));
    configuration.setExposedHeaders(List.of("Authorization"));
    configuration.setAllowCredentials(true);
    // Cachear la respuesta preflight 1 hora (reduce roundtrips desde móviles)
    configuration.setMaxAge(3600L);
    
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}
