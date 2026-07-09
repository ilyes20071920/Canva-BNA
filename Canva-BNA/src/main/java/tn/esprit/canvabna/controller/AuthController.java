package tn.esprit.canvabna.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.canvabna.dto.LoginRequest;
import tn.esprit.canvabna.dto.LoginResponse;
import tn.esprit.canvabna.dto.RegisterRequest;
import tn.esprit.canvabna.dto.UserResponse;
import tn.esprit.canvabna.service.AuthService;

/**
 * REST controller for authentication and user registration.
 *
 * <p>Public endpoints ({@code /api/auth/**}) are permitted without a token.
 * Registration is further protected with {@code @PreAuthorize} so only admins can call it.
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * POST /api/auth/login
     * Authenticates the user and returns a JWT.
     *
     * @param request validated login credentials
     * @return 200 OK with {@link LoginResponse}
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody final LoginRequest request) {
        log.info("Login request received for matricule: {}", request.getMatricule());
        final LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/auth/register
     * Creates a new user. Restricted to ROLE_ADMIN callers.
     *
     * @param request validated user creation payload
     * @return 201 CREATED with {@link UserResponse}
     */
    @PostMapping("/register")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody final RegisterRequest request) {
        log.info("Register request received for matricule: {}", request.getMatricule());
        final UserResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
