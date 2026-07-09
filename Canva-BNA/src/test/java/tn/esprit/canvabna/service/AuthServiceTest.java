package tn.esprit.canvabna.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import tn.esprit.canvabna.dto.LoginRequest;
import tn.esprit.canvabna.dto.LoginResponse;
import tn.esprit.canvabna.dto.RegisterRequest;
import tn.esprit.canvabna.dto.UserResponse;
import tn.esprit.canvabna.entity.Role;
import tn.esprit.canvabna.entity.User;
import tn.esprit.canvabna.mapper.UserMapper;
import tn.esprit.canvabna.repository.UserRepository;
import tn.esprit.canvabna.security.CustomUserDetails;
import tn.esprit.canvabna.security.JwtService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AuthService}.
 * All dependencies are mocked — no Spring context needed.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AuthService authService;

    private User sampleUser;
    private CustomUserDetails sampleUserDetails;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(1L)
                .matricule(12345)
                .password("$2a$10$encodedPassword")
                .structure("Direction Générale")
                .role(Role.ROLE_ADMIN)
                .enabled(true)
                .build();

        sampleUserDetails = new CustomUserDetails(sampleUser);
    }

    // ---------------------------------------------------------------- Login tests

    @Test
    @DisplayName("login — should return LoginResponse on valid credentials")
    void login_shouldReturnLoginResponse_whenCredentialsAreValid() {
        // Arrange
        final LoginRequest request = new LoginRequest();
        request.setMatricule(12345);
        request.setPassword("password");

        final UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(sampleUserDetails, null, sampleUserDetails.getAuthorities());

        when(authenticationManager.authenticate(any())).thenReturn(authToken);
        when(jwtService.generateToken(any(CustomUserDetails.class))).thenReturn("mock.jwt.token");

        // Act
        final LoginResponse response = authService.login(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("mock.jwt.token");
        assertThat(response.getType()).isEqualTo("Bearer");
        assertThat(response.getMatricule()).isEqualTo(12345);
        assertThat(response.getRole()).isEqualTo("ROLE_ADMIN");
        assertThat(response.getStructure()).isEqualTo("Direction Générale");

        verify(authenticationManager).authenticate(any());
        verify(jwtService).generateToken(any(CustomUserDetails.class));
    }

    @Test
    @DisplayName("login — should throw BadCredentialsException on invalid credentials")
    void login_shouldThrowBadCredentialsException_whenCredentialsAreInvalid() {
        // Arrange
        final LoginRequest request = new LoginRequest();
        request.setMatricule(12345);
        request.setPassword("wrong");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // Act & Assert
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Invalid credentials");

        verifyNoInteractions(jwtService);
    }

    // ---------------------------------------------------------------- Register tests

    @Test
    @DisplayName("register — should return UserResponse on successful registration")
    void register_shouldReturnUserResponse_whenMatriculeIsUnique() {
        // Arrange
        final RegisterRequest request = new RegisterRequest();
        request.setMatricule(99999);
        request.setPassword("secret");
        request.setStructure("Dept IT");
        request.setRole(Role.ROLE_CHEF_DIVISION);

        when(userRepository.existsByMatricule(99999)).thenReturn(false);
        when(passwordEncoder.encode("secret")).thenReturn("$2a$10$encodedSecret");
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        final UserResponse expectedResponse = UserResponse.builder()
                .id(1L).matricule(12345).structure("Dir").role("ROLE_ADMIN").enabled(true).build();
        when(userMapper.toResponse(sampleUser)).thenReturn(expectedResponse);

        // Act
        final UserResponse response = authService.register(request);

        // Assert
        assertThat(response).isNotNull();
        verify(passwordEncoder).encode("secret");
        verify(userRepository).save(argThat(u -> !u.getPassword().equals("secret"))); // never plain text
    }

    @Test
    @DisplayName("register — should throw IllegalArgumentException when matricule already exists")
    void register_shouldThrowIllegalArgumentException_whenMatriculeIsDuplicate() {
        // Arrange
        final RegisterRequest request = new RegisterRequest();
        request.setMatricule(12345);
        request.setPassword("secret");
        request.setStructure("Dept");
        request.setRole(Role.ROLE_PRISE_EN_CHARGE);

        when(userRepository.existsByMatricule(12345)).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("12345");

        verify(userRepository, never()).save(any());
    }
}
