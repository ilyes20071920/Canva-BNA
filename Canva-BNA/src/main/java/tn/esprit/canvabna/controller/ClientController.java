package tn.esprit.canvabna.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.canvabna.dto.ClientRequest;
import tn.esprit.canvabna.dto.ClientResponse;
import tn.esprit.canvabna.service.ClientService;

import java.util.List;

/**
 * REST controller pour la gestion de la fiche client entreprise (signalétique).
 * Base path: {@code /api/clients}
 *
 * <p>Contrôle d'accès par rôle via {@code @PreAuthorize} :
 * <ul>
 *   <li>Lecture globale : ADMIN et CHEF_DIVISION</li>
 *   <li>Lecture unitaire : tout utilisateur authentifié</li>
 *   <li>Création/Modification : ADMIN et CHEF_DIVISION</li>
 *   <li>Suppression : ADMIN uniquement</li>
 * </ul>
 */
@Slf4j
@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    /**
     * GET /api/clients
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_CHEF_DIVISION')")
    public ResponseEntity<List<ClientResponse>> getAll() {
        return ResponseEntity.ok(clientService.findAll());
    }

    /**
     * GET /api/clients/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ClientResponse> getById(@PathVariable final Long id) {
        return ResponseEntity.ok(clientService.findById(id));
    }

    /**
     * GET /api/clients/identifiant/{identifiant}
     * Recherche par identifiant unique (ex: "1042986R").
     */
    @GetMapping("/identifiant/{identifiant}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ClientResponse> getByIdentifiant(@PathVariable final String identifiant) {
        return ResponseEntity.ok(clientService.findByIdentifiant(identifiant));
    }

    /**
     * GET /api/clients/{clientId}/engagements-activite/comptes-depots
     */
    @GetMapping("/{clientId}/engagements-activite/comptes-depots")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<tn.esprit.canvabna.dto.EngagementsActiviteResponse> getEngagementsActivite(
            @PathVariable final Long clientId) {
        return ResponseEntity.ok(clientService.getEngagementsActivite(clientId));
    }

    /**
     * GET /api/clients/{clientId}/engagements-activite/activites
     */
    @GetMapping("/{clientId}/engagements-activite/activites")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<tn.esprit.canvabna.dto.ActiviteResponse> getActivite(
            @PathVariable final Long clientId) {
        return ResponseEntity.ok(clientService.getActivite(clientId));
    }

    /**
     * POST /api/clients
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_CHEF_DIVISION')")
    public ResponseEntity<ClientResponse> create(@Valid @RequestBody final ClientRequest request) {
        log.info("POST /api/clients — identifiant: {}", request.getIdentifiant());
        return ResponseEntity.status(HttpStatus.CREATED).body(clientService.create(request));
    }

    /**
     * PUT /api/clients/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_CHEF_DIVISION')")
    public ResponseEntity<ClientResponse> update(
            @PathVariable final Long id,
            @Valid @RequestBody final ClientRequest request) {
        return ResponseEntity.ok(clientService.update(id, request));
    }

    /**
     * DELETE /api/clients/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable final Long id) {
        clientService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
