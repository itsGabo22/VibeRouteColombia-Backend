package com.routeoptimizer.controller;

import com.routeoptimizer.dto.AuthenticationResponse;
import com.routeoptimizer.dto.LoginRequest;
import com.routeoptimizer.dto.RegisterRequest;
import com.routeoptimizer.service.AuthService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador para gestionar el registro y la autenticación de usuarios.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  /**
   * Endpoint para registrar un nuevo usuario en el sistema.
   */
  @PostMapping("/register")
  public ResponseEntity<AuthenticationResponse> register(@jakarta.validation.Valid @RequestBody RegisterRequest request) {
    return ResponseEntity.ok(authService.register(request));
  }

  /**
   * Endpoint para iniciar sesión y obtener un JWT.
   */
  @PostMapping("/login")
  public ResponseEntity<AuthenticationResponse> login(@RequestBody LoginRequest request) {
    return ResponseEntity.ok(authService.login(request));
  }
}
