package com.routeoptimizer.config;

import com.routeoptimizer.security.JwtAuthenticationEntryPoint;
import com.routeoptimizer.security.JwtAuthenticationFilter;
import com.routeoptimizer.security.CustomAccessDeniedHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuración de seguridad de Spring Security.
 * Define la cadena de filtros, políticas de sesión y permisos por rol.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthFilter;
  private final AuthenticationProvider authenticationProvider;
  private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
  private final CustomAccessDeniedHandler customAccessDeniedHandler;

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
            .requestMatchers("/api/v1/auth/**", "/api/v1/ping", "/error", "/ws-alertas/**").permitAll()
            // Specific DRIVER actions must come BEFORE the broad admin-only rules
            .requestMatchers(HttpMethod.PATCH, "/api/v1/drivers/*/status").hasAnyRole("DRIVER", "ADMIN")
            .requestMatchers(HttpMethod.PATCH, "/api/v1/orders/*/status").hasAnyRole("DRIVER", "LOGISTICS", "ADMIN")
            // Broad role-based access rules
            .requestMatchers("/api/v1/drivers/**").hasRole("ADMIN")
            .requestMatchers("/api/v1/orders/**", "/api/v1/orders", "/api/v1/batches/**", "/api/v1/analytics/**")
            .hasAnyRole("LOGISTICS", "ADMIN")
            .requestMatchers("/api/v1/routes/**", "/api/v1/locations/**").hasAnyRole("DRIVER", "ADMIN")
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

  @Bean
  public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
    org.springframework.web.cors.CorsConfiguration configuration = new org.springframework.web.cors.CorsConfiguration();
    configuration.setAllowedOriginPatterns(java.util.List.of("http://localhost:5173", "https://*.vercel.app", "https://*.onrender.com"));
    configuration.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(java.util.List.of("Authorization", "Content-Type"));
    configuration.setExposedHeaders(java.util.List.of("Authorization"));
    configuration.setAllowCredentials(true);
    
    org.springframework.web.cors.UrlBasedCorsConfigurationSource source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}
