package tn.esprit.canvabna.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.canvabna.dto.CompteRequest;
import tn.esprit.canvabna.dto.CompteResponse;
import tn.esprit.canvabna.service.CompteService;

import java.util.List;

/**
 * REST controller pour la gestion des comptes bancaires.
 * Base path: {@code /api/comptes}
 */
@Slf4j
@RestController
@RequestMapping("/api/comptes")
@RequiredArgsConstructor
public class CompteController {

    private final CompteService compteService;

    /**
     * GET /api/comptes/client/{clientId}
     * Retourne tous les comptes d'un client donné.
     */
    @GetMapping("/client/{clientId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CompteResponse>> getByClientId(@PathVariable final Long clientId) {
        return ResponseEntity.ok(compteService.findByClientId(clientId));
    }

    /**
     * GET /api/comptes/{codeGuichet}/{codeProduit}/{numCompte}
     */
    @GetMapping("/{codeGuichet}/{codeProduit}/{numCompte}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CompteResponse> getById(
            @PathVariable final String codeGuichet,
            @PathVariable final String codeProduit,
            @PathVariable final String numCompte) {
        return ResponseEntity.ok(compteService.findById(codeGuichet, codeProduit, numCompte));
    }

    /**
     * POST /api/comptes
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_CHEF_DIVISION', 'ROLE_PRISE_EN_CHARGE')")
    public ResponseEntity<CompteResponse> create(@Valid @RequestBody final CompteRequest request) {
        log.info("POST /api/comptes — codeGuichet: {}, codeProduit: {}, numCompte: {}, clientId: {}", 
                request.getCodeGuichet(), request.getCodeProduit(), request.getNumCompte(), request.getClientId());
        return ResponseEntity.status(HttpStatus.CREATED).body(compteService.create(request));
    }

    /**
     * PUT /api/comptes/{codeGuichet}/{codeProduit}/{numCompte}
     */
    @PutMapping("/{codeGuichet}/{codeProduit}/{numCompte}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_CHEF_DIVISION')")
    public ResponseEntity<CompteResponse> update(
            @PathVariable final String codeGuichet,
            @PathVariable final String codeProduit,
            @PathVariable final String numCompte,
            @Valid @RequestBody final CompteRequest request) {
        return ResponseEntity.ok(compteService.update(codeGuichet, codeProduit, numCompte, request));
    }

    /**
     * DELETE /api/comptes/{codeGuichet}/{codeProduit}/{numCompte}
     */
    @DeleteMapping("/{codeGuichet}/{codeProduit}/{numCompte}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> delete(
            @PathVariable final String codeGuichet,
            @PathVariable final String codeProduit,
            @PathVariable final String numCompte) {
        compteService.delete(codeGuichet, codeProduit, numCompte);
        return ResponseEntity.noContent().build();
    }
}
