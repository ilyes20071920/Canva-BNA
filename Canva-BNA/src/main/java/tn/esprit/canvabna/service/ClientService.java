package tn.esprit.canvabna.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.canvabna.dto.ClientRequest;
import tn.esprit.canvabna.dto.ClientResponse;
import tn.esprit.canvabna.entity.Client;
import tn.esprit.canvabna.entity.Structure;
import tn.esprit.canvabna.exception.ResourceNotFoundException;
import tn.esprit.canvabna.mapper.ClientMapper;
import tn.esprit.canvabna.repository.ClientRepository;
import tn.esprit.canvabna.repository.StructureRepository;

import java.util.List;

/**
 * Service de gestion des clients entreprise (fiche signalétique).
 * Orchestre les opérations CRUD et la relation optionnelle vers {@link Structure}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final StructureRepository structureRepository;
    private final tn.esprit.canvabna.repository.CompteRepository compteRepository;
    private final ClientMapper clientMapper;

    @Transactional(readOnly = true)
    public List<ClientResponse> findAll() {
        log.debug("Fetching all clients");
        return clientRepository.findAll()
                .stream()
                .map(clientMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ClientResponse findById(final Long id) {
        log.debug("Fetching client by id: {}", id);
        return clientRepository.findById(id)
                .map(clientMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Client", "id", id));
    }

    @Transactional(readOnly = true)
    public ClientResponse findByIdentifiant(final String identifiant) {
        log.debug("Fetching client by identifiant: {}", identifiant);
        return clientRepository.findByIdentifiant(identifiant)
                .map(clientMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Client", "identifiant", identifiant));
    }

    @Transactional
    public ClientResponse create(final ClientRequest request) {
        log.info("Creating client with identifiant: {}", request.getIdentifiant());
        if (clientRepository.existsByIdentifiant(request.getIdentifiant())) {
            throw new IllegalArgumentException("L'identifiant '" + request.getIdentifiant() + "' est déjà enregistré");
        }
        final Client client = buildClientFromRequest(request, new Client());
        return clientMapper.toResponse(clientRepository.save(client));
    }

    @Transactional
    public ClientResponse update(final Long id, final ClientRequest request) {
        log.info("Updating client id: {}", id);
        final Client existing = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client", "id", id));

        // Vérifier que le nouvel identifiant n'est pas pris par un autre client
        if (!existing.getIdentifiant().equals(request.getIdentifiant())
                && clientRepository.existsByIdentifiant(request.getIdentifiant())) {
            throw new IllegalArgumentException("L'identifiant '" + request.getIdentifiant() + "' est déjà enregistré");
        }

        buildClientFromRequest(request, existing);
        return clientMapper.toResponse(clientRepository.save(existing));
    }

    @Transactional
    public void delete(final Long id) {
        log.info("Deleting client id: {}", id);
        if (!clientRepository.existsById(id)) {
            throw new ResourceNotFoundException("Client", "id", id);
        }
        clientRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public tn.esprit.canvabna.dto.EngagementsActiviteResponse getEngagementsActivite(final Long clientId) {
        log.debug("Fetching engagements and activite for client id: {}", clientId);
        if (!clientRepository.existsById(clientId)) {
            throw new ResourceNotFoundException("Client", "id", clientId);
        }

        // 1. Récupérer les VRAIS comptes du client
        List<tn.esprit.canvabna.entity.Compte> comptes = compteRepository.findByClientId(clientId);
        
        // 2. Map avec des montants fictifs (MOCK) pour tester l'interface
        List<tn.esprit.canvabna.dto.CompteValideResponse> comptesResponses = new java.util.ArrayList<>();
        java.util.Map<String, java.math.BigDecimal> soldesMap = new java.util.HashMap<>();

        // Utilisation d'un pseudo-aléatoire basé sur le numéro de compte pour avoir toujours le même montant pour un compte
        for (tn.esprit.canvabna.entity.Compte c : comptes) {
            String guichet = c.getId().getCodeGuichet() != null ? c.getId().getCodeGuichet() : "";
            String produit = c.getId().getCodeProduit() != null ? c.getId().getCodeProduit() : "";
            String num = c.getId().getNumCompte() != null ? c.getId().getNumCompte() : "";
            String numeroCompletBrut = guichet + produit + num;
            
            // Génération d'un solde fictif déterministe (ex: en utilisant la longueur ou le hash du compte)
            long fakeSeed = Math.abs(numeroCompletBrut.hashCode());
            java.math.BigDecimal fakeSolde = new java.math.BigDecimal((fakeSeed % 900000) + 10000); // Solde entre 10k et 910k
            java.math.BigDecimal fakeAutorisation = new java.math.BigDecimal((fakeSeed % 50000));
            
            comptesResponses.add(tn.esprit.canvabna.dto.CompteValideResponse.builder()
                .numeroCompte(numeroCompletBrut)
                .dateOuverture(java.time.LocalDate.now().minusDays(fakeSeed % 2000))
                .autorisation(fakeAutorisation)
                .dateAutorisation(java.time.LocalDate.now().plusDays(fakeSeed % 500))
                .solde(fakeSolde)
                .build());
                
            // Extraction du type de compte (ex: IM02, IM85) via substring(3, 7)
            String typeCompte = numeroCompletBrut.length() >= 7 ? numeroCompletBrut.substring(3, 7) : produit;
            if (typeCompte.isEmpty()) typeCompte = "INCONNU";
            
            // Agrégation
            soldesMap.put(typeCompte, soldesMap.getOrDefault(typeCompte, java.math.BigDecimal.ZERO).add(fakeSolde));
        }

        // 3. Préparer le second tableau
        List<tn.esprit.canvabna.dto.SoldeParTypeResponse> soldesParType = new java.util.ArrayList<>();
        for (java.util.Map.Entry<String, java.math.BigDecimal> entry : soldesMap.entrySet()) {
            soldesParType.add(tn.esprit.canvabna.dto.SoldeParTypeResponse.builder()
                .typeCompte(entry.getKey())
                .soldeAlgebrique(entry.getValue())
                .build());
        }

        // 4. Préparer les Engagements BNA (DT)
        List<tn.esprit.canvabna.dto.EngagementBnaRowResponse> engagementsBna = java.util.Arrays.asList(
            tn.esprit.canvabna.dto.EngagementBnaRowResponse.builder()
                .code("DEBIT")
                .forme("Débit en compte")
                .encours(new java.math.BigDecimal("69107"))
                .ir(java.math.BigDecimal.ZERO)
                .detailsTitle("DÉTAILS : DÉBIT EN COMPTE")
                .subtotalLabel("S/ Total Débit")
                .details(java.util.Collections.singletonList(
                    tn.esprit.canvabna.dto.EngagementDetailResponse.builder()
                        .formeCredit("Facilité de Caisse / Débit en compte")
                        .encours(new java.math.BigDecimal("69107"))
                        .ir(java.math.BigDecimal.ZERO)
                        .build()
                ))
                .build(),
            tn.esprit.canvabna.dto.EngagementBnaRowResponse.builder()
                .code("ESCOMPTE")
                .forme("ESCOMPTE")
                .autorise(java.math.BigDecimal.ZERO)
                .encours(new java.math.BigDecimal("694769"))
                .impayes(java.math.BigDecimal.ZERO)
                .ir(java.math.BigDecimal.ZERO)
                .detailsTitle("DÉTAILS : ESCOMPTE")
                .subtotalLabel("S/ Total ESC")
                .details(java.util.Collections.singletonList(
                    tn.esprit.canvabna.dto.EngagementDetailResponse.builder()
                        .formeCredit("Escompte Commercial sur la Tunisie")
                        .autorise(java.math.BigDecimal.ZERO)
                        .encours(new java.math.BigDecimal("694769"))
                        .impayes(java.math.BigDecimal.ZERO)
                        .build()
                ))
                .build(),
            tn.esprit.canvabna.dto.EngagementBnaRowResponse.builder()
                .code("CME")
                .forme("C.M.E")
                .autorise(java.math.BigDecimal.ZERO)
                .encours(new java.math.BigDecimal("4000000"))
                .impayes(java.math.BigDecimal.ZERO)
                .ir(java.math.BigDecimal.ZERO)
                .detailsTitle("DÉTAILS : C.M.E")
                .subtotalLabel("S/ Total CME")
                .details(java.util.Collections.singletonList(
                    tn.esprit.canvabna.dto.EngagementDetailResponse.builder()
                        .formeCredit("Crédit de Financement de Stocks en dinars")
                        .autorise(java.math.BigDecimal.ZERO)
                        .encours(new java.math.BigDecimal("4000000"))
                        .impayes(java.math.BigDecimal.ZERO)
                        .ir(java.math.BigDecimal.ZERO)
                        .build()
                ))
                .build(),
            tn.esprit.canvabna.dto.EngagementBnaRowResponse.builder()
                .code("EPS")
                .forme("E.P.S")
                .autorise(java.math.BigDecimal.ZERO)
                .encours(new java.math.BigDecimal("2794700"))
                .ir(java.math.BigDecimal.ZERO)
                .detailsTitle("DÉTAILS : E.P.S")
                .subtotalLabel("S/ Total EPS")
                .details(java.util.Arrays.asList(
                    tn.esprit.canvabna.dto.EngagementDetailResponse.builder()
                        .formeCredit("Cautions d'approvisionnement")
                        .autorise(java.math.BigDecimal.ZERO)
                        .echeance(java.time.LocalDate.of(2026, 10, 31))
                        .encours(new java.math.BigDecimal("600000"))
                        .build(),
                    tn.esprit.canvabna.dto.EngagementDetailResponse.builder()
                        .formeCredit("Caution d'admission temporaire")
                        .autorise(java.math.BigDecimal.ZERO)
                        .encours(new java.math.BigDecimal("2068600"))
                        .build(),
                    tn.esprit.canvabna.dto.EngagementDetailResponse.builder()
                        .formeCredit("Caution fiscale")
                        .autorise(java.math.BigDecimal.ZERO)
                        .encours(new java.math.BigDecimal("126100"))
                        .build()
                ))
                .build(),
            tn.esprit.canvabna.dto.EngagementBnaRowResponse.builder()
                .code("SBE")
                .forme("SBE")
                .encours(new java.math.BigDecimal("8998990"))
                .impayes(java.math.BigDecimal.ZERO)
                .ir(java.math.BigDecimal.ZERO)
                .detailsTitle("DÉTAILS : SBE")
                .subtotalLabel("S/ Total SBE")
                .details(java.util.Collections.singletonList(
                    tn.esprit.canvabna.dto.EngagementDetailResponse.builder()
                        .formeCredit("Lettre de Crédit (au 05/2026)")
                        .encours(new java.math.BigDecimal("8998990"))
                        .build()
                ))
                .build(),
            tn.esprit.canvabna.dto.EngagementBnaRowResponse.builder()
                .code("LC")
                .forme("Lettre De crédit")
                .encours(new java.math.BigDecimal("8596285"))
                .impayes(java.math.BigDecimal.ZERO)
                .ir(java.math.BigDecimal.ZERO)
                .detailsTitle("DÉTAILS : LETTRE DE CRÉDIT")
                .subtotalLabel("S/ Total LC")
                .details(java.util.Collections.singletonList(
                    tn.esprit.canvabna.dto.EngagementDetailResponse.builder()
                        .formeCredit("Lettre de Crédit commerciale")
                        .autorise(java.math.BigDecimal.ZERO)
                        .encours(new java.math.BigDecimal("8596285"))
                        .impayes(java.math.BigDecimal.ZERO)
                        .ir(java.math.BigDecimal.ZERO)
                        .build()
                ))
                .build(),
            tn.esprit.canvabna.dto.EngagementBnaRowResponse.builder()
                .code("BPN")
                .forme("BPN")
                .autorise(java.math.BigDecimal.ZERO)
                .encours(java.math.BigDecimal.ZERO)
                .impayes(java.math.BigDecimal.ZERO)
                .ir(java.math.BigDecimal.ZERO)
                .detailsTitle("DÉTAILS : BPN")
                .subtotalLabel("S/ Total BPN")
                .details(java.util.Collections.singletonList(
                    tn.esprit.canvabna.dto.EngagementDetailResponse.builder()
                        .numDecisionCrd("")
                        .autorise(java.math.BigDecimal.ZERO)
                        .utilise(java.math.BigDecimal.ZERO)
                        .formeCredit("Billets à Ordre / BPN")
                        .aEchoirEnPrincipal(java.math.BigDecimal.ZERO)
                        .impayesPI(java.math.BigDecimal.ZERO)
                        .ir(java.math.BigDecimal.ZERO)
                        .build()
                ))
                .build(),
            tn.esprit.canvabna.dto.EngagementBnaRowResponse.builder()
                .code("CMT")
                .forme("CMT")
                .autorise(java.math.BigDecimal.ZERO)
                .encours(java.math.BigDecimal.ZERO)
                .impayes(java.math.BigDecimal.ZERO)
                .ir(java.math.BigDecimal.ZERO)
                .detailsTitle("DÉTAILS : CMT")
                .subtotalLabel("S/ Total CMT")
                .details(java.util.Collections.singletonList(
                    tn.esprit.canvabna.dto.EngagementDetailResponse.builder()
                        .numDecisionCrd("")
                        .autorise(java.math.BigDecimal.ZERO)
                        .utilise(java.math.BigDecimal.ZERO)
                        .formeCredit("Crédit à Moyen Terme (CMT)")
                        .aEchoirEnPrincipal(java.math.BigDecimal.ZERO)
                        .impayesPI(java.math.BigDecimal.ZERO)
                        .ir(java.math.BigDecimal.ZERO)
                        .build()
                ))
                .build(),
            tn.esprit.canvabna.dto.EngagementBnaRowResponse.builder()
                .code("AGRICOLE")
                .forme("AGRICOLE")
                .autorise(java.math.BigDecimal.ZERO)
                .encours(java.math.BigDecimal.ZERO)
                .impayes(java.math.BigDecimal.ZERO)
                .ir(java.math.BigDecimal.ZERO)
                .detailsTitle("DÉTAILS : AGRICOLE")
                .subtotalLabel("S/ Total AGRICOLE")
                .details(java.util.Collections.singletonList(
                    tn.esprit.canvabna.dto.EngagementDetailResponse.builder()
                        .formeCredit("Crédit Campagne Agricole")
                        .autorise(java.math.BigDecimal.ZERO)
                        .encours(java.math.BigDecimal.ZERO)
                        .impayes(java.math.BigDecimal.ZERO)
                        .ir(java.math.BigDecimal.ZERO)
                        .build()
                ))
                .build()
        );

        return tn.esprit.canvabna.dto.EngagementsActiviteResponse.builder()
                .comptes(comptesResponses)
                .soldesDisponibles(!soldesParType.isEmpty())
                .soldesParType(soldesParType)
                .engagementsBna(engagementsBna)
                .build();
    }

    @Transactional(readOnly = true)
    public tn.esprit.canvabna.dto.ActiviteResponse getActivite(final Long clientId) {
        log.debug("Fetching activite for client id: {}", clientId);
        if (!clientRepository.existsById(clientId)) {
            throw new ResourceNotFoundException("Client", "id", clientId);
        }

        // =========================================================================
        // TEMPORARY DEVELOPMENT DATA (MOCK) - DO NOT PERSIST IN DB
        // TODO: Replace this memory mock with real database/service queries once ready.
        // =========================================================================
        List<tn.esprit.canvabna.dto.ActiviteAnneeResponse> activities = java.util.Arrays.asList(
            tn.esprit.canvabna.dto.ActiviteAnneeResponse.builder()
                .annee(2024)
                .totalMouvement(new java.math.BigDecimal("14388891"))
                .ca(new java.math.BigDecimal("188571440"))
                .partConfiee(new java.math.BigDecimal("7.63"))
                .partEngagement(java.math.BigDecimal.ZERO)
                .build(),
            tn.esprit.canvabna.dto.ActiviteAnneeResponse.builder()
                .annee(2025)
                .totalMouvement(new java.math.BigDecimal("10554833"))
                .ca(new java.math.BigDecimal("183487104"))
                .partConfiee(new java.math.BigDecimal("5.75"))
                .partEngagement(java.math.BigDecimal.ZERO)
                .build(),
            tn.esprit.canvabna.dto.ActiviteAnneeResponse.builder()
                .annee(2026)
                .totalMouvement(new java.math.BigDecimal("4162329"))
                .ca(new java.math.BigDecimal("96745210"))
                .partConfiee(new java.math.BigDecimal("4.30"))
                .partEngagement(java.math.BigDecimal.ZERO)
                .build()
        );

        return tn.esprit.canvabna.dto.ActiviteResponse.builder()
                .dataAvailable(true)
                .activities(activities)
                .build();
    }

    // ---------------------------------------------------------------- Private helpers

    /**
     * Construit ou met à jour un {@link Client} à partir d'un {@link ClientRequest}.
     * Résout la référence optionnelle vers {@link Structure}.
     */
    private Client buildClientFromRequest(final ClientRequest request, final Client client) {
        client.setIdentifiant(request.getIdentifiant());
        client.setGroupe(request.getGroupe());
        client.setRelation(request.getRelation());
        client.setActivite(request.getActivite());
        client.setSegment(request.getSegment());
        client.setSiegeSocial(request.getSiegeSocial());
        client.setSecteur(request.getSecteur());
        client.setDateEntreeRelation(request.getDateEntreeRelation());
        client.setFormeJuridique(request.getFormeJuridique());
        client.setCapitalSocial(request.getCapitalSocial());
        client.setAgence(request.getAgence());
        client.setDirectionRegionale(request.getDirectionRegionale());

        if (request.getStructureId() != null) {
            final Structure structure = structureRepository.findById(request.getStructureId())
                    .orElseThrow(() -> new ResourceNotFoundException("Structure", "id", request.getStructureId()));
            client.setStructure(structure);
        } else {
            client.setStructure(null);
        }
        return client;
    }
}
