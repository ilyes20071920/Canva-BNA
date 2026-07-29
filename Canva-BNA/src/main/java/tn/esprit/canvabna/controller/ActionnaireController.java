package tn.esprit.canvabna.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.canvabna.dto.ActionnaireRequest;
import tn.esprit.canvabna.dto.ActionnaireResponse;
import tn.esprit.canvabna.service.ActionnaireService;

import java.util.List;

/**
 * REST controller pour la gestion des actionnaires/associés du capital social.
 * Base path: {@code /api/actionnaires}
 */
@Slf4j
@RestController
@RequestMapping("/api/actionnaires")
@RequiredArgsConstructor
public class ActionnaireController {

    private final ActionnaireService actionnaireService;

    /**
     * GET /api/actionnaires/client/{clientId}
     * Retourne tous les actionnaires d'un client donné.
     */
    @GetMapping("/client/{clientId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ActionnaireResponse>> getByClientId(@PathVariable final Long clientId) {
        return ResponseEntity.ok(actionnaireService.findByClientId(clientId));
    }

    /**
     * GET /api/actionnaires/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ActionnaireResponse> getById(@PathVariable final Long id) {
        return ResponseEntity.ok(actionnaireService.findById(id));
    }

    /**
     * POST /api/actionnaires
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_CHEF_DIVISION')")
    public ResponseEntity<ActionnaireResponse> create(@Valid @RequestBody final ActionnaireRequest request) {
        log.info("POST /api/actionnaires — nom: {}, clientId: {}", request.getNom(), request.getClientId());
        return ResponseEntity.status(HttpStatus.CREATED).body(actionnaireService.create(request));
    }

    /**
     * PUT /api/actionnaires/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_CHEF_DIVISION')")
    public ResponseEntity<ActionnaireResponse> update(
            @PathVariable final Long id,
            @Valid @RequestBody final ActionnaireRequest request) {
        return ResponseEntity.ok(actionnaireService.update(id, request));
    }

    /**
     * DELETE /api/actionnaires/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable final Long id) {
        actionnaireService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
