package tn.esprit.canvabna.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.canvabna.dto.ReferenceItemDto;
import tn.esprit.canvabna.service.ReferenceDataService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/reference")
@RequiredArgsConstructor
public class ReferenceDataController {

    private final ReferenceDataService referenceDataService;

    @GetMapping("/produits")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ReferenceItemDto>> getProduits() {
        log.debug("GET /api/reference/produits");
        return ResponseEntity.ok(referenceDataService.getAllProduits());
    }

    @GetMapping("/objets-credit")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ReferenceItemDto>> getObjetsCredit() {
        log.debug("GET /api/reference/objets-credit");
        return ResponseEntity.ok(referenceDataService.getAllObjetsCredit());
    }

    @GetMapping("/periodicites")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<String>> getPeriodicites() {
        return ResponseEntity.ok(referenceDataService.getPeriodicites());
    }

    @GetMapping("/types-taux")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<String>> getTypesTaux() {
        return ResponseEntity.ok(referenceDataService.getTypesTaux());
    }

    @GetMapping("/types-commission")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<String>> getTypesCommission() {
        return ResponseEntity.ok(referenceDataService.getTypesCommission());
    }
}
