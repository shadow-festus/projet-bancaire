package com.ega.egabank.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ega.egabank.dto.request.OperationRequest;
import com.ega.egabank.dto.request.TransferRequest;
import com.ega.egabank.dto.response.TransactionResponse;
import com.ega.egabank.entity.Account;
import com.ega.egabank.entity.Transaction;
import com.ega.egabank.enums.TypeTransaction;
import com.ega.egabank.exception.InsufficientBalanceException;
import com.ega.egabank.exception.OperationNotAllowedException;
import com.ega.egabank.exception.ResourceNotFoundException;
import com.ega.egabank.mapper.TransactionMapper;
import com.ega.egabank.repository.AccountRepository;
import com.ega.egabank.repository.TransactionRepository;
import com.ega.egabank.service.TransactionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implémentation du service des opérations bancaires
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TransactionServiceImpl implements TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;

    @Override
    public TransactionResponse deposit(String numeroCompte, OperationRequest request) {
        log.info("Dépôt de {} sur le compte {}", request.getMontant(), numeroCompte);

        Account account = findActiveAccount(numeroCompte);
        BigDecimal soldeAvant = account.getSolde();

        // Effectuer le dépôt
        account.crediter(request.getMontant());

        // Créer la transaction
        Transaction transaction = Transaction.builder()
                .type(TypeTransaction.DEPOT)
                .montant(request.getMontant())
                .description(request.getDescription() != null ? request.getDescription() : "Dépôt")
                .soldeAvant(soldeAvant)
                .soldeApres(account.getSolde())
                .compte(account)
                .build();

        accountRepository.save(account);
        transaction = transactionRepository.save(transaction);

        log.info("Dépôt effectué - nouveau solde: {}", account.getSolde());
        return transactionMapper.toResponse(transaction);
    }

    @Override
    public TransactionResponse withdraw(String numeroCompte, OperationRequest request) {
        log.info("Retrait de {} sur le compte {}", request.getMontant(), numeroCompte);

        Account account = findActiveAccount(numeroCompte);
        BigDecimal soldeAvant = account.getSolde();

        // Vérifier le solde
        if (!account.soldeEstSuffisant(request.getMontant())) {
            throw new InsufficientBalanceException(numeroCompte, account.getSolde(), request.getMontant());
        }

        // Effectuer le retrait
        account.debiter(request.getMontant());

        // Créer la transaction
        Transaction transaction = Transaction.builder()
                .type(TypeTransaction.RETRAIT)
                .montant(request.getMontant())
                .description(request.getDescription() != null ? request.getDescription() : "Retrait")
                .soldeAvant(soldeAvant)
                .soldeApres(account.getSolde())
                .compte(account)
                .build();

        accountRepository.save(account);
        transaction = transactionRepository.save(transaction);

        log.info("Retrait effectué - nouveau solde: {}", account.getSolde());
        return transactionMapper.toResponse(transaction);
    }

    @Override
    public TransactionResponse transfer(TransferRequest request) {
        log.info("Virement de {} de {} vers {}",
                request.getMontant(), request.getCompteSource(), request.getCompteDestination());

        // Vérifier que source et destination sont différents
        if (request.getCompteSource().equals(request.getCompteDestination())) {
            throw new OperationNotAllowedException("Le compte source et destination ne peuvent pas être identiques");
        }

        Account compteSource = findActiveAccount(request.getCompteSource());
        Account compteDestination = findActiveAccount(request.getCompteDestination());

        // Vérifier le solde du compte source
        if (!compteSource.soldeEstSuffisant(request.getMontant())) {
            throw new InsufficientBalanceException(
                    request.getCompteSource(), compteSource.getSolde(), request.getMontant());
        }

        BigDecimal soldeAvantSource = compteSource.getSolde();
        BigDecimal soldeAvantDest = compteDestination.getSolde();

        // Effectuer le virement
        compteSource.debiter(request.getMontant());
        compteDestination.crediter(request.getMontant());

        String description = request.getDescription() != null
                ? request.getDescription()
                : "Virement";

        // Créer la transaction sortante
        Transaction transactionSortante = Transaction.builder()
                .type(TypeTransaction.VIREMENT_SORTANT)
                .montant(request.getMontant())
                .description(description)
                .compteDestination(request.getCompteDestination())
                .soldeAvant(soldeAvantSource)
                .soldeApres(compteSource.getSolde())
                .compte(compteSource)
                .build();

        // Créer la transaction entrante
        Transaction transactionEntrante = Transaction.builder()
                .type(TypeTransaction.VIREMENT_ENTRANT)
                .montant(request.getMontant())
                .description(description)
                .compteDestination(request.getCompteSource()) // compte d'origine
                .soldeAvant(soldeAvantDest)
                .soldeApres(compteDestination.getSolde())
                .compte(compteDestination)
                .build();

        accountRepository.save(compteSource);
        accountRepository.save(compteDestination);
        transactionSortante = transactionRepository.save(transactionSortante);
        transactionRepository.save(transactionEntrante);

        log.info("Virement effectué - source: {}, destination: {}",
                compteSource.getSolde(), compteDestination.getSolde());

        return transactionMapper.toResponse(transactionSortante);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionHistory(String numeroCompte, LocalDate debut, LocalDate fin) {
        log.debug("Historique des transactions pour {} du {} au {}", numeroCompte, debut, fin);

        // Vérifier que le compte existe
        if (!accountRepository.existsByNumeroCompte(numeroCompte)) {
            throw new ResourceNotFoundException("Compte", "numéro", numeroCompte);
        }

        LocalDateTime debutDateTime = debut.atStartOfDay();
        LocalDateTime finDateTime = fin.atTime(LocalTime.MAX);

        List<Transaction> transactions = transactionRepository.findByCompteAndPeriod(
                numeroCompte, debutDateTime, finDateTime);

        return transactionMapper.toResponseList(transactions);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getAllTransactionsByAccount(String numeroCompte) {
        log.debug("Récupération de toutes les transactions pour {}", numeroCompte);

        Account account = accountRepository.findByNumeroCompte(numeroCompte)
                .orElseThrow(() -> new ResourceNotFoundException("Compte", "numéro", numeroCompte));

        List<Transaction> transactions = transactionRepository
                .findByCompteIdOrderByDateTransactionDesc(account.getId());

        return transactionMapper.toResponseList(transactions);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getAllTransactions() {
        log.debug("Récupération de toutes les transactions");
        List<Transaction> transactions = transactionRepository.findAllByOrderByDateTransactionDesc();
        return transactionMapper.toResponseList(transactions);
    }

    private Account findActiveAccount(String numeroCompte) {
        Account account = accountRepository.findByNumeroCompte(numeroCompte)
                .orElseThrow(() -> new ResourceNotFoundException("Compte", "numéro", numeroCompte));

        if (!account.getActif()) {
            throw new OperationNotAllowedException("Le compte " + numeroCompte + " est inactif");
        }

        return account;
    }
}
