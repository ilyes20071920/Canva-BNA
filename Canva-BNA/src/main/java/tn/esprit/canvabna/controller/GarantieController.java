package tn.esprit.canvabna.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.canvabna.dto.GarantiesSummaryResponse;
import tn.esprit.canvabna.service.GarantieService;

/**
 * REST controller pour les garanties en possession d'un client.
 * Base path: {@code /api/clients/{clientId}/garanties}
 *
 * <p>Contrôle d'accès : tout utilisateur authentifié peut consulter les garanties
 * (même règle que les engagements et les conditions banque).
 */
@Slf4j
@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class GarantieController {

    private final GarantieService garantieService;

    /**
     * GET /api/clients/{clientId}/garanties
     *
     * <p>Retourne la liste des garanties du client avec les totaux calculés dynamiquement :
     * <ul>
     *   <li>Total Charges Inscrites — somme de toutes les charges</li>
     *   <li>Dont BNA — somme des charges dont le bénéficiaire est BANQUE NATIONALE AGRICOLE
     *       (hypothèse de développement temporaire)</li>
     * </ul>
     *
     * @param clientId l'identifiant JPA du client
     * @return {@link GarantiesSummaryResponse} avec liste et totaux
     */
    @GetMapping("/{clientId}/garanties")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<GarantiesSummaryResponse> getGaranties(
            @PathVariable final Long clientId) {
        log.debug("GET /api/clients/{}/garanties", clientId);
        return ResponseEntity.ok(garantieService.getGarantiesForClient(clientId));
    }

    @PostMapping("/{clientId}/garanties")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<tn.esprit.canvabna.dto.GarantieResponse> addGarantie(
            @PathVariable final Long clientId,
            @jakarta.validation.Valid @RequestBody final tn.esprit.canvabna.dto.GarantieRequest request) {
        log.info("POST /api/clients/{}/garanties", clientId);
        return ResponseEntity.ok(garantieService.addGarantieForClient(clientId, request));
    }

    @DeleteMapping("/{clientId}/garanties/{garantieId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteGarantie(
            @PathVariable final Long clientId,
            @PathVariable final Long garantieId) {
        log.info("DELETE /api/clients/{}/garanties/{}", clientId, garantieId);
        garantieService.deleteGarantie(clientId, garantieId);
        return ResponseEntity.noContent().build();
    }
}
