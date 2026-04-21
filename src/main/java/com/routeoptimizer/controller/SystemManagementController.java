package com.routeoptimizer.controller;

import com.routeoptimizer.model.entity.SystemAudit;
import com.routeoptimizer.service.SystemAuditService;
import com.routeoptimizer.service.SystemAuditService;
import com.routeoptimizer.repository.UserRepository;
import com.routeoptimizer.model.entity.User;
import com.routeoptimizer.model.enums.Role;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Base64;
import com.routeoptimizer.model.entity.Driver;
import org.springframework.security.crypto.password.PasswordEncoder;

@RestController
@RequestMapping("/api/v1/system")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class SystemManagementController {

    private final SystemAuditService auditService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public SystemManagementController(SystemAuditService auditService, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.auditService = auditService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Obtener los últimos logs del sistema.
     */
    @GetMapping("/logs")
    public ResponseEntity<List<SystemAudit>> getSystemLogs(@RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(auditService.getLatestLogs(limit));
    }

    /**
     * Obtener lista global de todos los usuarios (para gestión masiva).
     */
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    /**
     * Borrado crítico de usuarios.
     * Solo accesible por SUPER_ADMIN.
     */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        User userToDelete = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        // El Super Admin no puede borrarse a sí mismo por accidente desde aquí
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (userToDelete.getEmail().equals(auth.getName())) {
             throw new RuntimeException("Operación denegada: No puedes eliminar tu propia cuenta de arquitecto.");
        }

        userRepository.deleteById(id);
        auditService.log(auth.getName(), "USER_DELETED_PERMANENT", "CRITICAL", "Eliminación total del usuario: " + userToDelete.getEmail());
        
        return ResponseEntity.noContent().build();
    }

    /**
     * Edición profunda de usuarios.
     * Permite al Super Admin modificar propiedades e incluso forzar reseteo de contraseña.
     */
    @PutMapping("/users/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody com.routeoptimizer.dto.RegisterRequest request) {
        User userToEdit = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();

        userToEdit.setName(request.name());
        userToEdit.setPhone(request.phone());
        userToEdit.setRole(request.role());
        
        if (request.assignedCity() != null && !request.assignedCity().isBlank()) {
            userToEdit.setAssignedCity(request.assignedCity());
        }

        if (request.password() != null && !request.password().isBlank()) {
            try {
                String decodedPassword = new String(Base64.getDecoder().decode(request.password()));
                userToEdit.setPasswordHash(passwordEncoder.encode(decodedPassword));
                auditService.log(auth.getName(), "PASSWORD_RESET", "CRITICAL", "Reinicio forzado de contraseña para: " + userToEdit.getEmail());
            } catch (Exception e) {
                 // Si no es base64
                 userToEdit.setPasswordHash(passwordEncoder.encode(request.password()));
            }
        }

        if (userToEdit instanceof Driver driver) {
            if (request.costPerHour() != null) {
                driver.setCostPerHour(request.costPerHour());
            }
        }

        User updatedUser = userRepository.save(userToEdit);
        auditService.log(auth.getName(), "USER_EDITED", "INFO", "Perfil modificado: " + updatedUser.getEmail());

        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Resuelve (aprueba o rechaza) un reseteo de contraseña pendiente (Tercera Barrera).
     */
    @PostMapping("/users/{id}/resolve-reset")
    public ResponseEntity<java.util.Map<String, String>> resolvePasswordReset(
            @PathVariable Long id,
            @RequestParam boolean approved) {
        
        User targetUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
                
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();

        if (Boolean.FALSE.equals(targetUser.getPendingPasswordReset())) {
             throw new RuntimeException("El usuario no tiene una solicitud de reseteo pendiente.");
        }

        if (approved) {
             targetUser.setPasswordHash(targetUser.getPendingPasswordHash());
             auditService.log(auth.getName(), "PASSWORD_RESET_APPROVED", "CRITICAL", "Aprobación de Tercera Barrera: Contraseña actualizada para " + targetUser.getEmail());
        } else {
             auditService.log(auth.getName(), "PASSWORD_RESET_DENIED", "WARNING", "Denegación de Tercera Barrera: Solicitud rechazada para " + targetUser.getEmail());
        }

        // Limpiar el estado pendiente independientemente de si se aprobó o rechazó.
        targetUser.setPendingPasswordReset(false);
        targetUser.setPendingPasswordHash(null);
        userRepository.save(targetUser);

        return ResponseEntity.ok(java.util.Map.of("message", approved ? "Reseteo aprobado y aplicado" : "Reseteo rechazado y limpiado"));
    }
}
