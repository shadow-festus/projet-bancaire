package com.ega.egabank.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.ega.egabank.dto.request.LoginRequest;
import com.ega.egabank.dto.request.RegisterRequest;
import com.ega.egabank.dto.response.AuthResponse;
import com.ega.egabank.entity.User;
import com.ega.egabank.enums.Role;
import com.ega.egabank.exception.DuplicateResourceException;
import com.ega.egabank.repository.UserRepository;
import com.ega.egabank.security.JwtTokenProvider;
import com.ega.egabank.service.impl.AuthServiceImpl;

/**
 * Tests unitaires pour AuthService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests du service Auth")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User user;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
                .username("testuser")
                .email("testuser@email.com")
                .password("password123")
                .build();

        loginRequest = LoginRequest.builder()
                .username("testuser")
                .password("password123")
                .build();

        user = User.builder()
                .id(1L)
                .username("testuser")
                .email("testuser@email.com")
                .password("encoded_password")
                .role(Role.ROLE_USER)
                .enabled(true)
                .build();
    }

    @Nested
    @DisplayName("Tests de register")
    class RegisterTests {

        @Test
        @DisplayName("Devrait inscrire un nouvel utilisateur avec succès")
        void shouldRegisterNewUserSuccessfully() {
            // Arrange
            when(userRepository.existsByUsername("testuser")).thenReturn(false);
            when(userRepository.existsByEmail("testuser@email.com")).thenReturn(false);
            when(passwordEncoder.encode("password123")).thenReturn("encoded_password");
            when(userRepository.save(any(User.class))).thenReturn(user);
            when(tokenProvider.generateAccessToken(anyString())).thenReturn("access-token");
            when(tokenProvider.generateRefreshToken(anyString())).thenReturn("refresh-token");
            when(tokenProvider.getExpirationTime()).thenReturn(3600L);

            // Act
            AuthResponse response = authService.register(registerRequest);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getAccessToken()).isEqualTo("access-token");
            assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
            assertThat(response.getUsername()).isEqualTo("testuser");
            assertThat(response.getRole()).isEqualTo("ROLE_USER");
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Devrait lancer une exception si le username existe déjà")
        void shouldThrowExceptionIfUsernameExists() {
            // Arrange
            when(userRepository.existsByUsername("testuser")).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> authService.register(registerRequest))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("username");
        }

        @Test
        @DisplayName("Devrait lancer une exception si l'email existe déjà")
        void shouldThrowExceptionIfEmailExists() {
            // Arrange
            when(userRepository.existsByUsername("testuser")).thenReturn(false);
            when(userRepository.existsByEmail("testuser@email.com")).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> authService.register(registerRequest))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("email");
        }

        @Test
        @DisplayName("Devrait encoder le mot de passe")
        void shouldEncodePassword() {
            // Arrange
            when(userRepository.existsByUsername(anyString())).thenReturn(false);
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(passwordEncoder.encode("password123")).thenReturn("encoded_password");
            when(userRepository.save(any(User.class))).thenReturn(user);
            when(tokenProvider.generateAccessToken(anyString())).thenReturn("token");
            when(tokenProvider.generateRefreshToken(anyString())).thenReturn("refresh");
            when(tokenProvider.getExpirationTime()).thenReturn(3600L);

            // Act
            authService.register(registerRequest);

            // Assert
            verify(passwordEncoder).encode("password123");
        }
    }

    @Nested
    @DisplayName("Tests de login")
    class LoginTests {

        @Test
        @DisplayName("Devrait connecter un utilisateur avec succès")
        void shouldLoginSuccessfully() {
            // Arrange
            Authentication authentication = mock(Authentication.class);
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authentication);
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
            when(tokenProvider.generateAccessToken(any(Authentication.class))).thenReturn("access-token");
            when(tokenProvider.generateRefreshToken(anyString())).thenReturn("refresh-token");
            when(tokenProvider.getExpirationTime()).thenReturn(3600L);

            // Act
            AuthResponse response = authService.login(loginRequest);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getAccessToken()).isEqualTo("access-token");
            assertThat(response.getUsername()).isEqualTo("testuser");
            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        }

        @Test
        @DisplayName("Devrait lancer une exception si les identifiants sont invalides")
        void shouldThrowExceptionIfInvalidCredentials() {
            // Arrange
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            // Act & Assert
            assertThatThrownBy(() -> authService.login(loginRequest))
                    .isInstanceOf(BadCredentialsException.class);
        }

        @Test
        @DisplayName("Devrait lancer une exception si l'utilisateur n'existe pas après authentification")
        void shouldThrowExceptionIfUserNotFoundAfterAuth() {
            // Arrange
            Authentication authentication = mock(Authentication.class);
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authentication);
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> authService.login(loginRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Utilisateur non trouvé");
        }
    }

    @Nested
    @DisplayName("Tests de refreshToken")
    class RefreshTokenTests {

        @Test
        @DisplayName("Devrait rafraîchir le token avec succès")
        void shouldRefreshTokenSuccessfully() {
            // Arrange
            String refreshToken = "valid-refresh-token";
            when(tokenProvider.validateToken(refreshToken)).thenReturn(true);
            when(tokenProvider.getUsernameFromToken(refreshToken)).thenReturn("testuser");
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
            when(tokenProvider.generateAccessToken(anyString())).thenReturn("new-access-token");
            when(tokenProvider.generateRefreshToken(anyString())).thenReturn("new-refresh-token");
            when(tokenProvider.getExpirationTime()).thenReturn(3600L);

            // Act
            AuthResponse response = authService.refreshToken(refreshToken);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getAccessToken()).isEqualTo("new-access-token");
            assertThat(response.getRefreshToken()).isEqualTo("new-refresh-token");
        }

        @Test
        @DisplayName("Devrait lancer une exception si le refresh token est invalide")
        void shouldThrowExceptionIfInvalidRefreshToken() {
            // Arrange
            when(tokenProvider.validateToken("invalid-token")).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> authService.refreshToken("invalid-token"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Token de rafraîchissement invalide");
        }

        @Test
        @DisplayName("Devrait lancer une exception si l'utilisateur n'existe plus")
        void shouldThrowExceptionIfUserNoLongerExists() {
            // Arrange
            String refreshToken = "valid-refresh-token";
            when(tokenProvider.validateToken(refreshToken)).thenReturn(true);
            when(tokenProvider.getUsernameFromToken(refreshToken)).thenReturn("deleteduser");
            when(userRepository.findByUsername("deleteduser")).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> authService.refreshToken(refreshToken))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Utilisateur non trouvé");
        }
    }
}
