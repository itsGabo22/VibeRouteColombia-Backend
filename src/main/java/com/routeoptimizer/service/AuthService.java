package com.routeoptimizer.service;

import java.util.Base64;

import com.routeoptimizer.model.entity.Driver;
import com.routeoptimizer.dto.AuthenticationResponse;
import com.routeoptimizer.dto.LoginRequest;
import com.routeoptimizer.dto.RegisterRequest;
import com.routeoptimizer.dto.UserResponseDTO;
import com.routeoptimizer.model.entity.User;
import com.routeoptimizer.model.enums.DriverStatus;
import com.routeoptimizer.model.enums.Role;
import com.routeoptimizer.repository.UserRepository;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio que maneja la lógica de autenticación y registro.
 */
@Service
public class AuthService {

  private final UserRepository userRepository;
  private final com.routeoptimizer.repository.DriverRepository driverRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final AuthenticationManager authenticationManager;

  public AuthService(UserRepository userRepository,
      com.routeoptimizer.repository.DriverRepository driverRepository, PasswordEncoder passwordEncoder,
      JwtService jwtService,
      AuthenticationManager authenticationManager) {
    this.userRepository = userRepository;
    this.driverRepository = driverRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
    this.authenticationManager = authenticationManager;
  }

  @Transactional
  public AuthenticationResponse register(RegisterRequest request) {
    // Validar que el email no exista ya
    if (userRepository.findByEmail(request.email()).isPresent()) {
      throw new RuntimeException("Ya existe un usuario registrado con el email: " + request.email());
    }

    String decodedPassword = decodePassword(request.password());

    User user;
    if (request.role() == Role.DRIVER) {
      Driver rep = new Driver();
      rep.setName(request.name());
      rep.setEmail(request.email());
      rep.setPasswordHash(passwordEncoder.encode(decodedPassword));
      rep.setPhone(request.phone());
      rep.setRole(request.role());
      rep.setStatus(DriverStatus.AVAILABLE);
      rep.setMaxCapacity(25);
      rep.setCostPerHour(request.costPerHour());
      user = driverRepository.save(rep);
    } else {
      user = new User();
      user.setName(request.name());
      user.setEmail(request.email());
      user.setPasswordHash(passwordEncoder.encode(decodedPassword));
      user.setPhone(request.phone());
      user.setRole(request.role());
      user = userRepository.save(user);
    }

    var jwtToken = jwtService.generateToken(user);
    UserResponseDTO userDto = new UserResponseDTO(user.getId(), user.getEmail(), user.getName(), user.getPhone(),
        user.getRole());
    return new AuthenticationResponse(jwtToken, userDto);
  }

  @Transactional
  public AuthenticationResponse login(LoginRequest request) {
    String decodedPassword = decodePassword(request.password());

    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            request.email(),
            decodedPassword));
    var user = userRepository.findByEmail(request.email())
        .orElseThrow();
    var jwtToken = jwtService.generateToken(user);
    UserResponseDTO userDto = new UserResponseDTO(user.getId(), user.getEmail(), user.getName(), user.getPhone(),
        user.getRole());
    return new AuthenticationResponse(jwtToken, userDto);
  }

  private String decodePassword(String encodedPassword) {
    try {
      return new String(Base64.getDecoder().decode(encodedPassword));
    } catch (Exception e) {
      // Si no es Base64 válido, devolvemos el original (para retrocompatibilidad o pruebas manuales)
      return encodedPassword;
    }
  }
}
