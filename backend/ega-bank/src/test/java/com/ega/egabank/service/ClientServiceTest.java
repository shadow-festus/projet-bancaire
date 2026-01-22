package com.ega.egabank.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
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

import com.ega.egabank.dto.request.ClientRequest;
import com.ega.egabank.dto.response.ClientResponse;
import com.ega.egabank.dto.response.PageResponse;
import com.ega.egabank.entity.Account;
import com.ega.egabank.entity.Client;
import com.ega.egabank.enums.Sexe;
import com.ega.egabank.exception.DuplicateResourceException;
import com.ega.egabank.exception.OperationNotAllowedException;
import com.ega.egabank.exception.ResourceNotFoundException;
import com.ega.egabank.mapper.ClientMapper;
import com.ega.egabank.repository.ClientRepository;
import com.ega.egabank.service.impl.ClientServiceImpl;

/**
 * Tests unitaires pour ClientService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests du service Client")
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ClientMapper clientMapper;

    @InjectMocks
    private ClientServiceImpl clientService;

    private Client client;
    private ClientRequest clientRequest;
    private ClientResponse clientResponse;

    @BeforeEach
    void setUp() {
        client = Client.builder()
                .id(1L)
                .nom("Dupont")
                .prenom("Jean")
                .dateNaissance(LocalDate.of(1990, 5, 15))
                .sexe(Sexe.MASCULIN)
                .courriel("jean.dupont@email.com")
                .telephone("+22890123456")
                .adresse("123 Rue de la Paix, Lomé")
                .nationalite("Togolaise")
                .comptes(new ArrayList<>())
                .build();

        clientRequest = ClientRequest.builder()
                .nom("Dupont")
                .prenom("Jean")
                .dateNaissance(LocalDate.of(1990, 5, 15))
                .sexe(Sexe.MASCULIN)
                .courriel("jean.dupont@email.com")
                .telephone("+22890123456")
                .adresse("123 Rue de la Paix, Lomé")
                .nationalite("Togolaise")
                .build();

        clientResponse = ClientResponse.builder()
                .id(1L)
                .nom("Dupont")
                .prenom("Jean")
                .nomComplet("Jean Dupont")
                .dateNaissance(LocalDate.of(1990, 5, 15))
                .sexe(Sexe.MASCULIN)
                .courriel("jean.dupont@email.com")
                .telephone("+22890123456")
                .adresse("123 Rue de la Paix, Lomé")
                .nationalite("Togolaise")
                .nombreComptes(0)
                .build();
    }

    @Nested
    @DisplayName("Tests de getAllClients")
    class GetAllClientsTests {

        @Test
        @DisplayName("Devrait retourner une page de clients")
        void shouldReturnPageOfClients() {
            // Arrange
            List<Client> clients = List.of(client);
            Page<Client> clientPage = new PageImpl<>(clients, PageRequest.of(0, 10), 1);

            when(clientRepository.findAll(any(Pageable.class))).thenReturn(clientPage);
            when(clientMapper.toResponseList(clients)).thenReturn(List.of(clientResponse));

            // Act
            PageResponse<ClientResponse> result = clientService.getAllClients(0, 10);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(1);
            verify(clientRepository).findAll(any(Pageable.class));
        }

        @Test
        @DisplayName("Devrait retourner une page vide si aucun client")
        void shouldReturnEmptyPageIfNoClients() {
            // Arrange
            Page<Client> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
            when(clientRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);
            when(clientMapper.toResponseList(any())).thenReturn(List.of());

            // Act
            PageResponse<ClientResponse> result = clientService.getAllClients(0, 10);

            // Assert
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }
    }

    @Nested
    @DisplayName("Tests de searchClients")
    class SearchClientsTests {

        @Test
        @DisplayName("Devrait rechercher des clients par terme")
        void shouldSearchClientsByTerm() {
            // Arrange
            String searchTerm = "Dupont";
            List<Client> clients = List.of(client);
            Page<Client> clientPage = new PageImpl<>(clients, PageRequest.of(0, 10), 1);

            when(clientRepository.search(eq(searchTerm), any(Pageable.class))).thenReturn(clientPage);
            when(clientMapper.toResponseList(clients)).thenReturn(List.of(clientResponse));

            // Act
            PageResponse<ClientResponse> result = clientService.searchClients(searchTerm, 0, 10);

            // Assert
            assertThat(result.getContent()).hasSize(1);
            verify(clientRepository).search(eq(searchTerm), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("Tests de getClientById")
    class GetClientByIdTests {

        @Test
        @DisplayName("Devrait retourner un client existant")
        void shouldReturnExistingClient() {
            // Arrange
            when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
            when(clientMapper.toResponse(client)).thenReturn(clientResponse);

            // Act
            ClientResponse result = clientService.getClientById(1L);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getNom()).isEqualTo("Dupont");
        }

        @Test
        @DisplayName("Devrait lancer une exception si le client n'existe pas")
        void shouldThrowExceptionIfClientNotFound() {
            // Arrange
            when(clientRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> clientService.getClientById(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Client");
        }
    }

    @Nested
    @DisplayName("Tests de getClientWithAccounts")
    class GetClientWithAccountsTests {

        @Test
        @DisplayName("Devrait retourner un client avec ses comptes")
        void shouldReturnClientWithAccounts() {
            // Arrange
            when(clientRepository.findByIdWithAccounts(1L)).thenReturn(Optional.of(client));
            when(clientMapper.toResponseWithAccounts(client)).thenReturn(clientResponse);

            // Act
            ClientResponse result = clientService.getClientWithAccounts(1L);

            // Assert
            assertThat(result).isNotNull();
            verify(clientRepository).findByIdWithAccounts(1L);
            verify(clientMapper).toResponseWithAccounts(client);
        }
    }

    @Nested
    @DisplayName("Tests de createClient")
    class CreateClientTests {

        @Test
        @DisplayName("Devrait créer un nouveau client")
        void shouldCreateNewClient() {
            // Arrange
            when(clientRepository.existsByCourriel(clientRequest.getCourriel())).thenReturn(false);
            when(clientMapper.toEntity(clientRequest)).thenReturn(client);
            when(clientRepository.save(client)).thenReturn(client);
            when(clientMapper.toResponse(client)).thenReturn(clientResponse);

            // Act
            ClientResponse result = clientService.createClient(clientRequest);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getNom()).isEqualTo("Dupont");
            verify(clientRepository).save(client);
        }

        @Test
        @DisplayName("Devrait lancer une exception si le courriel existe déjà")
        void shouldThrowExceptionIfEmailExists() {
            // Arrange
            when(clientRepository.existsByCourriel(clientRequest.getCourriel())).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> clientService.createClient(clientRequest))
                    .isInstanceOf(DuplicateResourceException.class);
        }

        @Test
        @DisplayName("Devrait créer un client sans courriel")
        void shouldCreateClientWithoutEmail() {
            // Arrange
            clientRequest.setCourriel(null);
            when(clientMapper.toEntity(clientRequest)).thenReturn(client);
            when(clientRepository.save(client)).thenReturn(client);
            when(clientMapper.toResponse(client)).thenReturn(clientResponse);

            // Act
            ClientResponse result = clientService.createClient(clientRequest);

            // Assert
            assertThat(result).isNotNull();
            verify(clientRepository, never()).existsByCourriel(any());
        }
    }

    @Nested
    @DisplayName("Tests de updateClient")
    class UpdateClientTests {

        @Test
        @DisplayName("Devrait mettre à jour un client existant")
        void shouldUpdateExistingClient() {
            // Arrange
            ClientRequest updateRequest = ClientRequest.builder()
                    .nom("DupontModifié")
                    .prenom("Jean")
                    .dateNaissance(LocalDate.of(1990, 5, 15))
                    .sexe(Sexe.MASCULIN)
                    .courriel("jean.dupont@email.com")
                    .build();

            when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
            when(clientRepository.save(client)).thenReturn(client);
            when(clientMapper.toResponse(client)).thenReturn(clientResponse);

            // Act
            ClientResponse result = clientService.updateClient(1L, updateRequest);

            // Assert
            assertThat(result).isNotNull();
            verify(clientMapper).updateEntity(client, updateRequest);
            verify(clientRepository).save(client);
        }

        @Test
        @DisplayName("Devrait lancer une exception si le nouveau courriel existe déjà")
        void shouldThrowExceptionIfNewEmailExists() {
            // Arrange
            ClientRequest updateRequest = ClientRequest.builder()
                    .nom("Dupont")
                    .prenom("Jean")
                    .dateNaissance(LocalDate.of(1990, 5, 15))
                    .sexe(Sexe.MASCULIN)
                    .courriel("autre@email.com")
                    .build();

            when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
            when(clientRepository.existsByCourriel("autre@email.com")).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> clientService.updateClient(1L, updateRequest))
                    .isInstanceOf(DuplicateResourceException.class);
        }
    }

    @Nested
    @DisplayName("Tests de deleteClient")
    class DeleteClientTests {

        @Test
        @DisplayName("Devrait supprimer un client sans comptes avec solde")
        void shouldDeleteClientWithoutBalances() {
            // Arrange
            when(clientRepository.findByIdWithAccounts(1L)).thenReturn(Optional.of(client));

            // Act
            clientService.deleteClient(1L);

            // Assert
            verify(clientRepository).delete(client);
        }

        @Test
        @DisplayName("Devrait lancer une exception si des comptes ont un solde non nul")
        void shouldThrowExceptionIfAccountsHaveBalance() {
            // Arrange
            Account accountWithBalance = Account.builder()
                    .id(1L)
                    .solde(new BigDecimal("1000.00"))
                    .build();
            client.getComptes().add(accountWithBalance);

            when(clientRepository.findByIdWithAccounts(1L)).thenReturn(Optional.of(client));

            // Act & Assert
            assertThatThrownBy(() -> clientService.deleteClient(1L))
                    .isInstanceOf(OperationNotAllowedException.class)
                    .hasMessageContaining("solde non nul");
        }

        @Test
        @DisplayName("Devrait permettre la suppression si les comptes ont un solde nul")
        void shouldAllowDeletionIfAccountsHaveZeroBalance() {
            // Arrange
            Account accountZeroBalance = Account.builder()
                    .id(1L)
                    .solde(BigDecimal.ZERO)
                    .build();
            client.getComptes().add(accountZeroBalance);

            when(clientRepository.findByIdWithAccounts(1L)).thenReturn(Optional.of(client));

            // Act
            clientService.deleteClient(1L);

            // Assert
            verify(clientRepository).delete(client);
        }
    }
}
