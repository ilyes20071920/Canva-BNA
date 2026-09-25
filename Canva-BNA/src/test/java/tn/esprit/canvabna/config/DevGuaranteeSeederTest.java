package tn.esprit.canvabna.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.canvabna.dto.GarantiesSummaryResponse;
import tn.esprit.canvabna.entity.Client;
import tn.esprit.canvabna.entity.Garantie;
import tn.esprit.canvabna.repository.ClientRepository;
import tn.esprit.canvabna.repository.GarantieRepository;
import tn.esprit.canvabna.service.GarantieService;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("dev")
@Transactional
class DevGuaranteeSeederTest {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private GarantieRepository garantieRepository;

    @Autowired
    private DevGuaranteeSeeder devGuaranteeSeeder;

    @Autowired
    private GarantieService garantieService;

    @Test
    @DisplayName("Vérifie que Math.floorMod empêche les valeurs négatives et génère des scénarios 0..3")
    void testFloorModScenarios() {
        // Given a test client
        final Client newClient = clientRepository.save(Client.builder()
                .identifiant("TEST_CLIENT_NEGATIVE_HASH_CHECK")
                .groupe("TEST GROUPE")
                .relation("STE TEST")
                .build());

        // When
        devGuaranteeSeeder.seedForClient(newClient);

        // Then: guarantees created based on floorMod(hashCode, 4)
        final int expectedScenario = Math.floorMod(newClient.getIdentifiant().hashCode(), 4);
        assertThat(expectedScenario).isBetween(0, 3);

        final List<Garantie> clientGaranties = garantieRepository.findByClientIdOrderById(newClient.getId());
        if (expectedScenario == 3) {
            assertThat(clientGaranties).isEmpty();
        } else {
            assertThat(clientGaranties).isNotEmpty();
        }
    }

    @Test
    @DisplayName("Vérifie l'idempotence : l'exécution multiple ne crée AUCUN doublon")
    void testSeederIdempotency() {
        // Given: Seed existing clients
        devGuaranteeSeeder.seedForAllClients();
        final long countFirstRun = garantieRepository.count();

        // When: Running seeder again
        devGuaranteeSeeder.seedForAllClients();
        final long countSecondRun = garantieRepository.count();

        // Then: Count must remain strictly identical
        assertThat(countSecondRun).isEqualTo(countFirstRun);
    }

    @Test
    @DisplayName("Vérifie le calcul dynamique des totaux (Total Charges Inscrites et Dont BNA)")
    void testDynamicTotalsCalculation() {
        final List<Client> clients = clientRepository.findAll();
        assertThat(clients).isNotEmpty();

        for (final Client client : clients) {
            final GarantiesSummaryResponse response = garantieService.getGarantiesForClient(client.getId());
            assertThat(response).isNotNull();

            final BigDecimal expectedTotal = response.getGaranties().stream()
                    .map(g -> g.getCharges() != null ? g.getCharges() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            final BigDecimal expectedDontBna = response.getGaranties().stream()
                    .filter(g -> "BANQUE NATIONALE AGRICOLE".equalsIgnoreCase(g.getBeneficiaire()))
                    .map(g -> g.getCharges() != null ? g.getCharges() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            assertThat(response.getTotalChargesInscrites()).isEqualByComparingTo(expectedTotal);
            assertThat(response.getDontBna()).isEqualByComparingTo(expectedDontBna);
        }
    }
}
