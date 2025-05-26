package com.nelumbo.zoo_api.config;

import com.nelumbo.zoo_api.models.Role;
import com.nelumbo.zoo_api.models.User;
import com.nelumbo.zoo_api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.logging.Logger;


@Configuration
public class DataLoader {
    Logger logger = Logger.getLogger(getClass().getName());

    @Value("${admin.email}")
    private String adminEmail;
    @Value("${admin.password}")
    private String adminPassword;

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {

            if (userRepository.findByEmail(adminEmail).isEmpty()) {
                User admin = new User();
                admin.setName("Admin");
                admin.setEmail(adminEmail);
                admin.setPassword(passwordEncoder.encode(adminPassword));
                admin.setRole(Role.ADMIN);


                userRepository.save(admin);
                logger.info("Usuario admin creado: admin@mail.com / admin");

                logger.info("Email: " + adminEmail);
                logger.info("Password (plain): " + adminPassword);
                logger.info("Password (encoded): " + admin.getPassword());
            }
        };
    }
}