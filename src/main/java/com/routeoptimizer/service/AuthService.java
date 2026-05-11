package com.routeoptimizer.service;

import java.util.Base64;

import com.routeoptimizer.model.entity.Driver;
import com.routeoptimizer.dto.AuthenticationResponse;
import com.routeoptimizer.dto.LoginRequest;
import com.routeoptimizer.dto.RegisterRequest;
import com.routeoptimizer.dto.PasswordResetRequest;
import com.routeoptimizer.dto.UserResponseDTO;
import com.routeoptimizer.model.entity.User;
import com.routeoptimizer.model.enums.DriverStatus;
import com.routeoptimizer.model.enums.Role;
import com.routeoptimizer.repository.UserRepository;
import com.routeoptimizer.service.SystemAuditService;

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
  private final SystemAuditService auditService;

  public AuthService(UserRepository userRepository,
      com.routeoptimizer.repository.DriverRepository driverRepository, PasswordEncoder passwordEncoder,
      JwtService jwtService,
      AuthenticationManager authenticationManager,
      SystemAuditService auditService) {
    this.userRepository = userRepository;
    this.driverRepository = driverRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
    this.authenticationManager = authenticationManager;
    this.auditService = auditService;
  }

  @Transactional
  public AuthenticationResponse register(RegisterRequest request) {
    // 1. Obtener el usuario que está realizando la acción (El Creador)
    var authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
    User creator = null;
    if (authentication != null && authentication.getPrincipal() instanceof User) {
      creator = (User) authentication.getPrincipal();
    }

    // 2. Validaciones de Jerarquía
    if (creator == null) {
      throw new RuntimeException("Acceso denegado: Se requiere autenticación para registrar usuarios.");
    }

    Role creatorRole = creator.getRole();
    Role targetRole = request.role();

    // Regla A: El Logístico solo puede crear Drivers
    if (creatorRole == Role.LOGISTICS && targetRole != Role.DRIVER) {
      auditService.log(creator.getEmail(), "CREATE_USER_DENIED", "WARNING", "Intento de Logístico de crear un " + targetRole);
      throw new RuntimeException("Error: Un Operador Logístico solo tiene permitido crear repartidores (DRIVER).");
    }

    // Regla B: El Admin puede crear Logística o Drivers, pero NO Super Admins
    if (creatorRole == Role.ADMIN && targetRole == Role.SUPER_ADMIN) {
      auditService.log(creator.getEmail(), "CREATE_USER_DENIED", "CRITICAL", "Intento de Admin de crear un SUPER_ADMIN");
      throw new RuntimeException("Error: Un Administrador no tiene permiso para crear un Super Admin.");
    }

    // Regla C: Solo el Super Admin o Admin pueden crear perfiles de gestión
    if (creatorRole != Role.SUPER_ADMIN && creatorRole != Role.ADMIN && (targetRole == Role.ADMIN || targetRole == Role.LOGISTICS)) {
      auditService.log(creator.getEmail(), "CREATE_USER_DENIED", "CRITICAL", "Fallo de jerarquía en creación de usuario");
      throw new RuntimeException("Error: No tienes permisos para crear perfiles de gestión (ADMIN/LOGISTICS).");
    }

    // 3. Validar que el email no exista ya (sin importar mayúsculas/minúsculas)
    if (userRepository.findByEmailIgnoreCase(request.email()).isPresent()) {
      throw new RuntimeException("El correo '" + request.email() + "' ya se encuentra registrado en el sistema. Intenta con otro, o verifica si ya tienes cuenta.");
    }

    String decodedPassword = decodePassword(request.password());

    User user;
    if (targetRole == Role.DRIVER) {
      // Ajuste: Si el request trae ciudad la usamos, si no, la del jefe
      String targetCity = (request.assignedCity() != null && !request.assignedCity().isBlank()) 
                          ? request.assignedCity() 
                          : ((creatorRole == Role.LOGISTICS) ? creator.getAssignedCity() : "Bogotá");


      Driver rep = new Driver();
      rep.setName(request.name());
      rep.setEmail(request.email());
      rep.setPasswordHash(passwordEncoder.encode(decodedPassword));
      rep.setPhone(request.phone());
      rep.setRole(targetRole);
      rep.setStatus(DriverStatus.AVAILABLE);
      rep.setMaxCapacity(25);
      rep.setCostPerHour(request.costPerHour() != null ? request.costPerHour() : new java.math.BigDecimal("15000.00"));
      rep.setAssignedCity(targetCity);
      user = driverRepository.save(rep);
    } else {
      user = new User();
      user.setName(request.name());
      user.setEmail(request.email());
      user.setPasswordHash(passwordEncoder.encode(decodedPassword));
      user.setPhone(request.phone());
      user.setRole(targetRole);
      
      String targetCity = (request.assignedCity() != null && !request.assignedCity().isBlank()) 
                          ? request.assignedCity() 
                          : (targetRole == Role.SUPER_ADMIN ? "Global" : "Colombia");
      user.setAssignedCity(targetCity);
      user = userRepository.save(user);
    }

    auditService.log(creator.getEmail(), "USER_CREATED", "AUDIT", "Usuario creado: " + user.getEmail() + " con rol " + user.getRole());

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
    var user = userRepository.findByEmailIgnoreCase(request.email())
        .orElseThrow();
    var jwtToken = jwtService.generateToken(user);
    
    auditService.log(request.email(), "LOGIN_SUCCESS", "INFO", "Inicio de sesión exitoso");

    UserResponseDTO userDto = new UserResponseDTO(user.getId(), user.getEmail(), user.getName(), user.getPhone(),
        user.getRole());
    return new AuthenticationResponse(jwtToken, userDto);
  }

  @Transactional
  public void resetPassword(PasswordResetRequest request) {
    var user = userRepository.findByEmailIgnoreCase(request.email())
        .orElseThrow(() -> new RuntimeException("No se encontró ningún usuario con ese correo electrónico."));

    // Validar el teléfono secreto como 2FA simulado
    if (user.getPhone() == null || !user.getPhone().equals(request.phone().trim())) {
        auditService.log(request.email(), "PASSWORD_RESET_FAILED", "WARNING", "Intento fallido de recuperación (Teléfono no coincide)");
        throw new RuntimeException("Verificación de seguridad fallida. El número de teléfono no coincide con nuestros registros.");
    }

    // Guardar temporalmente en el limbo de aprobación
    String decodedPassword = decodePassword(request.newPassword());
    user.setPendingPasswordReset(true);
    user.setPendingPasswordHash(passwordEncoder.encode(decodedPassword));
    userRepository.save(user);

    auditService.log(request.email(), "PASSWORD_RESET_REQUESTED", "INFO", "Solicitud de recuperación de clave enviada para escrutinio (Aprobación Arquitecto requerida)");
  }

  private String decodePassword(String encodedPassword) {
    if (encodedPassword == null) {
      return encodedPassword;
    }

    try {
      return new String(Base64.getDecoder().decode(encodedPassword));
    } catch (Exception e) {
      // Si no es Base64 válido, devolvemos el original
      return encodedPassword;
    }
  }
}
