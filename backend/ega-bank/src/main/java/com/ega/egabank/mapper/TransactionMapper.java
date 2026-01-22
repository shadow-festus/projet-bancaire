package com.ega.egabank.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.ega.egabank.dto.response.TransactionResponse;
import com.ega.egabank.entity.Transaction;

/**
 * Mapper pour les entités Transaction
 */
@Component
public class TransactionMapper {

    /**
     * Convertit une entité Transaction en TransactionResponse
     */
    public TransactionResponse toResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .type(transaction.getType())
                .typeLibelle(transaction.getType().getLibelle())
                .montant(transaction.getMontant())
                .dateTransaction(transaction.getDateTransaction())
                .description(transaction.getDescription())
                .compteDestination(transaction.getCompteDestination())
                .soldeAvant(transaction.getSoldeAvant())
                .soldeApres(transaction.getSoldeApres())
                .numeroCompte(transaction.getCompte() != null
                        ? transaction.getCompte().getNumeroCompte()
                        : null)
                .build();
    }

    /**
     * Convertit une liste de Transaction en liste de TransactionResponse
     */
    public List<TransactionResponse> toResponseList(List<Transaction> transactions) {
        return transactions.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
