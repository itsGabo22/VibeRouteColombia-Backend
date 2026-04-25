package com.routeoptimizer.config;

import com.routeoptimizer.model.entity.User;
import com.routeoptimizer.model.enums.Role;
import com.routeoptimizer.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Sincronizador Maestro de Usuarios y Esquema.
 * Forzado para ejecutarse al final del arranque de la aplicación.
 * Repara la base de datos eliminando restricciones antiguas que bloquean el Super Admin.
 */
@Component
public class UserSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    public UserSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder, JdbcTemplate jdbcTemplate) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Value("${ADMIN_EMAIL:superadmin@viberoute.com}")
    private String adminEmail;

    @Value("${ADMIN_PASSWORD:viberoute_master}")
    private String adminPassword;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0 || userRepository.findByEmail(adminEmail).isEmpty()) {
            System.out.println("🚀 [SEEDER] Iniciando sincronización de infraestructura maestra...");

            // PASO 1: Eliminar restricción de base de datos que bloquea SUPER_ADMIN (PostgreSQL fix)
            try {
                jdbcTemplate.execute("ALTER TABLE users DROP CONSTRAINT IF EXISTS users_role_check");
                System.out.println("🔨 [DATABASE] Restricción de roles eliminada con éxito.");
            } catch (Exception sqlEx) {
                System.out.println("⚠️ [DATABASE] No se pudo eliminar la restricción (puede que no exista): " + sqlEx.getMessage());
            }

            // PASO 2: Sincronizar el Super Admin usando variables de entorno
            String hashedMasterPass = passwordEncoder.encode(adminPassword);

            userRepository.findByEmail(adminEmail).ifPresentOrElse(
                user -> {
                    user.setRole(Role.SUPER_ADMIN);
                    user.setPasswordHash(hashedMasterPass);
                    user.setName("Arquitecto Maestro (VibeRoute)");
                    user.setAssignedCity("Global");
                    userRepository.save(user);
                    System.out.println("✅ [SYNC] Usuario '" + adminEmail + "' ACTUALIZADO a SUPER_ADMIN.");
                },
                () -> {
                    User superAdmin = new User();
                    superAdmin.setName("Arquitecto Maestro (VibeRoute)");
                    superAdmin.setEmail(adminEmail);
                    superAdmin.setPasswordHash(hashedMasterPass);
                    superAdmin.setRole(Role.SUPER_ADMIN);
                    superAdmin.setAssignedCity("Global");
                    superAdmin.setPhone("3000000000");
                    userRepository.save(superAdmin);
                    System.out.println("⭐ [SEEDER] Super Admin creado con éxito: " + adminEmail);
                }
            );

            System.out.println("=".repeat(60) + "\n\n");
        }
    }
}
