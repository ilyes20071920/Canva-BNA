package tn.esprit.canvabna.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.canvabna.dto.DecisionRequest;
import tn.esprit.canvabna.dto.SollicitationRequest;
import tn.esprit.canvabna.dto.SollicitationResponse;
import tn.esprit.canvabna.service.SollicitationService;

@Slf4j
@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class SollicitationController {

    private final SollicitationService sollicitationService;

    /** Charge LA PLUS RÉCENTE sollicitation du client pour compatibilité. */
    @GetMapping("/{clientId}/sollicitation")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SollicitationResponse> getSollicitationForClient(@PathVariable Long clientId) {
        log.debug("GET /api/clients/{}/sollicitation", clientId);
        return ResponseEntity.ok(sollicitationService.getSollicitationForClient(clientId));
    }

    /** Charge TOUTES les sollicitations du client. */
    @GetMapping("/{clientId}/sollicitations")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<java.util.List<SollicitationResponse>> getAllSollicitationsForClient(
            @PathVariable Long clientId) {
        log.debug("GET /api/clients/{}/sollicitations", clientId);
        return ResponseEntity.ok(sollicitationService.getAllSollicitationsForClient(clientId));
    }

    /** Crée ou met à jour le canevas de sollicitation (historiquement). */
    @PutMapping("/{clientId}/sollicitation")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SollicitationResponse> saveOrUpdateSollicitation(
            @PathVariable Long clientId,
            @Valid @RequestBody SollicitationRequest request) {
        log.debug("PUT /api/clients/{}/sollicitation", clientId);
        return ResponseEntity.ok(sollicitationService.saveOrUpdateSollicitation(clientId, request));
    }

    /** Crée explicitement UNE NOUVELLE sollicitation pour ce client. */
    @PostMapping("/{clientId}/sollicitation")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SollicitationResponse> createNouvelleSollicitation(
            @PathVariable Long clientId,
            @Valid @RequestBody SollicitationRequest request) {
        log.debug("POST /api/clients/{}/sollicitation (Nouveau)", clientId);
        return ResponseEntity.ok(sollicitationService.createNouvelleSollicitation(clientId, request));
    }

    /**
     * Enregistre la décision finale (ACCEPTE ou REFUSE) sur la sollicitation.
     * Réservé aux rôles ROLE_CHEF_DIVISION et ROLE_ADMIN.
     */
    @PostMapping("/{clientId}/sollicitation/decision")
    @PreAuthorize("hasRole('CHEF_DIVISION') or hasRole('ADMIN')")
    public ResponseEntity<SollicitationResponse> enregistrerDecision(
            @PathVariable Long clientId,
            @Valid @RequestBody DecisionRequest request) {
        log.debug("POST /api/clients/{}/sollicitation/decision — decision={}", clientId, request.getDecision());
        return ResponseEntity.ok(sollicitationService.enregistrerDecision(clientId, request));
    }

    /**
     * Récupère toutes les sollicitations soumises / décidées.
     */
    @GetMapping("/sollicitations/all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<java.util.List<SollicitationResponse>> getAllSollicitations() {
        log.debug("GET /api/clients/sollicitations/all");
        return ResponseEntity.ok(sollicitationService.getAllSollicitations());
    }

    /**
     * Permet la modification du statut d'une sollicitation (notamment pour
     * ROLE_PRISE_EN_CHARGE).
     */
    @PutMapping("/{clientId}/sollicitation/statut")
    @PreAuthorize("hasRole('PRISE_EN_CHARGE') or hasRole('ADMIN') or hasRole('CHEF_DIVISION')")
    public ResponseEntity<SollicitationResponse> updateStatut(
            @PathVariable Long clientId,
            @RequestParam String statut,
            @RequestParam(required = false) String commentaire) {
        log.debug("PUT /api/clients/{}/sollicitation/statut — statut={}", clientId, statut);
        return ResponseEntity.ok(sollicitationService.updateStatut(clientId, statut, commentaire));
    }
}
