package tn.esprit.canvabna.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.canvabna.entity.Client;
import tn.esprit.canvabna.entity.Garantie;
import tn.esprit.canvabna.repository.ClientRepository;
import tn.esprit.canvabna.repository.GarantieRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Seeder de développement pour les garanties en possession.
 *
 * <p><strong>Activation :</strong> Ce composant est actif sur tous les profils Spring
 * <em>sauf</em> {@code prod}. En l'absence de profil explicite (profil par défaut),
 * il s'active automatiquement.
 *
 * <ul>
 *   <li>{@code spring.profiles.active=dev}  → actif ✅</li>
 *   <li>{@code spring.profiles.active=test} → actif ✅</li>
 *   <li>aucun profil défini (défaut)        → actif ✅</li>
 *   <li>{@code spring.profiles.active=prod} → INACTIF ❌</li>
 * </ul>
 *
 * <p><strong>Idempotence :</strong> Pour chaque client, le seeder vérifie d'abord
 * si des garanties existent déjà ({@link GarantieRepository#existsByClientId}).
 * Si c'est le cas, il ne crée rien. Les redémarrages successifs ne produisent donc
 * jamais de doublons.
 *
 * <p><strong>Scénarios déterministes :</strong> Le scénario de test assigné à chaque
 * client est dérivé de son identifiant métier stable via :
 * {@code Math.floorMod(client.getIdentifiant().hashCode(), 4)}
 * Ce calcul renvoie toujours 0, 1, 2 ou 3 — sans valeurs négatives.
 *
 * <table border="1" summary="Scénarios de test">
 *   <tr><th>Scénario</th><th>Nombre de garanties</th><th>Profil</th></tr>
 *   <tr><td>0</td><td>4</td><td>Hypothèques, tous bénéficiaire BNA</td></tr>
 *   <tr><td>1</td><td>2</td><td>Nantissement + Garantie personnelle, rangs différents</td></tr>
 *   <tr><td>2</td><td>5</td><td>Mix complet : Hypothèque, Nantissement FdC, Matériel, Caution, Dépôt</td></tr>
 *   <tr><td>3</td><td>0</td><td>Intentionnellement vide — teste l'état vide de l'UI</td></tr>
 * </table>
 *
 * <p><strong>Ajouter un nouveau client de test :</strong> Il suffit d'ajouter le client
 * dans {@link DatabaseSeeder}. Ce seeder détecte automatiquement le nouveau client lors
 * du prochain démarrage et lui crée le bon jeu de garanties sans aucune modification ici.
 */
@Component
@Profile("!prod")
@RequiredArgsConstructor
@Slf4j
public class DevGuaranteeSeeder {

    private static final String BENEFICIAIRE_BNA = "BANQUE NATIONALE AGRICOLE";

    private final GarantieRepository garantieRepository;
    private final ClientRepository clientRepository;

    /**
     * Point d'entrée principal, appelé par {@link DatabaseSeeder} après le seeding des clients.
     * Itère sur TOUS les clients de la base et applique {@link #seedForClient} à chacun.
     */
    @Transactional
    public void seedForAllClients() {
        final List<Client> allClients = clientRepository.findAll();
        log.info("DevGuaranteeSeeder: checking {} client(s) for guarantee seeding...", allClients.size());

        for (final Client client : allClients) {
            seedForClient(client);
        }

        log.info("DevGuaranteeSeeder: guarantee seeding complete.");
    }

    /**
     * Crée les garanties de développement pour un client donné si et seulement si
     * ce client n'en possède pas encore (idempotence).
     *
     * @param client le client à traiter
     */
    @Transactional
    public void seedForClient(final Client client) {
        if (garantieRepository.existsByClientId(client.getId())) {
            log.debug("Client '{}' already has garanties — skipping.", client.getIdentifiant());
            return;
        }

        // Scénario déterministe : Math.floorMod garantit 0..3 même pour les hashCode négatifs
        final int scenario = Math.floorMod(client.getIdentifiant().hashCode(), 4);
        log.info("Seeding garanties for client '{}' (scenario {}).", client.getIdentifiant(), scenario);

        switch (scenario) {
            case 0 -> seedScenario0_HypothequeHeavy(client);
            case 1 -> seedScenario1_Mixed(client);
            case 2 -> seedScenario2_FullMix(client);
            case 3 -> {
                // Scénario 3 : zéro garanties — intentionnel pour tester l'état vide de l'UI
                log.info("Client '{}' assigned scenario 3 (intentionally empty garanties).",
                        client.getIdentifiant());
            }
            default -> log.warn("Unknown scenario {} for client '{}' — no garanties created.",
                    scenario, client.getIdentifiant());
        }
    }

    // ========================================================================
    // SCÉNARIO 0 — 4 garanties, Hypothèques, tous bénéficiaire BNA
    // ========================================================================

    private void seedScenario0_HypothequeHeavy(final Client client) {
        garantieRepository.saveAll(List.of(
                Garantie.builder()
                        .client(client)
                        .libelle("Hypothèque sur la totalité d'une propriété immatriculée - TUNIS/Titre Foncier[SFT14.785.231]")
                        .consistance("2000 m²")
                        .valeur(new BigDecimal("100000.000"))
                        .dateExpertise(LocalDate.of(2026, 7, 16))
                        .rang(1)
                        .beneficiaire(BENEFICIAIRE_BNA)
                        .charges(new BigDecimal("2500.000"))
                        .affectation("Crédit d'investissement")
                        .numSeqGar("BNa1")
                        .idCredit("01")
                        .build(),
                Garantie.builder()
                        .client(client)
                        .libelle("Hypothèque sur la totalité d'une propriété immatriculée - TUNIS/Titre Foncier[123]")
                        .consistance("J&A")
                        .valeur(new BigDecimal("100000.000"))
                        .dateExpertise(LocalDate.of(2026, 7, 20))
                        .rang(2)
                        .beneficiaire(BENEFICIAIRE_BNA)
                        .charges(new BigDecimal("1000.000"))
                        .affectation("Crédit moyen terme")
                        .numSeqGar("BNa1")
                        .idCredit("1")
                        .build(),
                Garantie.builder()
                        .client(client)
                        .libelle("Hypothèque sur immeuble commercial - ARIANA/Titre Foncier[ARI.2021.009]")
                        .consistance("350 m²")
                        .valeur(new BigDecimal("75000.000"))
                        .dateExpertise(LocalDate.of(2025, 11, 5))
                        .rang(1)
                        .beneficiaire(BENEFICIAIRE_BNA)
                        .charges(new BigDecimal("3000.000"))
                        .affectation("Crédit d'investissement")
                        .numSeqGar("BNa2")
                        .idCredit("01")
                        .build(),
                Garantie.builder()
                        .client(client)
                        .libelle("Hypothèque sur appartement - LA MARSA/Titre Foncier[MRS.442]")
                        .consistance("120 m²")
                        .valeur(new BigDecimal("55000.000"))
                        .dateExpertise(LocalDate.of(2024, 6, 18))
                        .rang(1)
                        .beneficiaire(BENEFICIAIRE_BNA)
                        .charges(new BigDecimal("1800.000"))
                        .affectation("Crédit à moyen terme")
                        .numSeqGar("BNa3")
                        .idCredit("02")
                        .build()
        ));
        log.info("Scenario 0: created 4 hypothèque garanties for client '{}'.", client.getIdentifiant());
    }

    // ========================================================================
    // SCÉNARIO 1 — 2 garanties, Nantissement + Garantie personnelle
    // ========================================================================

    private void seedScenario1_Mixed(final Client client) {
        garantieRepository.saveAll(List.of(
                Garantie.builder()
                        .client(client)
                        .libelle("Nantissement de fonds de commerce - Tunis Centre")
                        .consistance("Fonds de commerce inscrit au RC de Tunis sous N° 12345")
                        .valeur(new BigDecimal("80000.000"))
                        .dateExpertise(LocalDate.of(2025, 9, 12))
                        .rang(1)
                        .beneficiaire(BENEFICIAIRE_BNA)
                        .charges(new BigDecimal("4000.000"))
                        .affectation("Crédit d'exploitation")
                        .numSeqGar("NaN1")
                        .idCredit("03")
                        .build(),
                Garantie.builder()
                        .client(client)
                        .libelle("Garantie personnelle et solidaire du gérant")
                        .consistance("-")
                        .valeur(new BigDecimal("50000.000"))
                        .dateExpertise(LocalDate.of(2025, 9, 12))
                        .rang(2)
                        .beneficiaire(BENEFICIAIRE_BNA)
                        .charges(BigDecimal.ZERO)
                        .affectation("Crédit d'exploitation")
                        .numSeqGar("GPS1")
                        .idCredit("03")
                        .build()
        ));
        log.info("Scenario 1: created 2 garanties (nantissement + personnelle) for client '{}'.",
                client.getIdentifiant());
    }

    // ========================================================================
    // SCÉNARIO 2 — 5 garanties, combinaison complète
    // ========================================================================

    private void seedScenario2_FullMix(final Client client) {
        garantieRepository.saveAll(List.of(
                Garantie.builder()
                        .client(client)
                        .libelle("Hypothèque sur la totalité d'une propriété immatriculée - SFAX/Titre Foncier[SFX.881.2020]")
                        .consistance("1500 m²")
                        .valeur(new BigDecimal("120000.000"))
                        .dateExpertise(LocalDate.of(2026, 3, 10))
                        .rang(1)
                        .beneficiaire(BENEFICIAIRE_BNA)
                        .charges(new BigDecimal("6000.000"))
                        .affectation("Crédit d'investissement")
                        .numSeqGar("HYP1")
                        .idCredit("01")
                        .build(),
                Garantie.builder()
                        .client(client)
                        .libelle("Nantissement de fonds de commerce - Sfax Zone Industrielle")
                        .consistance("Fonds de commerce inscrit au RC Sfax N° 67890")
                        .valeur(new BigDecimal("65000.000"))
                        .dateExpertise(LocalDate.of(2025, 7, 22))
                        .rang(1)
                        .beneficiaire(BENEFICIAIRE_BNA)
                        .charges(new BigDecimal("2500.000"))
                        .affectation("Crédit d'exploitation")
                        .numSeqGar("NFC1")
                        .idCredit("02")
                        .build(),
                Garantie.builder()
                        .client(client)
                        .libelle("Nantissement de matériel industriel - Ligne de production ref. MAT-2022-44")
                        .consistance("Équipements industriels (inventaire annexé)")
                        .valeur(new BigDecimal("45000.000"))
                        .dateExpertise(LocalDate.of(2024, 12, 3))
                        .rang(1)
                        .beneficiaire(BENEFICIAIRE_BNA)
                        .charges(new BigDecimal("1500.000"))
                        .affectation("Crédit de financement de stocks")
                        .numSeqGar("NMA1")
                        .idCredit("02")
                        .build(),
                Garantie.builder()
                        .client(client)
                        .libelle("Caution personnelle et solidaire de l'associé principal")
                        .consistance("-")
                        .valeur(new BigDecimal("200000.000"))
                        .dateExpertise(LocalDate.of(2026, 1, 15))
                        .rang(1)
                        .beneficiaire(BENEFICIAIRE_BNA)
                        .charges(BigDecimal.ZERO)
                        .affectation("Crédit d'investissement")
                        .numSeqGar("CAU1")
                        .idCredit("01")
                        .build(),
                Garantie.builder()
                        .client(client)
                        .libelle("Dépôt / Garantie financière - Bon de caisse N° BC-2024-0123")
                        .consistance("Dépôt à terme affecté en garantie")
                        .valeur(new BigDecimal("30000.000"))
                        .dateExpertise(LocalDate.of(2024, 6, 1))
                        .rang(1)
                        .beneficiaire(BENEFICIAIRE_BNA)
                        .charges(new BigDecimal("500.000"))
                        .affectation("Facilité de caisse")
                        .numSeqGar("DEP1")
                        .idCredit("03")
                        .build()
        ));
        log.info("Scenario 2: created 5 garanties (full mix) for client '{}'.", client.getIdentifiant());
    }
}
