package com.ega.egabank.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ega.egabank.dto.request.OperationRequest;
import com.ega.egabank.dto.request.TransferRequest;
import com.ega.egabank.dto.response.TransactionResponse;
import com.ega.egabank.entity.Account;
import com.ega.egabank.entity.Transaction;
import com.ega.egabank.enums.TypeCompte;
import com.ega.egabank.enums.TypeTransaction;
import com.ega.egabank.exception.InsufficientBalanceException;
import com.ega.egabank.exception.OperationNotAllowedException;
import com.ega.egabank.exception.ResourceNotFoundException;
import com.ega.egabank.mapper.TransactionMapper;
import com.ega.egabank.repository.AccountRepository;
import com.ega.egabank.repository.TransactionRepository;
import com.ega.egabank.service.impl.TransactionServiceImpl;

/**
 * Tests unitaires pour TransactionService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests du service Transaction")
class TransactionServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private Account sourceAccount;
    private Account destinationAccount;
    private OperationRequest depositRequest;
    private OperationRequest withdrawRequest;
    private TransferRequest transferRequest;
    private TransactionResponse transactionResponse;

    private static final String SOURCE_IBAN = "TG53TG0000000000000000012345678";
    private static final String DEST_IBAN = "TG53TG0000000000000000087654321";

    @BeforeEach
    void setUp() {
        sourceAccount = Account.builder()
                .id(1L)
                .numeroCompte(SOURCE_IBAN)
                .typeCompte(TypeCompte.COURANT)
                .solde(new BigDecimal("1000.00"))
                .actif(true)
                .build();

        destinationAccount = Account.builder()
                .id(2L)
                .numeroCompte(DEST_IBAN)
                .typeCompte(TypeCompte.EPARGNE)
                .solde(new BigDecimal("500.00"))
                .actif(true)
                .build();

        depositRequest = OperationRequest.builder()
                .montant(new BigDecimal("200.00"))
                .description("Dépôt test")
                .build();

        withdrawRequest = OperationRequest.builder()
                .montant(new BigDecimal("150.00"))
                .description("Retrait test")
                .build();

        transferRequest = TransferRequest.builder()
                .compteSource(SOURCE_IBAN)
                .compteDestination(DEST_IBAN)
                .montant(new BigDecimal("300.00"))
                .description("Virement test")
                .build();

        transactionResponse = TransactionResponse.builder()
                .id(1L)
                .type(TypeTransaction.DEPOT)
                .montant(new BigDecimal("200.00"))
                .build();
    }

    @Nested
    @DisplayName("Tests de deposit")
    class DepositTests {

        @Test
        @DisplayName("Devrait effectuer un dépôt avec succès")
        void shouldDepositSuccessfully() {
            // Arrange
            BigDecimal initialBalance = sourceAccount.getSolde();
            when(accountRepository.findByNumeroCompte(SOURCE_IBAN)).thenReturn(Optional.of(sourceAccount));
            when(accountRepository.save(any(Account.class))).thenReturn(sourceAccount);
            when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));
            when(transactionMapper.toResponse(any(Transaction.class))).thenReturn(transactionResponse);

            // Act
            TransactionResponse result = transactionService.deposit(SOURCE_IBAN, depositRequest);

            // Assert
            assertThat(result).isNotNull();
            assertThat(sourceAccount.getSolde())
                    .isEqualByComparingTo(initialBalance.add(depositRequest.getMontant()));

            ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
            verify(transactionRepository).save(transactionCaptor.capture());
            assertThat(transactionCaptor.getValue().getType()).isEqualTo(TypeTransaction.DEPOT);
        }

        @Test
        @DisplayName("Devrait utiliser une description par défaut si aucune n'est fournie")
        void shouldUseDefaultDescriptionIfNoneProvided() {
            // Arrange
            depositRequest.setDescription(null);
            when(accountRepository.findByNumeroCompte(SOURCE_IBAN)).thenReturn(Optional.of(sourceAccount));
            when(accountRepository.save(any(Account.class))).thenReturn(sourceAccount);
            when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));
            when(transactionMapper.toResponse(any(Transaction.class))).thenReturn(transactionResponse);

            // Act
            transactionService.deposit(SOURCE_IBAN, depositRequest);

            // Assert
            ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
            verify(transactionRepository).save(transactionCaptor.capture());
            assertThat(transactionCaptor.getValue().getDescription()).isEqualTo("Dépôt");
        }

        @Test
        @DisplayName("Devrait lancer une exception si le compte n'existe pas")
        void shouldThrowExceptionIfAccountNotFound() {
            // Arrange
            when(accountRepository.findByNumeroCompte("INVALID")).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> transactionService.deposit("INVALID", depositRequest))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Devrait lancer une exception si le compte est inactif")
        void shouldThrowExceptionIfAccountInactive() {
            // Arrange
            sourceAccount.setActif(false);
            when(accountRepository.findByNumeroCompte(SOURCE_IBAN)).thenReturn(Optional.of(sourceAccount));

            // Act & Assert
            assertThatThrownBy(() -> transactionService.deposit(SOURCE_IBAN, depositRequest))
                    .isInstanceOf(OperationNotAllowedException.class)
                    .hasMessageContaining("inactif");
        }
    }

    @Nested
    @DisplayName("Tests de withdraw")
    class WithdrawTests {

        @Test
        @DisplayName("Devrait effectuer un retrait avec succès")
        void shouldWithdrawSuccessfully() {
            // Arrange
            BigDecimal initialBalance = sourceAccount.getSolde();
            when(accountRepository.findByNumeroCompte(SOURCE_IBAN)).thenReturn(Optional.of(sourceAccount));
            when(accountRepository.save(any(Account.class))).thenReturn(sourceAccount);
            when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));
            when(transactionMapper.toResponse(any(Transaction.class))).thenReturn(transactionResponse);

            // Act
            TransactionResponse result = transactionService.withdraw(SOURCE_IBAN, withdrawRequest);

            // Assert
            assertThat(result).isNotNull();
            assertThat(sourceAccount.getSolde())
                    .isEqualByComparingTo(initialBalance.subtract(withdrawRequest.getMontant()));

            ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
            verify(transactionRepository).save(transactionCaptor.capture());
            assertThat(transactionCaptor.getValue().getType()).isEqualTo(TypeTransaction.RETRAIT);
        }

        @Test
        @DisplayName("Devrait lancer une exception si le solde est insuffisant")
        void shouldThrowExceptionIfInsufficientBalance() {
            // Arrange
            withdrawRequest.setMontant(new BigDecimal("5000.00")); // Plus que le solde
            when(accountRepository.findByNumeroCompte(SOURCE_IBAN)).thenReturn(Optional.of(sourceAccount));

            // Act & Assert
            assertThatThrownBy(() -> transactionService.withdraw(SOURCE_IBAN, withdrawRequest))
                    .isInstanceOf(InsufficientBalanceException.class);
        }

        @Test
        @DisplayName("Devrait permettre un retrait du montant exact du solde")
        void shouldAllowWithdrawExactBalance() {
            // Arrange
            withdrawRequest.setMontant(new BigDecimal("1000.00")); // Montant exact
            when(accountRepository.findByNumeroCompte(SOURCE_IBAN)).thenReturn(Optional.of(sourceAccount));
            when(accountRepository.save(any(Account.class))).thenReturn(sourceAccount);
            when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));
            when(transactionMapper.toResponse(any(Transaction.class))).thenReturn(transactionResponse);

            // Act
            transactionService.withdraw(SOURCE_IBAN, withdrawRequest);

            // Assert
            assertThat(sourceAccount.getSolde()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("Tests de transfer")
    class TransferTests {

        @Test
        @DisplayName("Devrait effectuer un virement avec succès")
        void shouldTransferSuccessfully() {
            // Arrange
            BigDecimal sourceInitial = sourceAccount.getSolde();
            BigDecimal destInitial = destinationAccount.getSolde();

            when(accountRepository.findByNumeroCompte(SOURCE_IBAN)).thenReturn(Optional.of(sourceAccount));
            when(accountRepository.findByNumeroCompte(DEST_IBAN)).thenReturn(Optional.of(destinationAccount));
            when(accountRepository.save(any(Account.class))).thenAnswer(i -> i.getArgument(0));
            when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));
            when(transactionMapper.toResponse(any(Transaction.class))).thenReturn(transactionResponse);

            // Act
            TransactionResponse result = transactionService.transfer(transferRequest);

            // Assert
            assertThat(result).isNotNull();
            assertThat(sourceAccount.getSolde())
                    .isEqualByComparingTo(sourceInitial.subtract(transferRequest.getMontant()));
            assertThat(destinationAccount.getSolde())
                    .isEqualByComparingTo(destInitial.add(transferRequest.getMontant()));

            // Vérifier que 2 transactions ont été créées (sortante et entrante)
            verify(transactionRepository, times(2)).save(any(Transaction.class));
        }

        @Test
        @DisplayName("Devrait lancer une exception si source et destination sont identiques")
        void shouldThrowExceptionIfSameAccount() {
            // Arrange
            transferRequest.setCompteDestination(SOURCE_IBAN);

            // Act & Assert
            assertThatThrownBy(() -> transactionService.transfer(transferRequest))
                    .isInstanceOf(OperationNotAllowedException.class)
                    .hasMessageContaining("identiques");
        }

        @Test
        @DisplayName("Devrait lancer une exception si le solde source est insuffisant")
        void shouldThrowExceptionIfInsufficientSourceBalance() {
            // Arrange
            transferRequest.setMontant(new BigDecimal("5000.00"));
            when(accountRepository.findByNumeroCompte(SOURCE_IBAN)).thenReturn(Optional.of(sourceAccount));
            when(accountRepository.findByNumeroCompte(DEST_IBAN)).thenReturn(Optional.of(destinationAccount));

            // Act & Assert
            assertThatThrownBy(() -> transactionService.transfer(transferRequest))
                    .isInstanceOf(InsufficientBalanceException.class);
        }

        @Test
        @DisplayName("Devrait lancer une exception si le compte destination est inactif")
        void shouldThrowExceptionIfDestinationInactive() {
            // Arrange
            destinationAccount.setActif(false);
            when(accountRepository.findByNumeroCompte(SOURCE_IBAN)).thenReturn(Optional.of(sourceAccount));
            when(accountRepository.findByNumeroCompte(DEST_IBAN)).thenReturn(Optional.of(destinationAccount));

            // Act & Assert
            assertThatThrownBy(() -> transactionService.transfer(transferRequest))
                    .isInstanceOf(OperationNotAllowedException.class);
        }
    }

    @Nested
    @DisplayName("Tests de getTransactionHistory")
    class GetTransactionHistoryTests {

        @Test
        @DisplayName("Devrait retourner l'historique des transactions sur une période")
        void shouldReturnTransactionHistory() {
            // Arrange
            LocalDate debut = LocalDate.now().minusDays(30);
            LocalDate fin = LocalDate.now();
            List<Transaction> transactions = List.of(
                    Transaction.builder().id(1L).type(TypeTransaction.DEPOT).montant(new BigDecimal("100")).build());

            when(accountRepository.existsByNumeroCompte(SOURCE_IBAN)).thenReturn(true);
            when(transactionRepository.findByCompteAndPeriod(eq(SOURCE_IBAN), any(LocalDateTime.class),
                    any(LocalDateTime.class)))
                    .thenReturn(transactions);
            when(transactionMapper.toResponseList(transactions)).thenReturn(List.of(transactionResponse));

            // Act
            List<TransactionResponse> result = transactionService.getTransactionHistory(SOURCE_IBAN, debut, fin);

            // Assert
            assertThat(result).hasSize(1);
            verify(transactionRepository).findByCompteAndPeriod(eq(SOURCE_IBAN), any(), any());
        }

        @Test
        @DisplayName("Devrait lancer une exception si le compte n'existe pas")
        void shouldThrowExceptionIfAccountNotFound() {
            // Arrange
            when(accountRepository.existsByNumeroCompte("INVALID")).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(
                    () -> transactionService.getTransactionHistory("INVALID", LocalDate.now(), LocalDate.now()))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Tests de getAllTransactionsByAccount")
    class GetAllTransactionsByAccountTests {

        @Test
        @DisplayName("Devrait retourner toutes les transactions d'un compte")
        void shouldReturnAllTransactions() {
            // Arrange
            List<Transaction> transactions = List.of(
                    Transaction.builder().id(1L).type(TypeTransaction.DEPOT).montant(new BigDecimal("100")).build(),
                    Transaction.builder().id(2L).type(TypeTransaction.RETRAIT).montant(new BigDecimal("50")).build());

            when(accountRepository.findByNumeroCompte(SOURCE_IBAN)).thenReturn(Optional.of(sourceAccount));
            when(transactionRepository.findByCompteIdOrderByDateTransactionDesc(sourceAccount.getId()))
                    .thenReturn(transactions);
            when(transactionMapper.toResponseList(transactions))
                    .thenReturn(List.of(transactionResponse, transactionResponse));

            // Act
            List<TransactionResponse> result = transactionService.getAllTransactionsByAccount(SOURCE_IBAN);

            // Assert
            assertThat(result).hasSize(2);
        }
    }
}
