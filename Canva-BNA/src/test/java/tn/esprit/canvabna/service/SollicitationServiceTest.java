package tn.esprit.canvabna.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.canvabna.dto.*;
import tn.esprit.canvabna.entity.*;
import tn.esprit.canvabna.exception.ResourceNotFoundException;
import tn.esprit.canvabna.repository.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SollicitationServiceTest {

        @Mock
        private SollicitationRepository sollicitationRepository;
        @Mock
        private ClientRepository clientRepository;
        @Mock
        private CompteRepository compteRepository;
        @Mock
        private ProduitRefRepository produitRefRepository;
        @Mock
        private ObjetCreditRefRepository objetCreditRefRepository;

        @InjectMocks
        private SollicitationService sollicitationService;

        private Client client1;
        private Client client2;
        private Compte compte1;
        private Compte compte2;
        private ProduitRef produit;
        private ObjetCreditRef objetCredit;

        @BeforeEach
        void setUp() {
                client1 = Client.builder()
                                .id(1L)
                                .identifiant("30032002")
                                .groupe("ILYESMHESPRIT")
                                .relation("STE ENT. ILYES MHLHLI")
                                .comptes(new ArrayList<>())
                                .build();

                client2 = Client.builder()
                                .id(2L)
                                .identifiant("1010202K")
                                .groupe("OTHER CLIENT")
                                .relation("STE OTHER")
                                .comptes(new ArrayList<>())
                                .build();

                compte1 = Compte.builder()
                                .id(new CompteId("123", "IM02", "192019"))
                                .client(client1)
                                .build();

                compte2 = Compte.builder()
                                .id(new CompteId("120", "DT20", "001234"))
                                .client(client2)
                                .build();

                client1.getComptes().add(compte1);
                client2.getComptes().add(compte2);

                produit = ProduitRef.builder()
                                .id(10L)
                                .code("PROD_IND_TND")
                                .libelle("Crédit Finançant l'Industrie en dinars")
                                .build();

                objetCredit = ObjetCreditRef.builder()
                                .id(20L)
                                .code("OBJ_AGRO_ALIM")
                                .libelle("Agro-Alimentaires")
                                .build();
        }

        @Test
        @DisplayName("GET solicitation — throws ResourceNotFoundException for nonexistent client")
        void getSollicitation_shouldThrow404_whenClientDoesNotExist() {
                when(clientRepository.findById(999L)).thenReturn(Optional.empty());

                assertThatThrownBy(() -> sollicitationService.getSollicitationForClient(999L))
                                .isInstanceOf(ResourceNotFoundException.class)
                                .hasMessageContaining("Client");
        }

        @Test
        @DisplayName("GET solicitation — returns default response for client with no solicitation")
        void getSollicitation_shouldReturnDefaultResponse_whenNoSollicitationExists() {
                when(clientRepository.findById(1L)).thenReturn(Optional.of(client1));
                when(sollicitationRepository.findFirstByClientIdOrderByIdDesc(1L)).thenReturn(Optional.empty());

                SollicitationResponse response = sollicitationService.getSollicitationForClient(1L);

                assertThat(response).isNotNull();
                assertThat(response.getClientId()).isEqualTo(1L);
                assertThat(response.getId()).isNull();
                assertThat(response.getCompteNumeroComplet()).isEqualTo("123-IM02-192019");
        }

        @Test
        @DisplayName("SAVE solicitation — rejects compte belonging to another client")
        void saveSollicitation_shouldRejectAccount_whenAccountBelongsToAnotherClient() {
                SollicitationRequest request = SollicitationRequest.builder()
                                .dateDemande(LocalDate.now())
                                .compteCodeGuichet("120")
                                .compteCodeProduit("DT20")
                                .compteNumCompte("001234")
                                .produitId(10L)
                                .objetCreditId(20L)
                                .montantSollicite(new BigDecimal("10000.000"))
                                .dureeSolliciteeMois(12)
                                .build();

                when(clientRepository.findById(1L)).thenReturn(Optional.of(client1));
                when(compteRepository.findById(new CompteId("120", "DT20", "001234"))).thenReturn(Optional.of(compte2));

                assertThatThrownBy(() -> sollicitationService.saveOrUpdateSollicitation(1L, request))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("n'appartient pas au client");
        }

        @Test
        @DisplayName("SAVE solicitation — upserts solicitation and updates child items")
        void saveSollicitation_shouldSaveOrUpdateSollicitationSuccessfully() {
                SollicitationRequest request = SollicitationRequest.builder()
                                .dateDemande(LocalDate.now())
                                .compteCodeGuichet("123")
                                .compteCodeProduit("IM02")
                                .compteNumCompte("192019")
                                .produitId(10L)
                                .objetCreditId(20L)
                                .montantSollicite(new BigDecimal("50000.000"))
                                .dureeSolliciteeMois(24)
                                .periodicitePl("Trimestrielle")
                                .postesInvestissement(List.of(
                                                PosteInvestissementDto.builder().libelle("Équipement")
                                                                .montantSollicite(new BigDecimal("30000.000")).build(),
                                                PosteInvestissementDto.builder().libelle("Outillage")
                                                                .montantSollicite(new BigDecimal("20000.000")).build()))
                                .postesFinancement(List.of(
                                                PosteFinancementDto.builder().libelle("CMLT BNA")
                                                                .montantSollicite(new BigDecimal("50000.000")).build()))
                                .nouvellesGaranties(List.of(
                                                NouvelleGarantieDto.builder().natureGarantie("Hypothèque")
                                                                .estimation(new BigDecimal("60000.000")).build()))
                                .build();

                when(clientRepository.findById(1L)).thenReturn(Optional.of(client1));
                when(compteRepository.findById(new CompteId("123", "IM02", "192019"))).thenReturn(Optional.of(compte1));
                when(produitRefRepository.findById(10L)).thenReturn(Optional.of(produit));
                when(objetCreditRefRepository.findById(20L)).thenReturn(Optional.of(objetCredit));
                when(sollicitationRepository.findFirstByClientIdOrderByIdDesc(1L)).thenReturn(Optional.empty());

                Sollicitation savedEntity = Sollicitation.builder()
                                .id(100L)
                                .client(client1)
                                .compteDeblocage(compte1)
                                .produit(produit)
                                .objetCredit(objetCredit)
                                .dateDemande(LocalDate.now())
                                .montantSollicite(new BigDecimal("50000.000"))
                                .dureeSolliciteeMois(24)
                                .postesInvestissement(List.of(
                                                PosteInvestissement.builder().id(1L).libelle("Équipement")
                                                                .montantSollicite(new BigDecimal("30000.000")).build(),
                                                PosteInvestissement.builder().id(2L).libelle("Outillage")
                                                                .montantSollicite(new BigDecimal("20000.000")).build()))
                                .postesFinancement(List.of(
                                                PosteFinancement.builder().id(1L).libelle("CMLT BNA")
                                                                .montantSollicite(new BigDecimal("50000.000")).build()))
                                .nouvellesGaranties(List.of(
                                                NouvelleGarantie.builder().id(1L).natureGarantie("Hypothèque")
                                                                .estimation(new BigDecimal("60000.000")).build()))
                                .build();

                when(sollicitationRepository.save(any(Sollicitation.class))).thenReturn(savedEntity);

                SollicitationResponse response = sollicitationService.saveOrUpdateSollicitation(1L, request);

                assertThat(response).isNotNull();
                assertThat(response.getId()).isEqualTo(100L);
                assertThat(response.getTotalInvestissementSollicite()).isEqualByComparingTo("50000.000");
                assertThat(response.getTotalFinancementSollicite()).isEqualByComparingTo("50000.000");
                assertThat(response.getPostesInvestissement()).hasSize(2);
                assertThat(response.getPostesFinancement()).hasSize(1);
                assertThat(response.getNouvellesGaranties()).hasSize(1);
        }

        @Test
        @DisplayName("SAVE Phase 2 — preserves Phase 1 fields and saves all Phase 2 analysis & SWOT fields")
        void savePhase2_shouldPreservePhase1AndSavePhase2Fields() {
                SollicitationRequest request = SollicitationRequest.builder()
                                .dateDemande(LocalDate.now())
                                .compteCodeGuichet("123")
                                .compteCodeProduit("IM02")
                                .compteNumCompte("192019")
                                .produitId(10L)
                                .objetCreditId(20L)
                                .montantSollicite(new BigDecimal("100000.000"))
                                .dureeSolliciteeMois(60)
                                .apercuProjet("Projet industriel de grande envergure...")
                                .etudeMarche("Marché porteur en forte croissance...")
                                .syntheseRentabilite("TRI estimé à 18%...")
                                .swotForces("Atouts principaux de l'entreprise")
                                .swotFaiblesses("Dépendance à un fournisseur")
                                .swotOpportunites("Expansion régionale")
                                .swotMenaces("Fluctuation des devises")
                                .commentaireEtudeProjet("Avis favorable sous réserve d'hypothèque")
                                .build();

                when(clientRepository.findById(1L)).thenReturn(Optional.of(client1));
                when(compteRepository.findById(new CompteId("123", "IM02", "192019"))).thenReturn(Optional.of(compte1));
                when(produitRefRepository.findById(10L)).thenReturn(Optional.of(produit));
                when(objetCreditRefRepository.findById(20L)).thenReturn(Optional.of(objetCredit));
                when(sollicitationRepository.findFirstByClientIdOrderByIdDesc(1L)).thenReturn(Optional.empty());

                Sollicitation savedEntity = Sollicitation.builder()
                                .id(101L)
                                .client(client1)
                                .compteDeblocage(compte1)
                                .produit(produit)
                                .objetCredit(objetCredit)
                                .dateDemande(LocalDate.now())
                                .montantSollicite(new BigDecimal("100000.000"))
                                .dureeSolliciteeMois(60)
                                .apercuProjet("Projet industriel de grande envergure...")
                                .etudeMarche("Marché porteur en forte croissance...")
                                .syntheseRentabilite("TRI estimé à 18%...")
                                .swotForces("Atouts principaux de l'entreprise")
                                .swotFaiblesses("Dépendance à un fournisseur")
                                .swotOpportunites("Expansion régionale")
                                .swotMenaces("Fluctuation des devises")
                                .commentaireEtudeProjet("Avis favorable sous réserve d'hypothèque")
                                .build();

                when(sollicitationRepository.save(any(Sollicitation.class))).thenReturn(savedEntity);

                SollicitationResponse response = sollicitationService.saveOrUpdateSollicitation(1L, request);

                assertThat(response).isNotNull();
                assertThat(response.getMontantSollicite()).isEqualByComparingTo("100000.000");
                assertThat(response.getDureeSolliciteeMois()).isEqualTo(60);
                assertThat(response.getApercuProjet()).isEqualTo("Projet industriel de grande envergure...");
                assertThat(response.getSwotForces()).isEqualTo("Atouts principaux de l'entreprise");
                assertThat(response.getCommentaireEtudeProjet()).isEqualTo("Avis favorable sous réserve d'hypothèque");
        }

        @Test
        @DisplayName("CLEAR field explicitly — swotForces set to empty string clears it while preserving other fields")
        void clearFieldExplicitly_shouldUpdateTargetFieldToEmptyString() {
                SollicitationRequest request = SollicitationRequest.builder()
                                .dateDemande(LocalDate.now())
                                .compteCodeGuichet("123")
                                .compteCodeProduit("IM02")
                                .compteNumCompte("192019")
                                .produitId(10L)
                                .objetCreditId(20L)
                                .montantSollicite(new BigDecimal("100000.000"))
                                .dureeSolliciteeMois(60)
                                .apercuProjet("Projet industriel de grande envergure...")
                                .swotForces("") // Explicitly cleared
                                .build();

                when(clientRepository.findById(1L)).thenReturn(Optional.of(client1));
                when(compteRepository.findById(new CompteId("123", "IM02", "192019"))).thenReturn(Optional.of(compte1));
                when(produitRefRepository.findById(10L)).thenReturn(Optional.of(produit));
                when(objetCreditRefRepository.findById(20L)).thenReturn(Optional.of(objetCredit));
                when(sollicitationRepository.findFirstByClientIdOrderByIdDesc(1L)).thenReturn(Optional.empty());

                Sollicitation savedEntity = Sollicitation.builder()
                                .id(102L)
                                .client(client1)
                                .compteDeblocage(compte1)
                                .produit(produit)
                                .objetCredit(objetCredit)
                                .dateDemande(LocalDate.now())
                                .montantSollicite(new BigDecimal("100000.000"))
                                .dureeSolliciteeMois(60)
                                .apercuProjet("Projet industriel de grande envergure...")
                                .swotForces("")
                                .build();

                when(sollicitationRepository.save(any(Sollicitation.class))).thenReturn(savedEntity);

                SollicitationResponse response = sollicitationService.saveOrUpdateSollicitation(1L, request);

                assertThat(response).isNotNull();
                assertThat(response.getMontantSollicite()).isEqualByComparingTo("100000.000");
                assertThat(response.getApercuProjet()).isEqualTo("Projet industriel de grande envergure...");
                assertThat(response.getSwotForces()).isEmpty();
        }
}
