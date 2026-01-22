package com.ega.egabank.service.impl;

import java.math.BigDecimal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ega.egabank.dto.request.ClientRequest;
import com.ega.egabank.dto.response.ClientResponse;
import com.ega.egabank.dto.response.PageResponse;
import com.ega.egabank.entity.Client;
import com.ega.egabank.exception.DuplicateResourceException;
import com.ega.egabank.exception.OperationNotAllowedException;
import com.ega.egabank.exception.ResourceNotFoundException;
import com.ega.egabank.mapper.ClientMapper;
import com.ega.egabank.repository.ClientRepository;
import com.ega.egabank.repository.UserRepository;
import com.ega.egabank.service.ClientService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implémentation du service de gestion des clients
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ClientResponse> getAllClients(int page, int size) {
        log.debug("Récupération de tous les clients - page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by("nom", "prenom").ascending());
        Page<Client> clientPage = clientRepository.findAll(pageable);

        return PageResponse.of(
                clientMapper.toResponseList(clientPage.getContent()),
                clientPage.getNumber(),
                clientPage.getSize(),
                clientPage.getTotalElements(),
                clientPage.getTotalPages());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ClientResponse> searchClients(String search, int page, int size) {
        log.debug("Recherche de clients avec: '{}' - page: {}, size: {}", search, page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by("nom", "prenom").ascending());
        Page<Client> clientPage = clientRepository.search(search, pageable);

        return PageResponse.of(
                clientMapper.toResponseList(clientPage.getContent()),
                clientPage.getNumber(),
                clientPage.getSize(),
                clientPage.getTotalElements(),
                clientPage.getTotalPages());
    }

    @Override
    @Transactional(readOnly = true)
    public ClientResponse getClientById(Long id) {
        log.debug("Récupération du client avec id: {}", id);
        Client client = findClientById(id);
        return clientMapper.toResponse(client);
    }

    @Override
    @Transactional(readOnly = true)
    public ClientResponse getClientWithAccounts(Long id) {
        log.debug("Récupération du client avec ses comptes - id: {}", id);
        Client client = clientRepository.findByIdWithAccounts(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client", "id", id));
        return clientMapper.toResponseWithAccounts(client);
    }

    @Override
    public ClientResponse createClient(ClientRequest request) {
        log.info("Création d'un nouveau client: {} {}", request.getPrenom(), request.getNom());

        // Vérifier l'unicité du courriel
        if (request.getCourriel() != null && clientRepository.existsByCourriel(request.getCourriel())) {
            throw new DuplicateResourceException("Client", "courriel", request.getCourriel());
        }

        Client client = clientMapper.toEntity(request);
        client = clientRepository.save(client);

        log.info("Client créé avec succès - id: {}", client.getId());
        return clientMapper.toResponse(client);
    }

    @Override
    public ClientResponse updateClient(Long id, ClientRequest request) {
        log.info("Mise à jour du client - id: {}", id);

        Client client = findClientById(id);

        // Vérifier l'unicité du courriel si modifié
        if (request.getCourriel() != null &&
                !request.getCourriel().equals(client.getCourriel()) &&
                clientRepository.existsByCourriel(request.getCourriel())) {
            throw new DuplicateResourceException("Client", "courriel", request.getCourriel());
        }

        clientMapper.updateEntity(client, request);
        client = clientRepository.save(client);

        log.info("Client mis à jour avec succès - id: {}", id);
        return clientMapper.toResponse(client);
    }

    @Override
    public void deleteClient(Long id) {
        log.info("Suppression du client - id: {}", id);

        Client client = clientRepository.findByIdWithAccounts(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client", "id", id));

        // Vérifier que tous les comptes ont un solde nul
        boolean hasNonZeroBalance = client.getComptes().stream()
                .anyMatch(account -> account.getSolde().compareTo(BigDecimal.ZERO) != 0);

        if (hasNonZeroBalance) {
            throw new OperationNotAllowedException(
                    "Impossible de supprimer le client: des comptes ont un solde non nul");
        }

        // Supprimer l'utilisateur associé au client (si existe)
        // Cela évite l'erreur de contrainte FK lors de la suppression du client
        userRepository.findByClient(client).ifPresent(user -> {
            log.info("Suppression de l'utilisateur associé - userId: {}", user.getId());
            userRepository.delete(user);
        });

        clientRepository.delete(client);
        log.info("Client supprimé avec succès - id: {}", id);
    }

    private Client findClientById(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client", "id", id));
    }
}
