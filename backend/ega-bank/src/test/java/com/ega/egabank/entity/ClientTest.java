package com.ega.egabank.entity;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.ega.egabank.enums.Sexe;

/**
 * Tests unitaires pour l'entité Client
 */
@DisplayName("Tests de l'entité Client")
class ClientTest {

    private Client client;

    @BeforeEach
    void setUp() {
        client = Client.builder()
                .id(1L)
                .nom("Dupont")
                .prenom("Jean")
                .dateNaissance(LocalDate.of(1990, 5, 15))
                .sexe(Sexe.MASCULIN)
                .adresse("123 Rue de la Paix, Lomé")
                .telephone("+22890123456")
                .courriel("jean.dupont@email.com")
                .nationalite("Togolaise")
                .comptes(new ArrayList<>())
                .build();
    }

    @Nested
    @DisplayName("Tests de getNomComplet")
    class GetNomCompletTests {

        @Test
        @DisplayName("Devrait retourner le nom complet (prénom + nom)")
        void shouldReturnFullName() {
            // Act
            String nomComplet = client.getNomComplet();

            // Assert
            assertThat(nomComplet).isEqualTo("Jean Dupont");
        }

        @Test
        @DisplayName("Devrait retourner uniquement le nom si prénom est null")
        void shouldReturnOnlyNomIfPrenomNull() {
            // Arrange
            client.setPrenom(null);

            // Act
            String nomComplet = client.getNomComplet();

            // Assert
            assertThat(nomComplet).isEqualTo("Dupont");
        }

        @Test
        @DisplayName("Devrait retourner uniquement le prénom si nom est null")
        void shouldReturnOnlyPrenomIfNomNull() {
            // Arrange
            client.setNom(null);

            // Act
            String nomComplet = client.getNomComplet();

            // Assert
            assertThat(nomComplet).isEqualTo("Jean");
        }

        @Test
        @DisplayName("Devrait retourner chaîne vide si nom et prénom sont null")
        void shouldReturnEmptyIfBothNull() {
            // Arrange
            client.setNom(null);
            client.setPrenom(null);

            // Act
            String nomComplet = client.getNomComplet();

            // Assert
            assertThat(nomComplet).isEmpty();
        }

        @Test
        @DisplayName("Devrait gérer les espaces dans le nom et prénom")
        void shouldHandleSpacesInNameAndSurname() {
            // Arrange
            client.setNom("  Dupont  ");
            client.setPrenom("  Jean  ");

            // Act
            String nomComplet = client.getNomComplet();

            // Assert
            assertThat(nomComplet).isEqualTo("Jean Dupont");
        }

        @Test
        @DisplayName("Devrait gérer les chaînes vides")
        void shouldHandleEmptyStrings() {
            // Arrange
            client.setNom("");
            client.setPrenom("");

            // Act
            String nomComplet = client.getNomComplet();

            // Assert
            assertThat(nomComplet).isEmpty();
        }
    }

    @Nested
    @DisplayName("Tests du Builder")
    class BuilderTests {

        @Test
        @DisplayName("Devrait créer un client avec les valeurs par défaut")
        void shouldCreateClientWithDefaults() {
            // Act
            Client newClient = Client.builder()
                    .nom("Test")
                    .prenom("User")
                    .build();

            // Assert
            assertThat(newClient.getComptes()).isEmpty();
        }

        @Test
        @DisplayName("Devrait créer un client avec toutes les valeurs")
        void shouldCreateClientWithAllValues() {
            // Arrange
            LocalDate dateNaissance = LocalDate.of(1985, 3, 20);

            // Act
            Client newClient = Client.builder()
                    .id(99L)
                    .nom("Martin")
                    .prenom("Pierre")
                    .dateNaissance(dateNaissance)
                    .sexe(Sexe.MASCULIN)
                    .adresse("456 Avenue de la Liberté")
                    .telephone("+22899999999")
                    .courriel("pierre.martin@email.com")
                    .nationalite("Française")
                    .build();

            // Assert
            assertThat(newClient.getId()).isEqualTo(99L);
            assertThat(newClient.getNom()).isEqualTo("Martin");
            assertThat(newClient.getPrenom()).isEqualTo("Pierre");
            assertThat(newClient.getDateNaissance()).isEqualTo(dateNaissance);
            assertThat(newClient.getSexe()).isEqualTo(Sexe.MASCULIN);
            assertThat(newClient.getAdresse()).isEqualTo("456 Avenue de la Liberté");
            assertThat(newClient.getTelephone()).isEqualTo("+22899999999");
            assertThat(newClient.getCourriel()).isEqualTo("pierre.martin@email.com");
            assertThat(newClient.getNationalite()).isEqualTo("Française");
        }
    }

    @Nested
    @DisplayName("Tests des genres")
    class SexeTests {

        @Test
        @DisplayName("Devrait supporter le genre MASCULIN")
        void shouldSupportMasculin() {
            // Arrange
            client.setSexe(Sexe.MASCULIN);

            // Assert
            assertThat(client.getSexe()).isEqualTo(Sexe.MASCULIN);
            assertThat(client.getSexe().getLibelle()).isEqualTo("Masculin");
        }

        @Test
        @DisplayName("Devrait supporter le genre FEMININ")
        void shouldSupportFeminin() {
            // Arrange
            client.setSexe(Sexe.FEMININ);

            // Assert
            assertThat(client.getSexe()).isEqualTo(Sexe.FEMININ);
            assertThat(client.getSexe().getLibelle()).isEqualTo("Féminin");
        }
    }

    @Nested
    @DisplayName("Tests de la relation Client-Account")
    class ClientAccountRelationTests {

        @Test
        @DisplayName("Devrait pouvoir ajouter un compte")
        void shouldAddAccount() {
            // Arrange
            Account account = Account.builder()
                    .id(1L)
                    .numeroCompte("TG53TG0000000000000000012345678")
                    .build();

            // Act
            client.getComptes().add(account);

            // Assert
            assertThat(client.getComptes()).hasSize(1);
            assertThat(client.getComptes().get(0)).isEqualTo(account);
        }

        @Test
        @DisplayName("Devrait pouvoir avoir plusieurs comptes")
        void shouldHaveMultipleAccounts() {
            // Arrange
            Account account1 = Account.builder().id(1L).build();
            Account account2 = Account.builder().id(2L).build();
            Account account3 = Account.builder().id(3L).build();

            // Act
            client.getComptes().add(account1);
            client.getComptes().add(account2);
            client.getComptes().add(account3);

            // Assert
            assertThat(client.getComptes()).hasSize(3);
        }
    }

    @Nested
    @DisplayName("Tests de validation des dates")
    class DateValidationTests {

        @Test
        @DisplayName("Devrait accepter une date de naissance valide")
        void shouldAcceptValidBirthDate() {
            // Arrange
            LocalDate validDate = LocalDate.of(2000, 1, 1);

            // Act
            client.setDateNaissance(validDate);

            // Assert
            assertThat(client.getDateNaissance()).isEqualTo(validDate);
        }

        @Test
        @DisplayName("Devrait stocker la date de naissance correctement")
        void shouldStoreBirthDateCorrectly() {
            // Assert
            assertThat(client.getDateNaissance().getYear()).isEqualTo(1990);
            assertThat(client.getDateNaissance().getMonthValue()).isEqualTo(5);
            assertThat(client.getDateNaissance().getDayOfMonth()).isEqualTo(15);
        }
    }
}
