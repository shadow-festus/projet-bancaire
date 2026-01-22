package com.ega.egabank.service;

import java.util.List;

import com.ega.egabank.dto.request.AccountRequest;
import com.ega.egabank.dto.response.AccountResponse;
import com.ega.egabank.dto.response.PageResponse;

/**
 * Service pour la gestion des comptes bancaires
 */
public interface AccountService {

    PageResponse<AccountResponse> getAllAccounts(int page, int size);

    AccountResponse getAccountByNumber(String numeroCompte);

    List<AccountResponse> getAccountsByClient(Long clientId);

    AccountResponse createAccount(AccountRequest request);

    void deleteAccount(Long id);

    void deactivateAccount(Long id);
}
