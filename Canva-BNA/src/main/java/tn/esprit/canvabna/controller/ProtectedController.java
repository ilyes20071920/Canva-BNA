package tn.esprit.canvabna.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Demonstrates role-protected endpoints for each of the three roles.
 * Replace the stubs with your actual domain logic.
 */
@RestController
public class ProtectedController {

    /** GET /api/admin/dashboard — ROLE_ADMIN only */
    @GetMapping("/api/admin/dashboard")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Map<String, String>> adminDashboard() {
        return ResponseEntity.ok(Map.of(
                "message", "Admin dashboard — welcome!",
                "role", "ROLE_ADMIN"
        ));
    }

    /** GET /api/prise-en-charge/cases — ROLE_PRISE_EN_CHARGE only */
    @GetMapping("/api/prise-en-charge/cases")
    @PreAuthorize("hasAuthority('ROLE_PRISE_EN_CHARGE')")
    public ResponseEntity<Map<String, String>> priseEnChargeCases() {
        return ResponseEntity.ok(Map.of(
                "message", "Prise en charge — case list",
                "role", "ROLE_PRISE_EN_CHARGE"
        ));
    }

    /** GET /api/chef-division/reports — ROLE_CHEF_DIVISION only */
    @GetMapping("/api/chef-division/reports")
    @PreAuthorize("hasAuthority('ROLE_CHEF_DIVISION')")
    public ResponseEntity<Map<String, String>> chefDivisionReports() {
        return ResponseEntity.ok(Map.of(
                "message", "Chef de division — reports",
                "role", "ROLE_CHEF_DIVISION"
        ));
    }
}
