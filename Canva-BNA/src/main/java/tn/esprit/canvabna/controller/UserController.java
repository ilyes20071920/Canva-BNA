package tn.esprit.canvabna.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import tn.esprit.canvabna.dto.RegisterRequest;
import tn.esprit.canvabna.dto.UserResponse;
import tn.esprit.canvabna.security.CustomUserDetails;
import tn.esprit.canvabna.service.AuthService;
import tn.esprit.canvabna.service.UserService;

import java.util.List;

/**
 * Protected user management endpoints.
 * Demonstrates role-based method-level access control via {@code @PreAuthorize}.
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    /**
     * GET /api/users/me
     * Returns the profile of the currently authenticated user.
     * Any authenticated user can call this endpoint.
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMe(
            @AuthenticationPrincipal final CustomUserDetails currentUser) {
        return ResponseEntity.ok(userService.findByMatricule(currentUser.getUser().getMatricule()));
    }

    /**
     * POST /api/users
     * Creates a new user. ROLE_ADMIN only.
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<UserResponse> createUser(
            @jakarta.validation.Valid @RequestBody final RegisterRequest request) {
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED)
                .body(authService.register(request));
    }

    /**
     * GET /api/users
     * Returns all users. ROLE_ADMIN only.
     */
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.findAll());
    }

    /**
     * GET /api/users/{matricule}
     * Returns a specific user. ROLE_ADMIN only.
     */
    @GetMapping("/{matricule}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<UserResponse> getUserByMatricule(
            @PathVariable final Integer matricule) {
        return ResponseEntity.ok(userService.findByMatricule(matricule));
    }

    /**
     * PATCH /api/users/{matricule}/enable
     * Enables a user account. ROLE_ADMIN only.
     */
    @PatchMapping("/{matricule}/enable")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<UserResponse> enableUser(@PathVariable final Integer matricule) {
        return ResponseEntity.ok(userService.setEnabled(matricule, true));
    }

    /**
     * PATCH /api/users/{matricule}/disable
     * Disables a user account. ROLE_ADMIN only.
     */
    @PatchMapping("/{matricule}/disable")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<UserResponse> disableUser(@PathVariable final Integer matricule) {
        return ResponseEntity.ok(userService.setEnabled(matricule, false));
    }

    /**
     * PUT /api/users/{matricule}
     * Updates a user's structure and role. ROLE_ADMIN only.
     */
    @PutMapping("/{matricule}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable final Integer matricule,
            @jakarta.validation.Valid @RequestBody final tn.esprit.canvabna.dto.UpdateRequest request) {
        return ResponseEntity.ok(userService.update(matricule, request));
    }

    /**
     * DELETE /api/users/{matricule}
     * Deletes a user. ROLE_ADMIN only.
     */
    @DeleteMapping("/{matricule}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable final Integer matricule) {
        userService.delete(matricule);
        return ResponseEntity.noContent().build();
    }
}
