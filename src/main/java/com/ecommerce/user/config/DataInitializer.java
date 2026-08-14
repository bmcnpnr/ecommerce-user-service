package com.ecommerce.user.config;

import com.ecommerce.user.model.AppUser;
import com.ecommerce.user.model.UserRole;
import com.ecommerce.user.model.UserStatus;
import com.ecommerce.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_EMAIL    = "admin@ecommerce.com";
    private static final String ADMIN_PASSWORD = "admin";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.existsByUsername(ADMIN_USERNAME)) {
            log.info("Default admin user already exists, skipping seed.");
            return;
        }

        AppUser admin = AppUser.builder()
                .username(ADMIN_USERNAME)
                .email(ADMIN_EMAIL)
                .passwordHash(passwordEncoder.encode(ADMIN_PASSWORD))
                .firstName("Admin")
                .lastName("User")
                .role(UserRole.ADMIN)
                .status(UserStatus.ACTIVE)
                .build();

        userRepository.save(admin);
        log.info("Default admin user created — username: '{}', password: '{}'", ADMIN_USERNAME, ADMIN_PASSWORD);
    }
}
