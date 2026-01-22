package com.ega.egabank.dto.request;

import java.time.LocalDate;

import com.ega.egabank.enums.Sexe;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour la création/modification d'un client
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientRequest {

    @NotBlank(message = "Le nom est obligatoire")
    @Size(min = 2, max = 50, message = "Le nom doit contenir entre 2 et 50 caractères")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(min = 2, max = 50, message = "Le prénom doit contenir entre 2 et 50 caractères")
    private String prenom;

    @NotNull(message = "La date de naissance est obligatoire")
    @Past(message = "La date de naissance doit être dans le passé")
    private LocalDate dateNaissance;

    @NotNull(message = "Le sexe est obligatoire")
    private Sexe sexe;

    @Size(max = 200, message = "L'adresse ne doit pas dépasser 200 caractères")
    private String adresse;

    @Pattern(regexp = "^\\+?[0-9]{8,15}$", message = "Le numéro de téléphone n'est pas valide")
    private String telephone;

    @Email(message = "Le courriel n'est pas valide")
    private String courriel;

    @Size(max = 50, message = "La nationalité ne doit pas dépasser 50 caractères")
    private String nationalite;
}
