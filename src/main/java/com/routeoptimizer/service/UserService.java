package com.routeoptimizer.service;

import com.routeoptimizer.model.entity.Driver;
import com.routeoptimizer.model.entity.User;
import com.routeoptimizer.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SystemAuditService auditService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, SystemAuditService auditService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Transactional
    public void deleteUser(Long id, String adminEmail) {
        User userToDelete = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        if (userToDelete.getEmail().equals(adminEmail)) {
             throw new RuntimeException("Operación denegada: No puedes eliminar tu propia cuenta de arquitecto.");
        }

        userRepository.deleteById(id);
        auditService.log(adminEmail, "USER_DELETED_PERMANENT", "CRITICAL", "Eliminación total del usuario: " + userToDelete.getEmail());
    }

    @Transactional
    public User updateUser(Long id, com.routeoptimizer.dto.RegisterRequest request, String adminEmail) {
        User userToEdit = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

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
                auditService.log(adminEmail, "PASSWORD_RESET", "CRITICAL", "Reinicio forzado de contraseña para: " + userToEdit.getEmail());
            } catch (Exception e) {
                 userToEdit.setPasswordHash(passwordEncoder.encode(request.password()));
            }
        }

        if (userToEdit instanceof Driver driver) {
            if (request.costPerHour() != null) {
                driver.setCostPerHour(request.costPerHour());
            }
        }

        User updatedUser = userRepository.save(userToEdit);
        auditService.log(adminEmail, "USER_EDITED", "INFO", "Perfil modificado: " + updatedUser.getEmail());

        return updatedUser;
    }

    @Transactional
    public void resolvePasswordReset(Long id, boolean approved, String adminEmail) {
        User targetUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (Boolean.FALSE.equals(targetUser.getPendingPasswordReset())) {
             throw new RuntimeException("El usuario no tiene una solicitud de reseteo pendiente.");
        }

        if (approved) {
             targetUser.setPasswordHash(targetUser.getPendingPasswordHash());
             auditService.log(adminEmail, "PASSWORD_RESET_APPROVED", "CRITICAL", "Aprobación de Tercera Barrera: Contraseña actualizada para " + targetUser.getEmail());
        } else {
             auditService.log(adminEmail, "PASSWORD_RESET_DENIED", "WARNING", "Denegación de Tercera Barrera: Solicitud rechazada para " + targetUser.getEmail());
        }

        targetUser.setPendingPasswordReset(false);
        targetUser.setPendingPasswordHash(null);
        userRepository.save(targetUser);
    }
}
