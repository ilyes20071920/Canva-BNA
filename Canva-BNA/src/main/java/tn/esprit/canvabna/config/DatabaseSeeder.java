package tn.esprit.canvabna.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.canvabna.entity.Role;
import tn.esprit.canvabna.entity.User;
import tn.esprit.canvabna.repository.UserRepository;

/**
 * Automatically runs on application startup.
 * Checks if the database is completely empty. If it is, it generates a default admin account.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() == 0) {
            log.info("No users found in database. Seeding default ADMIN account...");

            final User defaultAdmin = User.builder()
                    .matricule(10001)
                    .nom("Admin")
                    .prenom("System")
                    // The password gets properly encoded via BCrypt here
                    .password(passwordEncoder.encode("admin123"))
                    .structure("Direction Générale")
                    .role(Role.ROLE_ADMIN)
                    .enabled(true)
                    .build();

            userRepository.save(defaultAdmin);

            log.info("==================================================");
            log.info(" DEFAULT ADMIN ACCOUNT CREATED");
            log.info(" Matricule : 10001");
            log.info(" Password  : admin123");
            log.info("==================================================");
        } else {
            log.debug("Database already contains users. Skipping seeder.");
        }
    }
}
