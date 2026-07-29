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
    private final tn.esprit.canvabna.repository.StructureRepository structureRepository;
    private final tn.esprit.canvabna.repository.ClientRepository clientRepository;
    private final tn.esprit.canvabna.repository.CompteRepository compteRepository;
    private final tn.esprit.canvabna.repository.ActionnaireRepository actionnaireRepository;
    private final tn.esprit.canvabna.repository.MandataireRepository mandataireRepository;

    @Override
    @Transactional
    public void run(String... args) {
        // 1. Seed Users
        if (userRepository.count() == 0) {
            log.info("No users found in database. Seeding default ADMIN account...");

            final User defaultAdmin = User.builder()
                    .matricule(10001)
                    .nom("Admin")
                    .prenom("System")
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
        }

        // 2. Seed Business Module
        if (clientRepository.count() == 0) {
            log.info("No clients found. Seeding default business module data...");

            // Structures
            final tn.esprit.canvabna.entity.Structure struct1 = tn.esprit.canvabna.entity.Structure.builder()
                    .nom("Direction Régionale Tunis I")
                    .code("DIR_REG_TUNIS_1")
                    .build();
            final tn.esprit.canvabna.entity.Structure struct2 = tn.esprit.canvabna.entity.Structure.builder()
                    .nom("Direction Régionale Tunis II")
                    .code("DIR_REG_TUNIS_2")
                    .build();
            structureRepository.save(struct1);
            structureRepository.save(struct2);

            // Client 1 : ILYESMHESPRIT
            final tn.esprit.canvabna.entity.Client client1 = tn.esprit.canvabna.entity.Client.builder()
                    .identifiant("30032002")
                    .groupe("ILYESMHESPRIT")
                    .relation("STE ENT. ILYES MHLHLI")
                    .activite("Développement Informatique")
                    .segment("Corporates")
                    .siegeSocial("BLOC 11 MOUROUJ 6")
                    .secteur("Sociétés non financières")
                    .dateEntreeRelation(java.time.LocalDate.of(2008, 10, 20))
                    .formeJuridique("Societe A Responsabilite Limitee")
                    .capitalSocial(new java.math.BigDecimal("1920.000"))
                    .agence("146")
                    .directionRegionale("DIR.REG. TUNIS I")
                    .structure(struct1)
                    .build();
            clientRepository.save(client1);

            // Compte Client 1
            final tn.esprit.canvabna.entity.Compte compte1 = tn.esprit.canvabna.entity.Compte.builder()
                    .id(new tn.esprit.canvabna.entity.CompteId("123", "IM02", "192019"))
                    .agence("146")
                    .client(client1)
                    .build();
            compteRepository.save(compte1);

            // Mandataire Client 1
            final tn.esprit.canvabna.entity.Mandataire mand1 = tn.esprit.canvabna.entity.Mandataire.builder()
                    .numMandat("30032002")
                    .numDemande("10-202200157")
                    .typeMandat("General")
                    .agence("146")
                    .mandant("ILYES MHLHLI")
                    .dateCreation(java.time.LocalDate.of(2022, 1, 17))
                    .dateDebut(java.time.LocalDate.of(2023, 11, 29))
                    .dateFin(java.time.LocalDate.of(2026, 11, 29))
                    .compte(compte1)
                    .build();
            mandataireRepository.save(mand1);

            // Client 2 : ILYES MH TRADING
            final tn.esprit.canvabna.entity.Client client2 = tn.esprit.canvabna.entity.Client.builder()
                    .identifiant("1010202K")
                    .groupe("ILYES MH TRADING")
                    .relation("STE ILYES MH TRADING")
                    .activite("Commerce international")
                    .segment("Corporates")
                    .siegeSocial("BLOC 11 MOUROUJ 6")
                    .secteur("Sociétés non financières")
                    .dateEntreeRelation(java.time.LocalDate.of(2015, 3, 15))
                    .formeJuridique("Societe A Responsabilite Limitee")
                    .capitalSocial(new java.math.BigDecimal("150000.000"))
                    .agence("120")
                    .directionRegionale("DIR.REG. TUNIS II")
                    .structure(struct2)
                    .build();
            clientRepository.save(client2);

            // Comptes Client 2
            final tn.esprit.canvabna.entity.Compte compte2 = tn.esprit.canvabna.entity.Compte.builder()
                    .id(new tn.esprit.canvabna.entity.CompteId("120", "DT20", "001234"))
                    .agence("120")
                    .client(client2)
                    .build();
            final tn.esprit.canvabna.entity.Compte compte3 = tn.esprit.canvabna.entity.Compte.builder()
                    .id(new tn.esprit.canvabna.entity.CompteId("120", "DV20", "005678"))
                    .agence("120")
                    .client(client2)
                    .build();
            compteRepository.save(compte2);
            compteRepository.save(compte3);

            // Actionnaires Client 2
            final tn.esprit.canvabna.entity.Actionnaire act1 = tn.esprit.canvabna.entity.Actionnaire.builder()
                    .nom("ILYES MHLHLI")
                    .nombreActions(1500)
                    .montant(new java.math.BigDecimal("75000.000"))
                    .pourcentageActions(50.0)
                    .client(client2)
                    .build();
            final tn.esprit.canvabna.entity.Actionnaire act2 = tn.esprit.canvabna.entity.Actionnaire.builder()
                    .nom("ALI ALI MHLHLI")
                    .nombreActions(1500)
                    .montant(new java.math.BigDecimal("75000.000"))
                    .pourcentageActions(50.0)
                    .client(client2)
                    .build();
            actionnaireRepository.save(act1);
            actionnaireRepository.save(act2);

            log.info("Business module database seeding complete.");
        }
    }
}
