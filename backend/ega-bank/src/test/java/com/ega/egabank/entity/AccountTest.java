package com.ega.egabank.entity;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.ega.egabank.enums.TypeCompte;

/**
 * Tests unitaires pour l'entité Account
 */
@DisplayName("Tests de l'entité Account")
class AccountTest {

    private Account account;

    @BeforeEach
    void setUp() {
        account = Account.builder()
                .id(1L)
                .numeroCompte("TG53TG0000000000000000012345678")
                .typeCompte(TypeCompte.COURANT)
                .solde(new BigDecimal("1000.00"))
                .actif(true)
                .build();
    }

    @Nested
    @DisplayName("Tests de crediter")
    class CrediterTests {

        @Test
        @DisplayName("Devrait créditer le compte correctement")
        void shouldCreditAccountCorrectly() {
            // Arrange
            BigDecimal initialBalance = account.getSolde();
            BigDecimal amount = new BigDecimal("500.00");

            // Act
            account.crediter(amount);

            // Assert
            assertThat(account.getSolde()).isEqualByComparingTo(initialBalance.add(amount));
        }

        @Test
        @DisplayName("Devrait créditer avec un montant décimal")
        void shouldCreditWithDecimalAmount() {
            // Arrange
            BigDecimal amount = new BigDecimal("123.45");
            BigDecimal expected = new BigDecimal("1123.45");

            // Act
            account.crediter(amount);

            // Assert
            assertThat(account.getSolde()).isEqualByComparingTo(expected);
        }

        @Test
        @DisplayName("Devrait permettre plusieurs crédits successifs")
        void shouldAllowMultipleCredits() {
            // Act
            account.crediter(new BigDecimal("100"));
            account.crediter(new BigDecimal("200"));
            account.crediter(new BigDecimal("300"));

            // Assert
            assertThat(account.getSolde()).isEqualByComparingTo(new BigDecimal("1600.00"));
        }
    }

    @Nested
    @DisplayName("Tests de debiter")
    class DebiterTests {

        @Test
        @DisplayName("Devrait débiter le compte correctement")
        void shouldDebitAccountCorrectly() {
            // Arrange
            BigDecimal amount = new BigDecimal("300.00");

            // Act
            account.debiter(amount);

            // Assert
            assertThat(account.getSolde()).isEqualByComparingTo(new BigDecimal("700.00"));
        }

        @Test
        @DisplayName("Devrait permettre de débiter jusqu'à zéro")
        void shouldAllowDebitToZero() {
            // Act
            account.debiter(new BigDecimal("1000.00"));

            // Assert
            assertThat(account.getSolde()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Devrait permettre des débits successifs")
        void shouldAllowMultipleDebits() {
            // Act
            account.debiter(new BigDecimal("100"));
            account.debiter(new BigDecimal("200"));
            account.debiter(new BigDecimal("300"));

            // Assert
            assertThat(account.getSolde()).isEqualByComparingTo(new BigDecimal("400.00"));
        }
    }

    @Nested
    @DisplayName("Tests de soldeEstSuffisant")
    class SoldeEstSuffisantTests {

        @Test
        @DisplayName("Devrait retourner true si le solde est suffisant")
        void shouldReturnTrueIfBalanceSufficient() {
            // Act & Assert
            assertThat(account.soldeEstSuffisant(new BigDecimal("500.00"))).isTrue();
        }

        @Test
        @DisplayName("Devrait retourner true si le montant égale le solde")
        void shouldReturnTrueIfAmountEqualsBalance() {
            // Act & Assert
            assertThat(account.soldeEstSuffisant(new BigDecimal("1000.00"))).isTrue();
        }

        @Test
        @DisplayName("Devrait retourner false si le solde est insuffisant")
        void shouldReturnFalseIfBalanceInsufficient() {
            // Act & Assert
            assertThat(account.soldeEstSuffisant(new BigDecimal("1500.00"))).isFalse();
        }

        @Test
        @DisplayName("Devrait gérer les montants décimaux")
        void shouldHandleDecimalAmounts() {
            // Arrange
            account.setSolde(new BigDecimal("100.50"));

            // Act & Assert
            assertThat(account.soldeEstSuffisant(new BigDecimal("100.49"))).isTrue();
            assertThat(account.soldeEstSuffisant(new BigDecimal("100.50"))).isTrue();
            assertThat(account.soldeEstSuffisant(new BigDecimal("100.51"))).isFalse();
        }
    }

    @Nested
    @DisplayName("Tests du Builder")
    class BuilderTests {

        @Test
        @DisplayName("Devrait créer un compte avec les valeurs par défaut")
        void shouldCreateAccountWithDefaults() {
            // Arrange & Act
            Account newAccount = Account.builder()
                    .numeroCompte("TG53TG0000000000000000099999999")
                    .typeCompte(TypeCompte.EPARGNE)
                    .build();

            // Assert
            assertThat(newAccount.getSolde()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(newAccount.getActif()).isTrue();
            assertThat(newAccount.getTransactions()).isEmpty();
        }

        @Test
        @DisplayName("Devrait créer un compte avec toutes les valeurs")
        void shouldCreateAccountWithAllValues() {
            // Arrange
            Client client = Client.builder().id(1L).nom("Test").build();
            LocalDateTime now = LocalDateTime.now();

            // Act
            Account newAccount = Account.builder()
                    .id(99L)
                    .numeroCompte("TG53TG0000000000000000099999999")
                    .typeCompte(TypeCompte.EPARGNE)
                    .solde(new BigDecimal("5000.00"))
                    .actif(false)
                    .dateCreation(now)
                    .proprietaire(client)
                    .build();

            // Assert
            assertThat(newAccount.getId()).isEqualTo(99L);
            assertThat(newAccount.getNumeroCompte()).isEqualTo("TG53TG0000000000000000099999999");
            assertThat(newAccount.getTypeCompte()).isEqualTo(TypeCompte.EPARGNE);
            assertThat(newAccount.getSolde()).isEqualByComparingTo(new BigDecimal("5000.00"));
            assertThat(newAccount.getActif()).isFalse();
            assertThat(newAccount.getProprietaire()).isEqualTo(client);
        }
    }

    @Nested
    @DisplayName("Tests des types de compte")
    class TypeCompteTests {

        @Test
        @DisplayName("Devrait supporter le type COURANT")
        void shouldSupportTypeCourant() {
            // Arrange
            account.setTypeCompte(TypeCompte.COURANT);

            // Assert
            assertThat(account.getTypeCompte()).isEqualTo(TypeCompte.COURANT);
            assertThat(account.getTypeCompte().getLibelle()).isEqualTo("Compte Courant");
        }

        @Test
        @DisplayName("Devrait supporter le type EPARGNE")
        void shouldSupportTypeEpargne() {
            // Arrange
            account.setTypeCompte(TypeCompte.EPARGNE);

            // Assert
            assertThat(account.getTypeCompte()).isEqualTo(TypeCompte.EPARGNE);
            assertThat(account.getTypeCompte().getLibelle()).isEqualTo("Compte Épargne");
        }
    }
}
