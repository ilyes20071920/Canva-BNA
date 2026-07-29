package tn.esprit.canvabna.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.canvabna.dto.MandataireRequest;
import tn.esprit.canvabna.dto.MandataireResponse;
import tn.esprit.canvabna.service.MandataireService;

import java.util.List;

/**
 * REST controller pour la gestion des mandataires bancaires.
 * Base path: {@code /api/mandataires}
 */
@Slf4j
@RestController
@RequestMapping("/api/mandataires")
@RequiredArgsConstructor
public class MandataireController {

    private final MandataireService mandataireService;

    /**
     * GET /api/mandataires/compte/{codeGuichet}/{codeProduit}/{numCompte}
     * Retourne tous les mandataires d'un compte donné.
     */
    @GetMapping("/compte/{codeGuichet}/{codeProduit}/{numCompte}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<MandataireResponse>> getByCompte(
            @PathVariable final String codeGuichet,
            @PathVariable final String codeProduit,
            @PathVariable final String numCompte) {
        return ResponseEntity.ok(mandataireService.findByCompte(codeGuichet, codeProduit, numCompte));
    }

    /**
     * GET /api/mandataires/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MandataireResponse> getById(@PathVariable final Long id) {
        return ResponseEntity.ok(mandataireService.findById(id));
    }

    /**
     * POST /api/mandataires
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_CHEF_DIVISION', 'ROLE_PRISE_EN_CHARGE')")
    public ResponseEntity<MandataireResponse> create(@Valid @RequestBody final MandataireRequest request) {
        log.info("POST /api/mandataires — mandant: {}, compte: {}-{}-{}", 
                request.getMandant(), request.getCompteCodeGuichet(), request.getCompteCodeProduit(), request.getCompteNumCompte());
        return ResponseEntity.status(HttpStatus.CREATED).body(mandataireService.create(request));
    }

    /**
     * PUT /api/mandataires/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_CHEF_DIVISION')")
    public ResponseEntity<MandataireResponse> update(
            @PathVariable final Long id,
            @Valid @RequestBody final MandataireRequest request) {
        return ResponseEntity.ok(mandataireService.update(id, request));
    }

    /**
     * DELETE /api/mandataires/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable final Long id) {
        mandataireService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
