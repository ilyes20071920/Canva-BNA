package tn.esprit.canvabna.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.canvabna.dto.StructureRequest;
import tn.esprit.canvabna.dto.StructureResponse;
import tn.esprit.canvabna.service.StructureService;

import java.util.List;

/**
 * REST controller pour la gestion des structures organisationnelles.
 * Base path: {@code /api/structures}
 */
@Slf4j
@RestController
@RequestMapping("/api/structures")
@RequiredArgsConstructor
public class StructureController {

    private final StructureService structureService;

    /**
     * GET /api/structures
     * Retourne toutes les structures. Réservé aux admins et chefs de division.
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_CHEF_DIVISION')")
    public ResponseEntity<List<StructureResponse>> getAll() {
        return ResponseEntity.ok(structureService.findAll());
    }

    /**
     * GET /api/structures/{id}
     * Retourne une structure par ID. Tout utilisateur authentifié.
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<StructureResponse> getById(@PathVariable final Long id) {
        return ResponseEntity.ok(structureService.findById(id));
    }

    /**
     * POST /api/structures
     * Crée une nouvelle structure. Réservé à ROLE_ADMIN.
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<StructureResponse> create(@Valid @RequestBody final StructureRequest request) {
        log.info("POST /api/structures — code: {}", request.getCode());
        return ResponseEntity.status(HttpStatus.CREATED).body(structureService.create(request));
    }

    /**
     * PUT /api/structures/{id}
     * Met à jour une structure. Réservé à ROLE_ADMIN.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<StructureResponse> update(
            @PathVariable final Long id,
            @Valid @RequestBody final StructureRequest request) {
        return ResponseEntity.ok(structureService.update(id, request));
    }

    /**
     * DELETE /api/structures/{id}
     * Supprime une structure. Réservé à ROLE_ADMIN.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable final Long id) {
        structureService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
