package com.ega.egabank.service;

import java.time.LocalDate;
import java.util.List;

import com.ega.egabank.dto.request.OperationRequest;
import com.ega.egabank.dto.request.TransferRequest;
import com.ega.egabank.dto.response.TransactionResponse;

/**
 * Service pour les opérations bancaires
 */
public interface TransactionService {

    TransactionResponse deposit(String numeroCompte, OperationRequest request);

    TransactionResponse withdraw(String numeroCompte, OperationRequest request);

    TransactionResponse transfer(TransferRequest request);

    List<TransactionResponse> getTransactionHistory(String numeroCompte, LocalDate debut, LocalDate fin);

    List<TransactionResponse> getAllTransactionsByAccount(String numeroCompte);

    /**
     * Récupère toutes les transactions de tous les comptes
     */
    List<TransactionResponse> getAllTransactions();
}
