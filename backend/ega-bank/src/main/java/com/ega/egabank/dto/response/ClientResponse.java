package com.ega.egabank.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.ega.egabank.enums.Sexe;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de réponse pour un client
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientResponse {

    private Long id;
    private String nom;
    private String prenom;
    private String nomComplet;
    private LocalDate dateNaissance;
    private Sexe sexe;
    private String adresse;
    private String telephone;
    private String courriel;
    private String nationalite;
    private LocalDateTime createdAt;
    private int nombreComptes;
    private List<AccountResponse> comptes;
}
