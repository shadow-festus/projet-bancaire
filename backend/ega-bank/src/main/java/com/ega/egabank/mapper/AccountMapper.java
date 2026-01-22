package com.ega.egabank.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.ega.egabank.dto.response.AccountResponse;
import com.ega.egabank.entity.Account;

/**
 * Mapper pour les entités Account
 */
@Component
public class AccountMapper {

    /**
     * Convertit une entité Account en AccountResponse
     */
    public AccountResponse toResponse(Account account) {
        return AccountResponse.builder()
                .id(account.getId())
                .numeroCompte(account.getNumeroCompte())
                .typeCompte(account.getTypeCompte())
                .typeCompteLibelle(account.getTypeCompte().getLibelle())
                .dateCreation(account.getDateCreation())
                .solde(account.getSolde())
                .actif(account.getActif())
                .clientId(account.getProprietaire() != null ? account.getProprietaire().getId() : null)
                .clientNomComplet(account.getProprietaire() != null
                        ? account.getProprietaire().getNomComplet()
                        : null)
                .build();
    }

    /**
     * Convertit une liste de Account en liste de AccountResponse
     */
    public List<AccountResponse> toResponseList(List<Account> accounts) {
        return accounts.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
