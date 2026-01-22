package com.ega.egabank.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de réponse pour l'authentification
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;
    private String username;
    private String email;
    private String role;
    private Long clientId;

    public static AuthResponse of(String accessToken, String refreshToken, Long expiresIn,
            String username, String email, String role, Long clientId) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .username(username)
                .email(email)
                .role(role)
                .clientId(clientId)
                .build();
    }
}
