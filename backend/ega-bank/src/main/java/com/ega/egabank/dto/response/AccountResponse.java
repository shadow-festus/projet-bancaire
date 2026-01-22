package com.ega.egabank.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.ega.egabank.enums.TypeCompte;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de réponse pour un compte
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponse {

    private Long id;
    private String numeroCompte;
    private TypeCompte typeCompte;
    private String typeCompteLibelle;
    private LocalDateTime dateCreation;
    private BigDecimal solde;
    private Boolean actif;
    private Long clientId;
    private String clientNomComplet;
}
