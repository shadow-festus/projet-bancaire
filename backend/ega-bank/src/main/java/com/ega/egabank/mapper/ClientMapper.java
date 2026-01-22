package com.ega.egabank.mapper;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.ega.egabank.dto.request.ClientRequest;
import com.ega.egabank.dto.response.AccountResponse;
import com.ega.egabank.dto.response.ClientResponse;
import com.ega.egabank.entity.Client;

/**
 * Mapper pour les entités Client
 */
@Component
public class ClientMapper {

    private final AccountMapper accountMapper;

    public ClientMapper(AccountMapper accountMapper) {
        this.accountMapper = accountMapper;
    }

    /**
     * Convertit un ClientRequest en entité Client
     */
    public Client toEntity(ClientRequest request) {
        return Client.builder()
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .dateNaissance(request.getDateNaissance())
                .sexe(request.getSexe())
                .adresse(request.getAdresse())
                .telephone(request.getTelephone())
                .courriel(request.getCourriel())
                .nationalite(request.getNationalite())
                .build();
    }

    /**
     * Met à jour une entité Client avec les données du ClientRequest
     */
    public void updateEntity(Client client, ClientRequest request) {
        client.setNom(request.getNom());
        client.setPrenom(request.getPrenom());
        client.setDateNaissance(request.getDateNaissance());
        client.setSexe(request.getSexe());
        client.setAdresse(request.getAdresse());
        client.setTelephone(request.getTelephone());
        client.setCourriel(request.getCourriel());
        client.setNationalite(request.getNationalite());
    }

    /**
     * Convertit une entité Client en ClientResponse (sans les comptes)
     */
    public ClientResponse toResponse(Client client) {
        return ClientResponse.builder()
                .id(client.getId())
                .nom(client.getNom())
                .prenom(client.getPrenom())
                .nomComplet(client.getNomComplet())
                .dateNaissance(client.getDateNaissance())
                .sexe(client.getSexe())
                .adresse(client.getAdresse())
                .telephone(client.getTelephone())
                .courriel(client.getCourriel())
                .nationalite(client.getNationalite())
                .createdAt(client.getCreatedAt())
                .nombreComptes(client.getComptes() != null ? client.getComptes().size() : 0)
                .build();
    }

    /**
     * Convertit une entité Client en ClientResponse (avec les comptes)
     */
    public ClientResponse toResponseWithAccounts(Client client) {
        List<AccountResponse> comptes = client.getComptes() != null
                ? client.getComptes().stream()
                        .map(accountMapper::toResponse)
                        .collect(Collectors.toList())
                : Collections.emptyList();

        return ClientResponse.builder()
                .id(client.getId())
                .nom(client.getNom())
                .prenom(client.getPrenom())
                .nomComplet(client.getNomComplet())
                .dateNaissance(client.getDateNaissance())
                .sexe(client.getSexe())
                .adresse(client.getAdresse())
                .telephone(client.getTelephone())
                .courriel(client.getCourriel())
                .nationalite(client.getNationalite())
                .createdAt(client.getCreatedAt())
                .nombreComptes(comptes.size())
                .comptes(comptes)
                .build();
    }

    /**
     * Convertit une liste de Client en liste de ClientResponse
     */
    public List<ClientResponse> toResponseList(List<Client> clients) {
        return clients.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
