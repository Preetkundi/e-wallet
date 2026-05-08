package com.sterling.user.config;

import com.sterling.user.entity.Role;
import com.sterling.user.entity.User;
import com.sterling.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds demo data into the H2 database on startup.
 * Only for development/testing purposes.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return; // Already seeded
        }

        User admin = User.builder()
                .fullName("Admin User")
                .email("admin@sterling.com")
                .password(passwordEncoder.encode("Admin@123"))
                .phone("9000000001")
                .role(Role.ADMIN)
                .enabled(true)
                .build();

        User alice = User.builder()
                .fullName("Simranpreet Singh")
                .email("simran@sterling.com")
                .password(passwordEncoder.encode("Password@123"))
                .phone("9876543210")
                .role(Role.USER)
                .enabled(true)
                .build();

        User bob = User.builder()
                .fullName("Preetinder Singh")
                .email("preetinder@sterling.com")
                .password(passwordEncoder.encode("Password@123"))
                .phone("9876543211")
                .role(Role.USER)
                .enabled(true)
                .build();

        User merchant = User.builder()
                .fullName("Allen John")
                .email("allen@sterling.com")
                .password(passwordEncoder.encode("Password@123"))
                .phone("9876543212")
                .role(Role.MERCHANT)
                .enabled(true)
                .build();

        userRepository.save(admin);
        userRepository.save(alice);
        userRepository.save(bob);
        userRepository.save(merchant);

        log.info("===================================================");
        log.info("  Demo users seeded:");
        log.info("  admin@sterling.com    / Admin@123  (ADMIN)");
        log.info("  simran@sterling.com   / Password@123 (USER)");
        log.info("  preetinder@sterling.com / Password@123 (USER)");
        log.info("  allen@sterling.com    / Password@123 (MERCHANT)");
        log.info("===================================================");
    }
}
