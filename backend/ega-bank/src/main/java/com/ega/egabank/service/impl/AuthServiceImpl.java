package com.ega.egabank.service.impl;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ega.egabank.dto.request.LoginRequest;
import com.ega.egabank.dto.request.RegisterRequest;
import com.ega.egabank.dto.response.AuthResponse;
import com.ega.egabank.entity.User;
import com.ega.egabank.enums.Role;
import com.ega.egabank.exception.DuplicateResourceException;
import com.ega.egabank.exception.InvalidTokenException;
import com.ega.egabank.exception.ResourceNotFoundException;
import com.ega.egabank.repository.UserRepository;
import com.ega.egabank.security.JwtTokenProvider;
import com.ega.egabank.service.AuthService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implémentation du service d'authentification
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;
    private final com.ega.egabank.repository.ClientRepository clientRepository;

    @Override
    public AuthResponse register(RegisterRequest request) {
        log.info("Inscription d'un nouvel utilisateur: {}", request.getUsername());

        // Vérifier l'unicité du username
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Utilisateur", "username", request.getUsername());
        }

        // Vérifier l'unicité de l'email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Utilisateur", "email", request.getEmail());
        }

        // Créer l'utilisateur
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ROLE_USER)
                .enabled(true)
                .build();

        // Créer l'entité Client associée
        com.ega.egabank.entity.Client client = com.ega.egabank.entity.Client.builder()
                .nom(request.getUsername()) // Utiliser le username comme nom par défaut
                .prenom("")
                .courriel(request.getEmail())
                .telephone("")
                .adresse("")
                .nationalite("")
                .sexe(com.ega.egabank.enums.Sexe.MASCULIN) // Valeur par défaut
                .dateNaissance(java.time.LocalDate.now()) // Valeur par défaut
                .build();

        client = clientRepository.save(client);
        user.setClient(client);

        user = userRepository.save(user);

        // Générer les tokens
        String accessToken = tokenProvider.generateAccessToken(user.getUsername());
        String refreshToken = tokenProvider.generateRefreshToken(user.getUsername());

        log.info("Utilisateur créé avec succès: {}", user.getUsername());

        return AuthResponse.of(
                accessToken,
                refreshToken,
                tokenProvider.getExpirationTime(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().name(),
                user.getClient() != null ? user.getClient().getId() : null);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        log.info("Tentative de connexion pour: {}", request.getUsername());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()));

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Utilisateur", "nom d'utilisateur", request.getUsername()));

        String accessToken = tokenProvider.generateAccessToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(user.getUsername());

        log.info("Connexion réussie pour: {}", request.getUsername());

        return AuthResponse.of(
                accessToken,
                refreshToken,
                tokenProvider.getExpirationTime(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().name(),
                user.getClient() != null ? user.getClient().getId() : null);
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {
        log.info("Rafraîchissement du token");

        if (!tokenProvider.validateToken(refreshToken)) {
            throw new InvalidTokenException("de rafraîchissement", "expiré ou malformé");
        }

        String username = tokenProvider.getUsernameFromToken(refreshToken);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "nom d'utilisateur", username));

        String newAccessToken = tokenProvider.generateAccessToken(username);
        String newRefreshToken = tokenProvider.generateRefreshToken(username);

        log.info("Token rafraîchi avec succès pour: {}", username);

        return AuthResponse.of(
                newAccessToken,
                newRefreshToken,
                tokenProvider.getExpirationTime(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().name(),
                user.getClient() != null ? user.getClient().getId() : null);
    }
}
