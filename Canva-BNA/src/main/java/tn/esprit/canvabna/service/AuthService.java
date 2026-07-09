package tn.esprit.canvabna.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.canvabna.dto.LoginRequest;
import tn.esprit.canvabna.dto.LoginResponse;
import tn.esprit.canvabna.dto.RegisterRequest;
import tn.esprit.canvabna.dto.UserResponse;
import tn.esprit.canvabna.entity.User;
import tn.esprit.canvabna.mapper.UserMapper;
import tn.esprit.canvabna.repository.UserRepository;
import tn.esprit.canvabna.security.CustomUserDetails;
import tn.esprit.canvabna.security.JwtService;

/**
 * Authentication service — handles login and user registration.
 *
 * <p>This service is the central orchestrator for the authentication flow:
 * <ol>
 *   <li>Delegates credential verification to Spring Security's {@link AuthenticationManager}</li>
 *   <li>Extracts the authenticated principal as {@link CustomUserDetails}</li>
 *   <li>Generates a JWT via {@link JwtService}</li>
 *   <li>Returns a structured {@link LoginResponse}</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserMapper userMapper;

    /**
     * Authenticates the user and returns a JWT response.
     *
     * @param request validated login DTO
     * @return {@link LoginResponse} containing the JWT and user info
     * @throws org.springframework.security.authentication.BadCredentialsException if credentials are invalid
     * @throws org.springframework.security.authentication.DisabledException        if the account is disabled
     */
    @Transactional(readOnly = true)
    public LoginResponse login(final LoginRequest request) {
        log.info("Login attempt for matricule: {}", request.getMatricule());

        final Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getMatricule().toString(),
                        request.getPassword()
                )
        );

        final CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        final String jwt = jwtService.generateToken(userDetails);
        final User user = userDetails.getUser();

        log.info("Login successful for matricule: {}", user.getMatricule());

        return LoginResponse.builder()
                .token(jwt)
                .type("Bearer")
                .matricule(user.getMatricule())
                .nom(user.getNom())
                .prenom(user.getPrenom())
                .structure(user.getStructure())
                .role(user.getRole().name())
                .build();
    }

    /**
     * Registers a new user (admin-only operation).
     *
     * @param request validated register DTO
     * @return the created user as {@link UserResponse}
     * @throws IllegalArgumentException if the matricule is already taken
     */
    @Transactional
    public UserResponse register(final RegisterRequest request) {
        log.info("Registering new user with matricule: {}", request.getMatricule());

        if (userRepository.existsByMatricule(request.getMatricule())) {
            throw new IllegalArgumentException(
                    "Matricule " + request.getMatricule() + " is already registered");
        }

        final User user = User.builder()
                .matricule(request.getMatricule())
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .password(passwordEncoder.encode(request.getPassword()))
                .structure(request.getStructure())
                .role(request.getRole())
                .enabled(true)
                .build();

        final User saved = userRepository.save(user);
        log.info("User registered successfully with id: {}", saved.getId());

        return userMapper.toResponse(saved);
    }
}
