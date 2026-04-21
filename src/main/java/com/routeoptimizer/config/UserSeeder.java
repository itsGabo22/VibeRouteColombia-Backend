package com.routeoptimizer.config;

import com.routeoptimizer.model.entity.User;
import com.routeoptimizer.model.enums.Role;
import com.routeoptimizer.repository.UserRepository;
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

    @Override
    @Transactional
    public void run(String... args) {
        try {
            System.out.println("\n\n" + "=".repeat(60));
            System.out.println("🧙 [SYSTEM] INICIANDO REPARACIÓN DE ESQUEMA Y ROLES");
            System.out.println("=".repeat(60));

            // PASO 1: Eliminar restricción de base de datos que bloquea SUPER_ADMIN (PostgreSQL fix)
            try {
                jdbcTemplate.execute("ALTER TABLE users DROP CONSTRAINT IF EXISTS users_role_check");
                System.out.println("🔨 [DATABASE] Restricción de roles eliminada con éxito.");
            } catch (Exception sqlEx) {
                System.out.println("⚠️ [DATABASE] No se pudo eliminar la restricción (puede que no exista): " + sqlEx.getMessage());
            }

            // PASO 2: Sincronizar el Super Admin
            String masterEmail = "superadmin@viberoute.com";
            String hashedMasterPass = passwordEncoder.encode("viberoute_master");

            userRepository.findByEmail(masterEmail).ifPresentOrElse(
                user -> {
                    user.setRole(Role.SUPER_ADMIN);
                    user.setPasswordHash(hashedMasterPass);
                    user.setName("Arquitecto Maestro (VibeRoute)");
                    user.setAssignedCity("Global");
                    userRepository.save(user);
                    System.out.println("✅ [SYNC] Usuario '" + masterEmail + "' ACTUALIZADO a SUPER_ADMIN.");
                },
                () -> {
                    User newUser = new User();
                    newUser.setEmail(masterEmail);
                    newUser.setPasswordHash(hashedMasterPass);
                    newUser.setName("Arquitecto Maestro (VibeRoute)");
                    newUser.setRole(Role.SUPER_ADMIN);
                    newUser.setAssignedCity("Global");
                    userRepository.save(newUser);
                    System.out.println("✨ [SYNC] Usuario '" + masterEmail + "' CREADO como SUPER_ADMIN.");
                }
            );

            System.out.println("=".repeat(60) + "\n\n");

        } catch (Exception e) {
            System.err.println("❌ ERROR CRÍTICO EN SEEDER: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
