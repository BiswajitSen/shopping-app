package com.shopapp.shared.config;

import com.shopapp.shared.domain.Role;
import com.shopapp.user.domain.User;
import com.shopapp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    @Profile("!test")
    CommandLineRunner initData() {
        return args -> {
            // Create admin user if not exists
            if (userRepository.findByEmail("admin@shopapp.com").isEmpty()) {
                User admin = User.builder()
                        .email("admin@shopapp.com")
                        .password(passwordEncoder.encode("admin123456"))
                        .firstName("System")
                        .lastName("Admin")
                        .roles(Set.of(Role.USER, Role.VENDOR, Role.ADMIN))
                        .enabled(true)
                        .build();
                userRepository.save(admin);
                log.info("Created admin user: admin@shopapp.com with password: admin123456");
            }
        };
    }
}
