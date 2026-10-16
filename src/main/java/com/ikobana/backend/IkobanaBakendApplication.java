package com.ikobana.backend;

import com.ikobana.backend.entity.User;
import com.ikobana.backend.entity.UserRole;
import com.ikobana.backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Entry point for the Ikobana Frozen Food Backend application.
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.ikobana.backend")
public class IkobanaBakendApplication {

    public static void main(String[] args) {
        SpringApplication.run(IkobanaBakendApplication.class, args);
    }

    /**
     * Initializes default seeds on application startup.
     * Seeds one dummy account per role (for testing / evaluation) if they do not already exist.
     */
    @Bean
    public CommandLineRunner init(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Akun dummy untuk penilaian (lihat README.md)
            seedUser(userRepository, passwordEncoder, "admin@ikobana.com", "admin",
                    "Admin@12345", "Administrator Ikobana", "0812-3456-7890", UserRole.ADMIN);
            seedUser(userRepository, passwordEncoder, "customer@ikobana.com", "customer",
                    "Customer@12345", "Customer Ikobana", "0812-9876-5432", UserRole.CUSTOMER);

            // Log application startup
            System.out.println("✅ Ikobana Frozen Food Backend - Started successfully!");
            System.out.println("📚 API Documentation: http://localhost:8080/swagger-ui.html");
            System.out.println("🔗 API Base URL: http://localhost:8080/api");
        };
    }

    private static void seedUser(UserRepository userRepository, PasswordEncoder passwordEncoder,
                                 String email, String username, String password,
                                 String fullName, String phone, UserRole role) {
        if (userRepository.existsByEmail(email)) {
            return;
        }
        User user = new User();
        user.setEmail(email);
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setFullName(fullName);
        user.setPhone(phone);
        user.setRole(role);
        user.setIsActive(true);
        userRepository.save(user);
        System.out.println("✅ " + role.name() + " user created: " + email);
    }
}
