package tn.esprit.canvabna.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.canvabna.dto.*;
import tn.esprit.canvabna.entity.*;
import tn.esprit.canvabna.exception.ResourceNotFoundException;
import tn.esprit.canvabna.repository.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SollicitationService {

        private final SollicitationRepository sollicitationRepository;
        private final ClientRepository clientRepository;
        private final CompteRepository compteRepository;
        private final ProduitRefRepository produitRefRepository;
        private final ObjetCreditRefRepository objetCreditRefRepository;

        @Transactional(readOnly = true)
        public SollicitationResponse getSollicitationForClient(Long clientId) {
                Client client = clientRepository.findById(clientId)
                                .orElseThrow(() -> new ResourceNotFoundException("Client", "id", clientId));

                return sollicitationRepository.findFirstByClientIdOrderByIdDesc(clientId)
                                .map(this::mapToResponse)
                                .orElseGet(() -> buildDefaultResponse(client));
        }

        @Transactional(readOnly = true)
        public List<SollicitationResponse> getAllSollicitationsForClient(Long clientId) {
                clientRepository.findById(clientId)
                                .orElseThrow(() -> new ResourceNotFoundException("Client", "id", clientId));

                return sollicitationRepository.findAllByClientId(clientId).stream()
                                .sorted((s1, s2) -> s2.getId().compareTo(s1.getId())) // descending order
                                .map(this::mapToResponse)
                                .collect(Collectors.toList());
        }

        @Transactional
        public SollicitationResponse createNouvelleSollicitation(Long clientId, SollicitationRequest request) {
                Client client = clientRepository.findById(clientId)
                                .orElseThrow(() -> new ResourceNotFoundException("Client", "id", clientId));

                Sollicitation sollicitation = Sollicitation.builder()
                                .client(client)
                                .statut(StatutSollicitation.BROUILLON)
                                .postesInvestissement(new ArrayList<>())
                                .postesFinancement(new ArrayList<>())
                                .nouvellesGaranties(new ArrayList<>())
                                .build();

                return populateAndSaveSollicitation(sollicitation, clientId, request, true);
        }

        @Transactional
        public SollicitationResponse saveOrUpdateSollicitation(Long clientId, SollicitationRequest request) {
                Client client = clientRepository.findById(clientId)
                                .orElseThrow(() -> new ResourceNotFoundException("Client", "id", clientId));

                Sollicitation sollicitation = sollicitationRepository.findFirstByClientIdOrderByIdDesc(clientId)
                                .orElseGet(() -> Sollicitation.builder()
                                                .client(client)
                                                .statut(StatutSollicitation.BROUILLON)
                                                .postesInvestissement(new ArrayList<>())
                                                .postesFinancement(new ArrayList<>())
                                                .nouvellesGaranties(new ArrayList<>())
                                                .build());

                return populateAndSaveSollicitation(sollicitation, clientId, request, false);
        }

        private SollicitationResponse populateAndSaveSollicitation(Sollicitation sollicitation, Long clientId,
                        SollicitationRequest request, boolean forceFirstSave) {
                Compte compteDeblocage = null;
                if (request.getCompteCodeGuichet() != null && !request.getCompteCodeGuichet().isBlank()
                                && request.getCompteCodeProduit() != null && !request.getCompteCodeProduit().isBlank()
                                && request.getCompteNumCompte() != null && !request.getCompteNumCompte().isBlank()) {

                        CompteId compteId = new CompteId(request.getCompteCodeGuichet(), request.getCompteCodeProduit(),
                                        request.getCompteNumCompte());
                        compteDeblocage = compteRepository.findById(compteId)
                                        .orElseThrow(() -> new ResourceNotFoundException("Compte", "clé",
                                                        request.getCompteCodeGuichet() + "-"
                                                                        + request.getCompteCodeProduit() + "-"
                                                                        + request.getCompteNumCompte()));

                        if (!compteDeblocage.getClient().getId().equals(clientId)) {
                                throw new IllegalArgumentException(
                                                "Le compte de déblocage spécifié n'appartient pas au client "
                                                                + clientId);
                        }
                }

                ProduitRef produitRef = produitRefRepository.findById(request.getProduitId())
                                .orElseThrow(() -> new ResourceNotFoundException("Produit", "id",
                                                request.getProduitId()));

                ObjetCreditRef objetCreditRef = objetCreditRefRepository.findById(request.getObjetCreditId())
                                .orElseThrow(() -> new ResourceNotFoundException("ObjetCredit", "id",
                                                request.getObjetCreditId()));

                boolean isFirstSave = forceFirstSave || sollicitation.getStatut() == StatutSollicitation.BROUILLON
                                || sollicitation.getId() == null;

                if (isFirstSave) {
                        sollicitation.setDateDemande(
                                        request.getDateDemande() != null ? request.getDateDemande() : LocalDate.now());
                        sollicitation.setCompteDeblocage(compteDeblocage);
                        sollicitation.setProduit(produitRef);
                        sollicitation.setObjetCredit(objetCreditRef);
                        sollicitation.setDureeSolliciteeMois(
                                        request.getDureeSolliciteeMois() != null ? request.getDureeSolliciteeMois()
                                                        : 0);
                        sollicitation.setPeriodicitePl(request.getPeriodicitePl());
                        sollicitation.setNbrePeriodesSolliciteesPl(request.getNbrePeriodesSolliciteesPl());
                        sollicitation.setNbrePeriodesGracePl(request.getNbrePeriodesGracePl());
                        sollicitation.setPeriodiciteInt(request.getPeriodiciteInt());
                        sollicitation.setNbrePeriodesSolliciteesInt(request.getNbrePeriodesSolliciteesInt());
                        sollicitation.setNbrePeriodesGraceInt(request.getNbrePeriodesGraceInt());
                }

                sollicitation.setMontantSollicite(request.getMontantSollicite() != null ? request.getMontantSollicite()
                                : BigDecimal.ZERO);
                sollicitation.setTypeTauxSollicite(request.getTypeTauxSollicite());
                sollicitation.setMarge(request.getMarge());
                sollicitation.setTauxSollicite(request.getTauxSollicite());
                sollicitation.setTypeCommission(request.getTypeCommission());
                sollicitation.setCommissionForfaitaireSollicitee(request.getCommissionForfaitaireSollicitee());
                sollicitation.setCommentaireDr(request.getCommentaireDr());
                sollicitation.setApercuProjet(request.getApercuProjet());
                sollicitation.setEtudeMarche(request.getEtudeMarche());
                sollicitation.setSyntheseRentabilite(request.getSyntheseRentabilite());
                sollicitation.setSwotForces(request.getSwotForces());
                sollicitation.setSwotFaiblesses(request.getSwotFaiblesses());
                sollicitation.setSwotOpportunites(request.getSwotOpportunites());
                sollicitation.setSwotMenaces(request.getSwotMenaces());
                sollicitation.setCommentaireEtudeProjet(request.getCommentaireEtudeProjet());

                sollicitation.getPostesInvestissement().clear();
                if (request.getPostesInvestissement() != null) {
                        for (PosteInvestissementDto dto : request.getPostesInvestissement()) {
                                sollicitation.getPostesInvestissement().add(PosteInvestissement.builder()
                                                .libelle(dto.getLibelle())
                                                .montantSollicite(dto.getMontantSollicite() != null
                                                                ? dto.getMontantSollicite()
                                                                : BigDecimal.ZERO)
                                                .sollicitation(sollicitation)
                                                .build());
                        }
                }

                sollicitation.getPostesFinancement().clear();
                if (request.getPostesFinancement() != null) {
                        for (PosteFinancementDto dto : request.getPostesFinancement()) {
                                sollicitation.getPostesFinancement().add(PosteFinancement.builder()
                                                .libelle(dto.getLibelle())
                                                .montantSollicite(dto.getMontantSollicite() != null
                                                                ? dto.getMontantSollicite()
                                                                : BigDecimal.ZERO)
                                                .sollicitation(sollicitation)
                                                .build());
                        }
                }

                sollicitation.getNouvellesGaranties().clear();
                if (request.getNouvellesGaranties() != null) {
                        for (NouvelleGarantieDto dto : request.getNouvellesGaranties()) {
                                sollicitation.getNouvellesGaranties().add(NouvelleGarantie.builder()
                                                .natureGarantie(dto.getNatureGarantie())
                                                .estimation(dto.getEstimation())
                                                .dateExpertise(dto.getDateExpertise())
                                                .chargesInscrites(dto.getChargesInscrites())
                                                .sollicitation(sollicitation)
                                                .build());
                        }
                }

                if (isFirstSave) {
                        sollicitation.setStatut(StatutSollicitation.EN_ATTENTE_DECISION);
                        log.info("Sollicitation client {} : BROUILLON → EN_ATTENTE_DECISION", clientId);
                }

                Sollicitation saved = sollicitationRepository.save(sollicitation);
                return mapToResponse(saved);
        }

        /**
         * Enregistre la décision finale (ACCEPTE ou REFUSE) sur une sollicitation.
         * Réservé aux rôles ROLE_CHEF_DIVISION et ROLE_ADMIN.
         *
         * @param clientId id du client
         * @param request  payload contenant la décision et le commentaire
         * @return la sollicitation mise à jour
         */
        @Transactional
        public SollicitationResponse enregistrerDecision(Long clientId, DecisionRequest request) {
                Sollicitation sollicitation = sollicitationRepository.findFirstByClientIdOrderByIdDesc(clientId)
                                .orElseThrow(() -> new ResourceNotFoundException("Sollicitation", "clientId",
                                                clientId));

                if (sollicitation.getStatut() != StatutSollicitation.EN_ATTENTE_DECISION) {
                        throw new IllegalStateException(
                                        "Impossible de prendre une décision : la sollicitation est en statut '"
                                                        + sollicitation.getStatut()
                                                        + "'. Statut requis : EN_ATTENTE_DECISION.");
                }

                StatutSollicitation nouveauStatut = "ACCEPTE".equals(request.getDecision())
                                ? StatutSollicitation.ACCEPTE
                                : StatutSollicitation.REFUSE;

                sollicitation.setDecision(request.getDecision());
                sollicitation.setCommentaireDecision(request.getCommentaireDecision());
                sollicitation.setDateDecision(LocalDate.now());
                sollicitation.setStatut(nouveauStatut);

                log.info("Sollicitation client {} : EN_ATTENTE_DECISION → {} (commentaire: {})",
                                clientId, nouveauStatut, request.getCommentaireDecision());

                Sollicitation saved = sollicitationRepository.save(sollicitation);
                return mapToResponse(saved);
        }

        private SollicitationResponse mapToResponse(Sollicitation entity) {
                BigDecimal totalInvest = entity.getPostesInvestissement().stream()
                                .map(PosteInvestissement::getMontantSollicite)
                                .filter(m -> m != null)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal totalFinanc = entity.getPostesFinancement().stream()
                                .map(PosteFinancement::getMontantSollicite)
                                .filter(m -> m != null)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                Compte c = entity.getCompteDeblocage();
                String numComplet = null;
                if (c != null && c.getId() != null) {
                        numComplet = String.format("%s-%s-%s", c.getId().getCodeGuichet(), c.getId().getCodeProduit(),
                                        c.getId().getNumCompte());
                }

                return SollicitationResponse.builder()
                                .id(entity.getId())
                                .clientId(entity.getClient() != null ? entity.getClient().getId() : null)
                                .clientIdentifiant(
                                                entity.getClient() != null ? entity.getClient().getIdentifiant() : null)
                                .clientRelation(entity.getClient() != null ? entity.getClient().getRelation() : null)
                                .clientGroupe(entity.getClient() != null ? entity.getClient().getGroupe() : null)
                                .dateDemande(entity.getDateDemande())
                                .compteCodeGuichet(c != null && c.getId() != null ? c.getId().getCodeGuichet() : null)
                                .compteCodeProduit(c != null && c.getId() != null ? c.getId().getCodeProduit() : null)
                                .compteNumCompte(c != null && c.getId() != null ? c.getId().getNumCompte() : null)
                                .compteNumeroComplet(numComplet)
                                .produit(entity.getProduit() != null ? ReferenceItemDto.builder()
                                                .id(entity.getProduit().getId())
                                                .code(entity.getProduit().getCode())
                                                .libelle(entity.getProduit().getLibelle())
                                                .build() : null)
                                .objetCredit(entity.getObjetCredit() != null ? ReferenceItemDto.builder()
                                                .id(entity.getObjetCredit().getId())
                                                .code(entity.getObjetCredit().getCode())
                                                .libelle(entity.getObjetCredit().getLibelle())
                                                .build() : null)
                                .montantSollicite(entity.getMontantSollicite())
                                .dureeSolliciteeMois(entity.getDureeSolliciteeMois())
                                .periodicitePl(entity.getPeriodicitePl())
                                .nbrePeriodesSolliciteesPl(entity.getNbrePeriodesSolliciteesPl())
                                .nbrePeriodesGracePl(entity.getNbrePeriodesGracePl())
                                .periodiciteInt(entity.getPeriodiciteInt())
                                .nbrePeriodesSolliciteesInt(entity.getNbrePeriodesSolliciteesInt())
                                .nbrePeriodesGraceInt(entity.getNbrePeriodesGraceInt())
                                .typeTauxSollicite(entity.getTypeTauxSollicite())
                                .marge(entity.getMarge())
                                .tauxSollicite(entity.getTauxSollicite())
                                .typeCommission(entity.getTypeCommission())
                                .commissionForfaitaireSollicitee(entity.getCommissionForfaitaireSollicitee())
                                .commentaireDr(entity.getCommentaireDr())
                                .apercuProjet(entity.getApercuProjet())
                                .etudeMarche(entity.getEtudeMarche())
                                .syntheseRentabilite(entity.getSyntheseRentabilite())
                                .swotForces(entity.getSwotForces())
                                .swotFaiblesses(entity.getSwotFaiblesses())
                                .swotOpportunites(entity.getSwotOpportunites())
                                .swotMenaces(entity.getSwotMenaces())
                                .commentaireEtudeProjet(entity.getCommentaireEtudeProjet())
                                .statut(entity.getStatut() != null ? entity.getStatut().name()
                                                : StatutSollicitation.BROUILLON.name())
                                .decision(entity.getDecision())
                                .commentaireDecision(entity.getCommentaireDecision())
                                .dateDecision(entity.getDateDecision())
                                .totalInvestissementSollicite(totalInvest)
                                .totalFinancementSollicite(totalFinanc)
                                .postesInvestissement(entity.getPostesInvestissement().stream()
                                                .map(pi -> PosteInvestissementDto.builder()
                                                                .id(pi.getId())
                                                                .libelle(pi.getLibelle())
                                                                .montantSollicite(pi.getMontantSollicite())
                                                                .build())
                                                .collect(Collectors.toList()))
                                .postesFinancement(entity.getPostesFinancement().stream()
                                                .map(pf -> PosteFinancementDto.builder()
                                                                .id(pf.getId())
                                                                .libelle(pf.getLibelle())
                                                                .montantSollicite(pf.getMontantSollicite())
                                                                .build())
                                                .collect(Collectors.toList()))
                                .nouvellesGaranties(entity.getNouvellesGaranties().stream()
                                                .map(ng -> NouvelleGarantieDto.builder()
                                                                .id(ng.getId())
                                                                .natureGarantie(ng.getNatureGarantie())
                                                                .estimation(ng.getEstimation())
                                                                .dateExpertise(ng.getDateExpertise())
                                                                .chargesInscrites(ng.getChargesInscrites())
                                                                .build())
                                                .collect(Collectors.toList()))
                                .build();
        }

        private SollicitationResponse buildDefaultResponse(Client client) {
                String codeGuichet = null;
                String codeProduit = null;
                String numCompte = null;
                String numComplet = null;

                if (client.getComptes() != null && !client.getComptes().isEmpty()) {
                        Compte c = client.getComptes().get(0);
                        if (c.getId() != null) {
                                codeGuichet = c.getId().getCodeGuichet();
                                codeProduit = c.getId().getCodeProduit();
                                numCompte = c.getId().getNumCompte();
                                numComplet = String.format("%s-%s-%s", codeGuichet, codeProduit, numCompte);
                        }
                }

                return SollicitationResponse.builder()
                                .id(null)
                                .clientId(client.getId())
                                .clientIdentifiant(client.getIdentifiant())
                                .clientRelation(client.getRelation())
                                .clientGroupe(client.getGroupe())
                                .dateDemande(LocalDate.now())
                                .compteCodeGuichet(codeGuichet)
                                .compteCodeProduit(codeProduit)
                                .compteNumCompte(numCompte)
                                .compteNumeroComplet(numComplet)
                                .produit(null)
                                .objetCredit(null)
                                .montantSollicite(BigDecimal.ZERO)
                                .dureeSolliciteeMois(0)
                                .periodicitePl("Trimestrielle")
                                .nbrePeriodesSolliciteesPl(0)
                                .nbrePeriodesGracePl(0)
                                .periodiciteInt("Trimestrielle")
                                .nbrePeriodesSolliciteesInt(0)
                                .nbrePeriodesGraceInt(0)
                                .typeTauxSollicite("Taux Variable")
                                .marge(BigDecimal.ZERO)
                                .tauxSollicite(BigDecimal.ZERO)
                                .typeCommission("Forfaitaire")
                                .commissionForfaitaireSollicitee(BigDecimal.ZERO)
                                .commentaireDr("")
                                .apercuProjet("")
                                .etudeMarche("")
                                .syntheseRentabilite("")
                                .swotForces("")
                                .swotFaiblesses("")
                                .swotOpportunites("")
                                .swotMenaces("")
                                .commentaireEtudeProjet("")
                                .totalInvestissementSollicite(BigDecimal.ZERO)
                                .totalFinancementSollicite(BigDecimal.ZERO)
                                .postesInvestissement(new ArrayList<>())
                                .postesFinancement(new ArrayList<>())
                                .nouvellesGaranties(new ArrayList<>())
                                .build();
        }

        /**
         * Récupère la liste de toutes les sollicitations enregistrées (excluant
         * BROUILLON si nécessaire).
         */
        @Transactional(readOnly = true)
        public List<SollicitationResponse> getAllSollicitations() {
                return sollicitationRepository.findAll().stream()
                                .filter(s -> s.getStatut() != null && s.getStatut() != StatutSollicitation.BROUILLON)
                                .map(this::mapToResponse)
                                .collect(Collectors.toList());
        }

        /**
         * Permet aux rôles habilités (notamment ROLE_PRISE_EN_CHARGE) de modifier le
         * statut d'une sollicitation.
         *
         * @param clientId            ID du client propriétaire
         * @param nouveauStatutStr    Nom du statut ("EN_ATTENTE_DECISION", "ACCEPTE",
         *                            "REFUSE")
         * @param commentaireDecision Commentaire explicatif
         * @return SollicitationResponse mise à jour
         */
        @Transactional
        public SollicitationResponse updateStatut(Long clientId, String nouveauStatutStr, String commentaireDecision) {
                Sollicitation sollicitation = sollicitationRepository.findFirstByClientIdOrderByIdDesc(clientId)
                                .orElseThrow(() -> new ResourceNotFoundException("Sollicitation", "clientId",
                                                clientId));

                StatutSollicitation nouveauStatut = StatutSollicitation.valueOf(nouveauStatutStr);
                sollicitation.setStatut(nouveauStatut);

                if (nouveauStatut == StatutSollicitation.ACCEPTE || nouveauStatut == StatutSollicitation.REFUSE) {
                        sollicitation.setDecision(nouveauStatut.name());
                        sollicitation.setDateDecision(LocalDate.now());
                }

                if (commentaireDecision != null && !commentaireDecision.isBlank()) {
                        sollicitation.setCommentaireDecision(commentaireDecision);
                }

                log.info("Mise à jour du statut par Prise en Charge pour client {}: statut={}", clientId,
                                nouveauStatut);

                Sollicitation saved = sollicitationRepository.save(sollicitation);
                return mapToResponse(saved);
        }
}
