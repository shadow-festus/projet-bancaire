package com.ega.egabank.service;

import com.ega.egabank.dto.request.ClientRequest;
import com.ega.egabank.dto.response.ClientResponse;
import com.ega.egabank.dto.response.PageResponse;

/**
 * Service pour la gestion des clients
 */
public interface ClientService {

    PageResponse<ClientResponse> getAllClients(int page, int size);

    PageResponse<ClientResponse> searchClients(String search, int page, int size);

    ClientResponse getClientById(Long id);

    ClientResponse getClientWithAccounts(Long id);

    ClientResponse createClient(ClientRequest request);

    ClientResponse updateClient(Long id, ClientRequest request);

    void deleteClient(Long id);
}
