package tn.esprit.canvabna.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.canvabna.dto.GarantieResponse;
import tn.esprit.canvabna.dto.GarantiesSummaryResponse;
import tn.esprit.canvabna.entity.Garantie;
import tn.esprit.canvabna.exception.ResourceNotFoundException;
import tn.esprit.canvabna.repository.ClientRepository;
import tn.esprit.canvabna.repository.GarantieRepository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service de lecture des garanties en possession d'un client.
 *
 * <p>Ce service ne fait que lire les données déjà persistées par {@link tn.esprit.canvabna.config.DevGuaranteeSeeder}.
 * Il calcule dynamiquement les totaux ("Total Charges Inscrites" et "Dont BNA") à partir
 * des garanties retournées — ces totaux ne sont jamais stockés en base.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GarantieService {

    private static final String BENEFICIAIRE_BNA = "BANQUE NATIONALE AGRICOLE";

    private final GarantieRepository garantieRepository;
    private final ClientRepository clientRepository;

    /**
     * Retourne toutes les garanties d'un client avec les totaux calculés dynamiquement.
     *
     * @param clientId identifiant JPA du client
     * @return résumé des garanties avec totaux
     * @throws ResourceNotFoundException si le client n'existe pas
     */
    @Transactional(readOnly = true)
    public GarantiesSummaryResponse getGarantiesForClient(final Long clientId) {
        log.debug("Fetching garanties for client id: {}", clientId);

        if (!clientRepository.existsById(clientId)) {
            throw new ResourceNotFoundException("Client", "id", clientId);
        }

        final List<Garantie> garanties = garantieRepository.findByClientIdOrderById(clientId);

        final List<GarantieResponse> garantieResponses = garanties.stream()
                .map(this::toResponse)
                .toList();

        // ---------------------------------------------------------------
        // Totaux calculés dynamiquement — JAMAIS persistés en base
        // ---------------------------------------------------------------
        final BigDecimal totalChargesInscrites = garanties.stream()
                .map(g -> g.getCharges() != null ? g.getCharges() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // HYPOTHÈSE TEMPORAIRE DE DÉVELOPPEMENT :
        // dontBna = somme des charges dont le bénéficiaire est "BANQUE NATIONALE AGRICOLE".
        // Ce calcul devra être révisé avec le vrai modèle de charges inscrites BNA.
        final BigDecimal dontBna = garanties.stream()
                .filter(g -> BENEFICIAIRE_BNA.equalsIgnoreCase(g.getBeneficiaire()))
                .map(g -> g.getCharges() != null ? g.getCharges() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        log.debug("Client {} has {} garantie(s). totalCharges={}, dontBna={}",
                clientId, garanties.size(), totalChargesInscrites, dontBna);

        return GarantiesSummaryResponse.builder()
                .garanties(garantieResponses)
                .totalChargesInscrites(totalChargesInscrites)
                .dontBna(dontBna)
                .build();
    }

    @Transactional
    public GarantieResponse addGarantieForClient(final Long clientId, final tn.esprit.canvabna.dto.GarantieRequest request) {
        log.info("Adding new garantie for client id: {}", clientId);
        final tn.esprit.canvabna.entity.Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client", "id", clientId));

        final Garantie entity = Garantie.builder()
                .client(client)
                .libelle(request.getLibelle())
                .consistance(request.getConsistance() != null ? request.getConsistance() : "-")
                .valeur(request.getValeur() != null ? request.getValeur() : BigDecimal.ZERO)
                .dateExpertise(request.getDateExpertise())
                .rang(request.getRang() != null ? request.getRang() : 1)
                .beneficiaire(request.getBeneficiaire() != null ? request.getBeneficiaire() : BENEFICIAIRE_BNA)
                .charges(request.getCharges() != null ? request.getCharges() : BigDecimal.ZERO)
                .affectation(request.getAffectation() != null ? request.getAffectation() : "Nouvelle Garantie Sollicitée")
                .numSeqGar(request.getNumSeqGar() != null ? request.getNumSeqGar() : "NG1")
                .idCredit(request.getIdCredit() != null ? request.getIdCredit() : "01")
                .build();

        final Garantie saved = garantieRepository.save(entity);
        return toResponse(saved);
    }

    @Transactional
    public void deleteGarantie(final Long clientId, final Long garantieId) {
        log.info("Deleting garantie id: {} for client id: {}", garantieId, clientId);
        final Garantie g = garantieRepository.findById(garantieId)
                .orElseThrow(() -> new ResourceNotFoundException("Garantie", "id", garantieId));
        if (!g.getClient().getId().equals(clientId)) {
            throw new IllegalArgumentException("La garantie n'appartient pas au client " + clientId);
        }
        garantieRepository.delete(g);
    }

    // ---------------------------------------------------------------- Mapping

    private GarantieResponse toResponse(final Garantie g) {
        return GarantieResponse.builder()
                .id(g.getId())
                .libelle(g.getLibelle())
                .consistance(g.getConsistance())
                .valeur(g.getValeur())
                .dateExpertise(g.getDateExpertise())
                .rang(g.getRang())
                .beneficiaire(g.getBeneficiaire())
                .charges(g.getCharges())
                .affectation(g.getAffectation())
                .numSeqGar(g.getNumSeqGar())
                .idCredit(g.getIdCredit())
                .build();
    }
}
