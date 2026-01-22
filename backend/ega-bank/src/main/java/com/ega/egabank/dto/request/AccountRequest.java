package com.ega.egabank.dto.request;

import com.ega.egabank.enums.TypeCompte;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour la création d'un compte
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountRequest {

    @NotNull(message = "Le type de compte est obligatoire")
    private TypeCompte typeCompte;

    @NotNull(message = "L'identifiant du client est obligatoire")
    private Long clientId;
}
