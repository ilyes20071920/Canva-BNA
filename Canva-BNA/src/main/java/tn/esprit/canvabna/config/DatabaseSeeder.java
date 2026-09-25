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

import java.util.Optional;

/**
 * Initialise la base de données au démarrage de l'application.
 *
 * <p><strong>Stratégie d'idempotence :</strong> Chaque enregistrement est inséré
 * individuellement et conditionnellement à l'aide d'une vérification par identifiant
 * métier stable. Les redémarrages successifs ne produisent jamais de doublons.
 *
 * <p><strong>Ordre d'exécution :</strong>
 * <ol>
 *   <li>Utilisateurs (par matricule)</li>
 *   <li>Structures (par code)</li>
 *   <li>Clients (par identifiant) — {@code seedClientIfMissing}</li>
 *   <li>Comptes (par clé composite)</li>
 *   <li>Actionnaires (par nom + client)</li>
 *   <li>Mandataires (par numéro de mandat)</li>
 *   <li>Garanties — délégué à {@link DevGuaranteeSeeder} (profil non-prod uniquement)</li>
 * </ol>
 *
 * <p><strong>Ajouter un nouveau client de test :</strong>
 * Appelez simplement {@code seedClientIfMissing(...)} avec les données du nouveau client
 * à la fin de la section "Clients". Au prochain redémarrage, le client sera inséré
 * et ses garanties seront automatiquement créées par {@link DevGuaranteeSeeder}.
 * Aucune modification de {@code DevGuaranteeSeeder} n'est nécessaire.
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
    private final tn.esprit.canvabna.service.ReferenceDataService referenceDataService;

    /**
     * {@link DevGuaranteeSeeder} est injecté via {@link Optional} pour que l'application
     * démarre proprement sur le profil {@code prod} où ce bean n'existe pas.
     */
    private final Optional<DevGuaranteeSeeder> devGuaranteeSeeder;

    @Override
    @Transactional
    public void run(String... args) {

        // ============================================================
        // 1. USERS — par matricule
        // ============================================================
        seedUserIfMissing(10001, "Admin", "System", "Direction Générale", Role.ROLE_ADMIN);
        seedUserIfMissing(20002, "Prise", "EnCharge", "Service Prise en Charge", Role.ROLE_PRISE_EN_CHARGE);
        seedUserIfMissing(30003, "Chef", "Division", "Division Technique", Role.ROLE_CHEF_DIVISION);

        // ============================================================
        // 2. REFERENCE DATA — idempotent par code stable
        // ============================================================
        referenceDataService.seedProduitIfMissing("PROD_IND_TND", "Crédit Finançant l'Industrie en dinars");
        referenceDataService.seedProduitIfMissing("PROD_CMT_ENG", "CMT Prise en Charge d'Engagements");
        referenceDataService.seedProduitIfMissing("PROD_CONSOL_SYND", "Crédit de consolidation syndiqué");
        referenceDataService.seedProduitIfMissing("PROD_SERV_DEV", "Crédit Finançant les Services en devises");
        referenceDataService.seedProduitIfMissing("PROD_FODEC", "Dotations FODEC");
        referenceDataService.seedProduitIfMissing("PROD_RAIDET_COMM", "Dotations Raidet Commerciaux et industriels");
        referenceDataService.seedProduitIfMissing("PROD_ARTISANAT", "Crédit Finançant les Investissements d'Artisanat & les Petits Métiers");
        referenceDataService.seedProduitIfMissing("PROD_PRIVATISATION", "Crédit Finançant la Privatisation");
        referenceDataService.seedProduitIfMissing("PROD_COVID_19", "Crédit MT - COVID 19");
        referenceDataService.seedProduitIfMissing("PROD_FODERI", "Dotations FODERI");

        referenceDataService.seedObjetCreditIfMissing("OBJ_AGRO_ALIM", "Agro-Alimentaires");
        referenceDataService.seedObjetCreditIfMissing("OBJ_AUTRES_SERV", "Autres services");
        referenceDataService.seedObjetCreditIfMissing("OBJ_BOIS_AMEUBLE", "Bois, Liège et Ameublement");
        referenceDataService.seedObjetCreditIfMissing("OBJ_COLLECTE_CEREALES", "Collecte céréales et produits agricoles");
        referenceDataService.seedObjetCreditIfMissing("OBJ_COMMERCE_AGRO", "Commerce produits agro-alimentaires");
        referenceDataService.seedObjetCreditIfMissing("OBJ_ALIMENT_BETAIL", "Fabrication d'aliments pour bétail");
        referenceDataService.seedObjetCreditIfMissing("OBJ_EMBALLAGE_IND", "Fabrication emballage industriel");
        referenceDataService.seedObjetCreditIfMissing("OBJ_CHIMIE_IND", "Industrie chimique");

        // ============================================================
        // 3. STRUCTURES — par code
        // ============================================================
        final tn.esprit.canvabna.entity.Structure struct1 = seedStructureIfMissing(
                "DIR_REG_TUNIS_1", "Direction Régionale Tunis I");
        final tn.esprit.canvabna.entity.Structure struct2 = seedStructureIfMissing(
                "DIR_REG_TUNIS_2", "Direction Régionale Tunis II");

        // ============================================================
        // 3. CLIENTS — idempotent par identifiant métier stable
        //
        // Pour ajouter un nouveau client de test :
        //   1. Ajouter un appel seedClientIfMissing(...) ci-dessous
        //   2. Redémarrer le backend
        //   → Le client sera inséré s'il est absent.
        //   → DevGuaranteeSeeder lui créera automatiquement des garanties.
        //   → Aucune autre modification n'est requise.
        // ============================================================

        // Client 1 : ILYESMHESPRIT
        final tn.esprit.canvabna.entity.Client client1 = seedClientIfMissing(
                "30032002",
                tn.esprit.canvabna.entity.Client.builder()
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
                        .build()
        );

        // Client 2 : ILYES MH TRADING
        final tn.esprit.canvabna.entity.Client client2 = seedClientIfMissing(
                "1010202K",
                tn.esprit.canvabna.entity.Client.builder()
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
                        .build()
        );

        // Client 3 : BOUDOKHANE MOKHTAR
        final tn.esprit.canvabna.entity.Client client3 = seedClientIfMissing(
                "1042986R",
                tn.esprit.canvabna.entity.Client.builder()
                        .identifiant("1042986R")
                        .groupe("BOUDOKHANE MOKHTAR")
                        .relation("STE ENT. BOUDOKHANE MOKHTAR")
                        .activite("Bâtiment et Travaux Publics (BTP)")
                        .segment("Corporates")
                        .siegeSocial("BLOC 11 CITE OLYMPIQUE TUN 1003")
                        .secteur("Sociétés non financières")
                        .dateEntreeRelation(java.time.LocalDate.of(2005, 6, 12))
                        .formeJuridique("Societe Anonyme")
                        .capitalSocial(new java.math.BigDecimal("500000.000"))
                        .agence("146")
                        .directionRegionale("DIR.REG. TUNIS I")
                        .structure(struct1)
                        .build()
        );

        // Client 4 : STE AGRO BEN SAID
        final tn.esprit.canvabna.entity.Client client4 = seedClientIfMissing(
                "2054321A",
                tn.esprit.canvabna.entity.Client.builder()
                        .identifiant("2054321A")
                        .groupe("GROUPE BEN SAID")
                        .relation("STE AGRO BEN SAID")
                        .activite("Transformation & Agro-alimentaire")
                        .segment("Corporates")
                        .siegeSocial("ZONE INDUSTRIELLE SFAX")
                        .secteur("Agro-alimentaire")
                        .dateEntreeRelation(java.time.LocalDate.of(2012, 9, 1))
                        .formeJuridique("Societe A Responsabilite Limitee")
                        .capitalSocial(new java.math.BigDecimal("300000.000"))
                        .agence("120")
                        .directionRegionale("DIR.REG. TUNIS II")
                        .structure(struct2)
                        .build()
        );

        // Client 5 : STE TUNISIE INDUSTRIE
        final tn.esprit.canvabna.entity.Client client5 = seedClientIfMissing(
                "3098765B",
                tn.esprit.canvabna.entity.Client.builder()
                        .identifiant("3098765B")
                        .groupe("GROUPE TUNISIE INDUSTRIE")
                        .relation("STE TUNISIE INDUSTRIE")
                        .activite("Fabrication Mécanique & Métallurgique")
                        .segment("Corporates")
                        .siegeSocial("ZONE INDUSTRIELLE MEGHRINE")
                        .secteur("Industries Manufacturières")
                        .dateEntreeRelation(java.time.LocalDate.of(2018, 1, 10))
                        .formeJuridique("Societe Anonyme")
                        .capitalSocial(new java.math.BigDecimal("1200000.000"))
                        .agence("146")
                        .directionRegionale("DIR.REG. TUNIS I")
                        .structure(struct1)
                        .build()
        );

        // ============================================================
        // 4. COMPTES — par clé composite (codeGuichet, codeProduit, numCompte)
        // ============================================================
        seedCompteIfMissing("123", "IM02", "192019", "146", client1);
        seedCompteIfMissing("123", "DT10", "008877", "146", client1);
        seedCompteIfMissing("123", "DV30", "009988", "146", client1);
        seedCompteIfMissing("120", "DT20", "001234", "120", client2);
        seedCompteIfMissing("120", "DV20", "005678", "120", client2);
        seedCompteIfMissing("146", "IM01", "004455", "146", client3);
        seedCompteIfMissing("146", "DT05", "006677", "146", client3);
        seedCompteIfMissing("120", "AG01", "001122", "120", client4);
        seedCompteIfMissing("146", "IND1", "003344", "146", client5);

        // ============================================================
        // 5. ACTIONNAIRES — par nom + clientId
        // ============================================================
        seedActionnaireIfMissing("ILYES MHLHLI", client2, 1500,
                new java.math.BigDecimal("75000.000"), 50.0);
        seedActionnaireIfMissing("ALI ALI MHLHLI", client2, 1500,
                new java.math.BigDecimal("75000.000"), 50.0);
        seedActionnaireIfMissing("BOUDOKHANE MOKHTAR", client3, 2500,
                new java.math.BigDecimal("250000.000"), 50.0);
        seedActionnaireIfMissing("MOKHTAR BOUDOKHANE", client3, 2500,
                new java.math.BigDecimal("250000.000"), 50.0);

        // ============================================================
        // 6. MANDATAIRES — par numMandat
        // ============================================================
        seedMandataireIfMissing("30032002", "10-202200157", "General", "146",
                "ILYES MHLHLI",
                java.time.LocalDate.of(2022, 1, 17),
                java.time.LocalDate.of(2023, 11, 29),
                java.time.LocalDate.of(2026, 11, 29),
                "123", "IM02", "192019");

        // ============================================================
        // 7. GARANTIES — délégué à DevGuaranteeSeeder (profil !prod)
        //
        // DevGuaranteeSeeder.seedForAllClients() itère sur tous les clients
        // et crée automatiquement les garanties pour ceux qui n'en ont pas encore.
        // Aucune action ici n'est nécessaire lors de l'ajout d'un nouveau client.
        // ============================================================
        devGuaranteeSeeder.ifPresentOrElse(
                seeder -> {
                    log.info("DevGuaranteeSeeder available — triggering guarantee seeding...");
                    seeder.seedForAllClients();
                },
                () -> log.info("DevGuaranteeSeeder not active (prod profile) — skipping guarantee seeding.")
        );

        log.info("DatabaseSeeder: initialization complete.");
    }

    // ================================================================
    // Private helper — idempotent seeders
    // ================================================================

    private void seedUserIfMissing(final int matricule, final String nom, final String prenom,
                                    final String structure, final Role role) {
        if (userRepository.findByMatricule(matricule).isEmpty()) {
            final User user = User.builder()
                    .matricule(matricule)
                    .nom(nom)
                    .prenom(prenom)
                    .password(passwordEncoder.encode("admin123"))
                    .structure(structure)
                    .role(role)
                    .enabled(true)
                    .build();
            userRepository.save(user);
            log.info("Created user matricule={}, role={}", matricule, role);
        }
    }

    private tn.esprit.canvabna.entity.Structure seedStructureIfMissing(final String code, final String nom) {
        return structureRepository.findByCode(code).orElseGet(() -> {
            final tn.esprit.canvabna.entity.Structure s = tn.esprit.canvabna.entity.Structure.builder()
                    .code(code)
                    .nom(nom)
                    .build();
            final tn.esprit.canvabna.entity.Structure saved = structureRepository.save(s);
            log.info("Created structure code={}", code);
            return saved;
        });
    }

    /**
     * Insère le client uniquement s'il n'existe pas encore (contrôle par {@code identifiant}).
     * Si le client existe déjà, retourne l'instance déjà en base sans la modifier.
     *
     * @param identifiant identifiant métier stable du client
     * @param newClient   entité à insérer si absente
     * @return l'entité persistée (existante ou nouvellement créée)
     */
    private tn.esprit.canvabna.entity.Client seedClientIfMissing(
            final String identifiant,
            final tn.esprit.canvabna.entity.Client newClient) {
        return clientRepository.findByIdentifiant(identifiant).orElseGet(() -> {
            final tn.esprit.canvabna.entity.Client saved = clientRepository.save(newClient);
            log.info("Created client identifiant='{}'", identifiant);
            return saved;
        });
    }

    private void seedCompteIfMissing(final String codeGuichet, final String codeProduit,
                                      final String numCompte, final String agence,
                                      final tn.esprit.canvabna.entity.Client client) {
        if (!compteRepository.existsById_CodeGuichetAndId_CodeProduitAndId_NumCompte(
                codeGuichet, codeProduit, numCompte)) {
            final tn.esprit.canvabna.entity.Compte compte = tn.esprit.canvabna.entity.Compte.builder()
                    .id(new tn.esprit.canvabna.entity.CompteId(codeGuichet, codeProduit, numCompte))
                    .agence(agence)
                    .client(client)
                    .build();
            compteRepository.save(compte);
            log.info("Created compte {}-{}-{}", codeGuichet, codeProduit, numCompte);
        }
    }

    private void seedActionnaireIfMissing(final String nom,
                                           final tn.esprit.canvabna.entity.Client client,
                                           final int nombreActions,
                                           final java.math.BigDecimal montant,
                                           final double pourcentage) {
        final boolean exists = actionnaireRepository.findByClientId(client.getId())
                .stream()
                .anyMatch(a -> nom.equals(a.getNom()));
        if (!exists) {
            final tn.esprit.canvabna.entity.Actionnaire act = tn.esprit.canvabna.entity.Actionnaire.builder()
                    .nom(nom)
                    .nombreActions(nombreActions)
                    .montant(montant)
                    .pourcentageActions(pourcentage)
                    .client(client)
                    .build();
            actionnaireRepository.save(act);
            log.info("Created actionnaire '{}' for client '{}'", nom, client.getIdentifiant());
        }
    }

    private void seedMandataireIfMissing(final String numMandat, final String numDemande,
                                          final String typeMandat, final String agence,
                                          final String mandant,
                                          final java.time.LocalDate dateCreation,
                                          final java.time.LocalDate dateDebut,
                                          final java.time.LocalDate dateFin,
                                          final String codeGuichet, final String codeProduit,
                                          final String numCompte) {
        final boolean exists = mandataireRepository.findByNumMandat(numMandat).isPresent();
        if (!exists) {
            final tn.esprit.canvabna.entity.Compte compte = compteRepository
                    .findById_CodeGuichetAndId_CodeProduitAndId_NumCompte(
                            codeGuichet, codeProduit, numCompte)
                    .orElse(null);
            if (compte == null) {
                log.warn("Cannot create mandataire '{}' — compte {}-{}-{} not found.",
                        numMandat, codeGuichet, codeProduit, numCompte);
                return;
            }
            final tn.esprit.canvabna.entity.Mandataire mand = tn.esprit.canvabna.entity.Mandataire.builder()
                    .numMandat(numMandat)
                    .numDemande(numDemande)
                    .typeMandat(typeMandat)
                    .agence(agence)
                    .mandant(mandant)
                    .dateCreation(dateCreation)
                    .dateDebut(dateDebut)
                    .dateFin(dateFin)
                    .compte(compte)
                    .build();
            mandataireRepository.save(mand);
            log.info("Created mandataire numMandat='{}'", numMandat);
        }
    }
}
