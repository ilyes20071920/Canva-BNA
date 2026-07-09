package tn.esprit.canvabna.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;
import tn.esprit.canvabna.entity.Role;
import tn.esprit.canvabna.entity.User;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link JwtService}.
 * Uses ReflectionTestUtils to inject @Value properties without a Spring context.
 */
class JwtServiceTest {

    // 512-bit Base64 key for tests
    private static final String TEST_SECRET =
            "7A25432A462D4A614E645267556B58703272357538782F413F442847285B6B6170645367566B59703373367638792F423F4528482B4D6251655468576D5A713474";
    private static final long TEST_EXPIRATION_MS = 86_400_000L; // 24 h

    private JwtService jwtService;
    private CustomUserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", TEST_SECRET);
        ReflectionTestUtils.setField(jwtService, "expirationMs", TEST_EXPIRATION_MS);

        final User user = User.builder()
                .id(1L)
                .matricule(12345)
                .password("$2a$10$dummyhash")
                .structure("Direction Générale")
                .role(Role.ROLE_ADMIN)
                .enabled(true)
                .build();

        userDetails = new CustomUserDetails(user);
    }

    @Test
    @DisplayName("generateToken — should return a non-null, non-blank JWT string")
    void generateToken_shouldReturnNonBlankToken() {
        final String token = jwtService.generateToken(userDetails);
        assertThat(token).isNotBlank();
    }

    @Test
    @DisplayName("extractUsername — should return matricule as string (subject)")
    void extractUsername_shouldReturnMatriculeAsString() {
        final String token = jwtService.generateToken(userDetails);
        assertThat(jwtService.extractUsername(token)).isEqualTo("12345");
    }

    @Test
    @DisplayName("isTokenValid — should return true for a freshly generated token")
    void isTokenValid_shouldReturnTrueForFreshToken() {
        final String token = jwtService.generateToken(userDetails);
        assertThat(jwtService.isTokenValid(token, userDetails)).isTrue();
    }

    @Test
    @DisplayName("isTokenValid — should return false when username does not match")
    void isTokenValid_shouldReturnFalseForWrongUser() {
        final User otherUser = User.builder()
                .id(2L)
                .matricule(99999)
                .password("$2a$10$anotherhash")
                .structure("Other")
                .role(Role.ROLE_CHEF_DIVISION)
                .enabled(true)
                .build();
        final CustomUserDetails otherDetails = new CustomUserDetails(otherUser);

        final String tokenForFirstUser = jwtService.generateToken(userDetails);
        assertThat(jwtService.isTokenValid(tokenForFirstUser, otherDetails)).isFalse();
    }

    @Test
    @DisplayName("isTokenStructurallyValid — should return true for a valid token")
    void isTokenStructurallyValid_shouldReturnTrueForValidToken() {
        final String token = jwtService.generateToken(userDetails);
        assertThat(jwtService.isTokenStructurallyValid(token)).isTrue();
    }

    @Test
    @DisplayName("isTokenStructurallyValid — should return false for a tampered token")
    void isTokenStructurallyValid_shouldReturnFalseForTamperedToken() {
        final String token = jwtService.generateToken(userDetails);
        final String tampered = token.substring(0, token.length() - 5) + "XXXXX";
        assertThat(jwtService.isTokenStructurallyValid(tampered)).isFalse();
    }

    @Test
    @DisplayName("isTokenStructurallyValid — should return false for an expired token")
    void isTokenStructurallyValid_shouldReturnFalseForExpiredToken() {
        // Overwrite expiration with -1 ms so the token is born expired
        ReflectionTestUtils.setField(jwtService, "expirationMs", -1L);
        final String expiredToken = jwtService.generateToken(userDetails);
        assertThat(jwtService.isTokenStructurallyValid(expiredToken)).isFalse();
    }
}
