package com.ega.egabank.entity;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.ega.egabank.enums.TypeTransaction;

/**
 * Tests unitaires pour l'entité Transaction
 */
@DisplayName("Tests de l'entité Transaction")
class TransactionTest {

    private Transaction transaction;
    private Account account;

    @BeforeEach
    void setUp() {
        account = Account.builder()
                .id(1L)
                .numeroCompte("TG53TG0000000000000000012345678")
                .build();

        transaction = Transaction.builder()
                .id(1L)
                .type(TypeTransaction.DEPOT)
                .montant(new BigDecimal("500.00"))
                .description("Test dépôt")
                .soldeAvant(new BigDecimal("1000.00"))
                .soldeApres(new BigDecimal("1500.00"))
                .compte(account)
                .build();
    }

    @Nested
    @DisplayName("Tests de getDescriptionComplete")
    class GetDescriptionCompleteTests {

        @Test
        @DisplayName("Devrait retourner le libellé du type avec description")
        void shouldReturnTypeWithDescription() {
            // Act
            String descriptionComplete = transaction.getDescriptionComplete();

            // Assert
            assertThat(descriptionComplete).isEqualTo("Dépôt - Test dépôt");
        }

        @Test
        @DisplayName("Devrait retourner uniquement le libellé si description est null")
        void shouldReturnOnlyTypeIfDescriptionNull() {
            // Arrange
            transaction.setDescription(null);

            // Act
            String descriptionComplete = transaction.getDescriptionComplete();

            // Assert
            assertThat(descriptionComplete).isEqualTo("Dépôt");
        }

        @Test
        @DisplayName("Devrait retourner uniquement le libellé si description est vide")
        void shouldReturnOnlyTypeIfDescriptionEmpty() {
            // Arrange
            transaction.setDescription("");

            // Act
            String descriptionComplete = transaction.getDescriptionComplete();

            // Assert
            assertThat(descriptionComplete).isEqualTo("Dépôt");
        }

        @Test
        @DisplayName("Devrait inclure le compte destination pour les virements")
        void shouldIncludeDestinationForTransfers() {
            // Arrange
            transaction.setType(TypeTransaction.VIREMENT_SORTANT);
            transaction.setCompteDestination("TG53TG0000000000000000087654321");

            // Act
            String descriptionComplete = transaction.getDescriptionComplete();

            // Assert
            assertThat(descriptionComplete).contains("vers TG53TG0000000000000000087654321");
        }

        @Test
        @DisplayName("Devrait afficher description et compte destination")
        void shouldIncludeBothDescriptionAndDestination() {
            // Arrange
            transaction.setType(TypeTransaction.VIREMENT_SORTANT);
            transaction.setDescription("Virement mensuel");
            transaction.setCompteDestination("TG53TG0000000000000000087654321");

            // Act
            String descriptionComplete = transaction.getDescriptionComplete();

            // Assert
            assertThat(descriptionComplete)
                    .contains("Virement émis")
                    .contains("Virement mensuel")
                    .contains("vers TG53TG0000000000000000087654321");
        }
    }

    @Nested
    @DisplayName("Tests du Builder")
    class BuilderTests {

        @Test
        @DisplayName("Devrait créer une transaction avec toutes les valeurs")
        void shouldCreateTransactionWithAllValues() {
            // Arrange & Act
            Transaction newTransaction = Transaction.builder()
                    .id(99L)
                    .type(TypeTransaction.RETRAIT)
                    .montant(new BigDecimal("250.00"))
                    .description("Retrait DAB")
                    .soldeAvant(new BigDecimal("1000.00"))
                    .soldeApres(new BigDecimal("750.00"))
                    .compte(account)
                    .build();

            // Assert
            assertThat(newTransaction.getId()).isEqualTo(99L);
            assertThat(newTransaction.getType()).isEqualTo(TypeTransaction.RETRAIT);
            assertThat(newTransaction.getMontant()).isEqualByComparingTo(new BigDecimal("250.00"));
            assertThat(newTransaction.getDescription()).isEqualTo("Retrait DAB");
            assertThat(newTransaction.getSoldeAvant()).isEqualByComparingTo(new BigDecimal("1000.00"));
            assertThat(newTransaction.getSoldeApres()).isEqualByComparingTo(new BigDecimal("750.00"));
            assertThat(newTransaction.getCompte()).isEqualTo(account);
        }
    }

    @Nested
    @DisplayName("Tests des types de transaction")
    class TypeTransactionTests {

        @Test
        @DisplayName("Devrait supporter le type DEPOT")
        void shouldSupportTypeDepot() {
            // Arrange
            transaction.setType(TypeTransaction.DEPOT);

            // Assert
            assertThat(transaction.getType()).isEqualTo(TypeTransaction.DEPOT);
            assertThat(transaction.getType().getLibelle()).isEqualTo("Dépôt");
        }

        @Test
        @DisplayName("Devrait supporter le type RETRAIT")
        void shouldSupportTypeRetrait() {
            // Arrange
            transaction.setType(TypeTransaction.RETRAIT);

            // Assert
            assertThat(transaction.getType()).isEqualTo(TypeTransaction.RETRAIT);
            assertThat(transaction.getType().getLibelle()).isEqualTo("Retrait");
        }

        @Test
        @DisplayName("Devrait supporter le type VIREMENT_ENTRANT")
        void shouldSupportTypeVirementEntrant() {
            // Arrange
            transaction.setType(TypeTransaction.VIREMENT_ENTRANT);

            // Assert
            assertThat(transaction.getType()).isEqualTo(TypeTransaction.VIREMENT_ENTRANT);
            assertThat(transaction.getType().getLibelle()).isEqualTo("Virement reçu");
        }

        @Test
        @DisplayName("Devrait supporter le type VIREMENT_SORTANT")
        void shouldSupportTypeVirementSortant() {
            // Arrange
            transaction.setType(TypeTransaction.VIREMENT_SORTANT);

            // Assert
            assertThat(transaction.getType()).isEqualTo(TypeTransaction.VIREMENT_SORTANT);
            assertThat(transaction.getType().getLibelle()).isEqualTo("Virement émis");
        }
    }

    @Nested
    @DisplayName("Tests de cohérence des soldes")
    class BalanceConsistencyTests {

        @Test
        @DisplayName("Devrait avoir soldeApres > soldeAvant pour un dépôt")
        void shouldHaveHigherBalanceAfterDeposit() {
            // Arrange
            transaction.setType(TypeTransaction.DEPOT);
            transaction.setSoldeAvant(new BigDecimal("1000.00"));
            transaction.setSoldeApres(new BigDecimal("1500.00"));

            // Assert
            assertThat(transaction.getSoldeApres()).isGreaterThan(transaction.getSoldeAvant());
        }

        @Test
        @DisplayName("Devrait avoir soldeApres < soldeAvant pour un retrait")
        void shouldHaveLowerBalanceAfterWithdraw() {
            // Arrange
            transaction.setType(TypeTransaction.RETRAIT);
            transaction.setSoldeAvant(new BigDecimal("1000.00"));
            transaction.setSoldeApres(new BigDecimal("800.00"));

            // Assert
            assertThat(transaction.getSoldeApres()).isLessThan(transaction.getSoldeAvant());
        }

        @Test
        @DisplayName("Devrait calculer correctement la différence de solde")
        void shouldCalculateBalanceDifferenceCorrectly() {
            // Arrange
            BigDecimal montant = new BigDecimal("500.00");
            BigDecimal soldeAvant = new BigDecimal("1000.00");
            BigDecimal soldeApres = new BigDecimal("1500.00");

            transaction.setMontant(montant);
            transaction.setSoldeAvant(soldeAvant);
            transaction.setSoldeApres(soldeApres);

            // Assert
            BigDecimal difference = transaction.getSoldeApres().subtract(transaction.getSoldeAvant());
            assertThat(difference).isEqualByComparingTo(montant);
        }
    }
}
