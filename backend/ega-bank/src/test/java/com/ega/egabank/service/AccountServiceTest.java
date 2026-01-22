package com.ega.egabank.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.ega.egabank.dto.request.AccountRequest;
import com.ega.egabank.dto.response.AccountResponse;
import com.ega.egabank.dto.response.PageResponse;
import com.ega.egabank.entity.Account;
import com.ega.egabank.entity.Client;
import com.ega.egabank.enums.TypeCompte;
import com.ega.egabank.exception.AccountGenerationException;
import com.ega.egabank.exception.OperationNotAllowedException;
import com.ega.egabank.exception.ResourceNotFoundException;
import com.ega.egabank.mapper.AccountMapper;
import com.ega.egabank.repository.AccountRepository;
import com.ega.egabank.repository.ClientRepository;
import com.ega.egabank.service.impl.AccountServiceImpl;
import com.ega.egabank.util.IbanGenerator;

/**
 * Tests unitaires pour AccountService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests du service Account")
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private IbanGenerator ibanGenerator;

    @InjectMocks
    private AccountServiceImpl accountService;

    private Account account;
    private Client client;
    private AccountRequest accountRequest;
    private AccountResponse accountResponse;
    private static final String IBAN = "TG53TG0000000000000000012345678";

    @BeforeEach
    void setUp() {
        client = Client.builder()
                .id(1L)
                .nom("Dupont")
                .prenom("Jean")
                .build();

        account = Account.builder()
                .id(1L)
                .numeroCompte(IBAN)
                .typeCompte(TypeCompte.COURANT)
                .solde(BigDecimal.ZERO)
                .actif(true)
                .proprietaire(client)
                .build();

        accountRequest = AccountRequest.builder()
                .typeCompte(TypeCompte.COURANT)
                .clientId(1L)
                .build();

        accountResponse = AccountResponse.builder()
                .id(1L)
                .numeroCompte(IBAN)
                .typeCompte(TypeCompte.COURANT)
                .typeCompteLibelle("Compte Courant")
                .solde(BigDecimal.ZERO)
                .actif(true)
                .clientId(1L)
                .clientNomComplet("Jean Dupont")
                .build();
    }

    @Nested
    @DisplayName("Tests de getAllAccounts")
    class GetAllAccountsTests {

        @Test
        @DisplayName("Devrait retourner une page de comptes actifs")
        void shouldReturnPageOfActiveAccounts() {
            // Arrange
            List<Account> accounts = List.of(account);
            Page<Account> accountPage = new PageImpl<>(accounts, PageRequest.of(0, 10), 1);

            when(accountRepository.findAll(any(Pageable.class))).thenReturn(accountPage);
            when(accountMapper.toResponseList(accounts)).thenReturn(List.of(accountResponse));

            // Act
            PageResponse<AccountResponse> result = accountService.getAllAccounts(0, 10);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(1);
            verify(accountRepository).findAll(any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("Tests de getAccountByNumber")
    class GetAccountByNumberTests {

        @Test
        @DisplayName("Devrait retourner un compte par son numéro")
        void shouldReturnAccountByNumber() {
            // Arrange
            when(accountRepository.findByNumeroCompteWithClient(IBAN)).thenReturn(Optional.of(account));
            when(accountMapper.toResponse(account)).thenReturn(accountResponse);

            // Act
            AccountResponse result = accountService.getAccountByNumber(IBAN);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getNumeroCompte()).isEqualTo(IBAN);
        }

        @Test
        @DisplayName("Devrait lancer une exception si le compte n'existe pas")
        void shouldThrowExceptionIfAccountNotFound() {
            // Arrange
            when(accountRepository.findByNumeroCompteWithClient("INVALID")).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> accountService.getAccountByNumber("INVALID"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Compte");
        }
    }

    @Nested
    @DisplayName("Tests de getAccountsByClient")
    class GetAccountsByClientTests {

        @Test
        @DisplayName("Devrait retourner les comptes d'un client")
        void shouldReturnAccountsByClient() {
            // Arrange
            when(clientRepository.existsById(1L)).thenReturn(true);
            when(accountRepository.findByProprietaireId(1L)).thenReturn(List.of(account));
            when(accountMapper.toResponseList(any())).thenReturn(List.of(accountResponse));

            // Act
            List<AccountResponse> result = accountService.getAccountsByClient(1L);

            // Assert
            assertThat(result).hasSize(1);
            verify(accountRepository).findByProprietaireId(1L);
        }

        @Test
        @DisplayName("Devrait lancer une exception si le client n'existe pas")
        void shouldThrowExceptionIfClientNotFound() {
            // Arrange
            when(clientRepository.existsById(999L)).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> accountService.getAccountsByClient(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Client");
        }
    }

    @Nested
    @DisplayName("Tests de createAccount")
    class CreateAccountTests {

        @Test
        @DisplayName("Devrait créer un nouveau compte")
        void shouldCreateNewAccount() {
            // Arrange
            when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
            when(ibanGenerator.generate()).thenReturn(IBAN);
            when(accountRepository.existsByNumeroCompte(IBAN)).thenReturn(false);
            when(accountRepository.save(any(Account.class))).thenReturn(account);
            when(accountMapper.toResponse(any(Account.class))).thenReturn(accountResponse);

            // Act
            AccountResponse result = accountService.createAccount(accountRequest);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getNumeroCompte()).isEqualTo(IBAN);
            verify(accountRepository).save(any(Account.class));
        }

        @Test
        @DisplayName("Devrait générer un nouvel IBAN si le premier existe déjà")
        void shouldGenerateNewIbanIfFirstExists() {
            // Arrange
            String secondIban = "TG53TG0090123456789012345678";
            when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
            when(ibanGenerator.generate()).thenReturn(IBAN, secondIban);
            when(accountRepository.existsByNumeroCompte(IBAN)).thenReturn(true);
            when(accountRepository.existsByNumeroCompte(secondIban)).thenReturn(false);
            when(accountRepository.save(any(Account.class))).thenReturn(account);
            when(accountMapper.toResponse(any(Account.class))).thenReturn(accountResponse);

            // Act
            AccountResponse result = accountService.createAccount(accountRequest);

            // Assert
            assertThat(result).isNotNull();
            verify(ibanGenerator, times(2)).generate();
        }

        @Test
        @DisplayName("Devrait lancer une exception si le nombre maximum de tentatives est atteint")
        void shouldThrowExceptionIfMaxAttemptsReached() {
            // Arrange
            when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
            when(ibanGenerator.generate()).thenReturn(IBAN);
            when(accountRepository.existsByNumeroCompte(IBAN)).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> accountService.createAccount(accountRequest))
                    .isInstanceOf(AccountGenerationException.class)
                    .hasMessageContaining("Impossible de générer un numéro de compte");

            verify(ibanGenerator, atLeast(101)).generate();

        }

        @Test
        @DisplayName("Devrait lancer une exception si le client n'existe pas")
        void shouldThrowExceptionIfClientNotFound() {
            // Arrange
            when(clientRepository.findById(999L)).thenReturn(Optional.empty());
            accountRequest.setClientId(999L);

            // Act & Assert
            assertThatThrownBy(() -> accountService.createAccount(accountRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Client");
        }
    }

    @Nested
    @DisplayName("Tests de deleteAccount")
    class DeleteAccountTests {

        @Test
        @DisplayName("Devrait supprimer un compte avec solde nul")
        void shouldDeleteAccountWithZeroBalance() {
            // Arrange
            account.setSolde(BigDecimal.ZERO);
            when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

            // Act
            accountService.deleteAccount(1L);

            // Assert
            verify(accountRepository).delete(account);
        }

        @Test
        @DisplayName("Devrait lancer une exception si le solde n'est pas nul")
        void shouldThrowExceptionIfBalanceNotZero() {
            // Arrange
            account.setSolde(new BigDecimal("100.00"));
            when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

            // Act & Assert
            assertThatThrownBy(() -> accountService.deleteAccount(1L))
                    .isInstanceOf(OperationNotAllowedException.class)
                    .hasMessageContaining("solde n'est pas nul");
        }

        @Test
        @DisplayName("Devrait lancer une exception si le compte n'existe pas")
        void shouldThrowExceptionIfAccountNotFound() {
            // Arrange
            when(accountRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> accountService.deleteAccount(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Tests de deactivateAccount")
    class DeactivateAccountTests {

        @Test
        @DisplayName("Devrait désactiver un compte actif")
        void shouldDeactivateActiveAccount() {
            // Arrange
            when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
            when(accountRepository.save(account)).thenReturn(account);

            // Act
            accountService.deactivateAccount(1L);

            // Assert
            assertThat(account.getActif()).isFalse();
            verify(accountRepository).save(account);
        }

        @Test
        @DisplayName("Devrait lancer une exception si le compte n'existe pas")
        void shouldThrowExceptionIfAccountNotFoundForDeactivation() {
            // Arrange
            when(accountRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> accountService.deactivateAccount(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
