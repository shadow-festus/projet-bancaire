package com.ega.egabank.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ega.egabank.dto.request.OperationRequest;
import com.ega.egabank.dto.request.TransferRequest;
import com.ega.egabank.dto.response.TransactionResponse;
import com.ega.egabank.service.TransactionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import com.ega.egabank.repository.UserRepository;
import com.ega.egabank.repository.AccountRepository;
import com.ega.egabank.entity.User;
import com.ega.egabank.entity.Account;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.Collections;
import java.util.ArrayList;
import com.ega.egabank.exception.OperationNotAllowedException;

import lombok.RequiredArgsConstructor;

/**
 * Contrôleur pour les opérations bancaires
 */
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Opérations bancaires (dépôt, retrait, virement)")
public class TransactionController {

    private final TransactionService transactionService;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    @Operation(summary = "Effectuer un dépôt sur un compte")
    @PostMapping("/{numeroCompte}/deposit")
    public ResponseEntity<TransactionResponse> deposit(
            @Parameter(description = "Numéro de compte (IBAN)") @PathVariable String numeroCompte,
            @Valid @RequestBody OperationRequest request) {

        checkAccountOwnership(numeroCompte);

        TransactionResponse response = transactionService.deposit(numeroCompte, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Effectuer un retrait sur un compte")
    @PostMapping("/{numeroCompte}/withdraw")
    public ResponseEntity<TransactionResponse> withdraw(
            @Parameter(description = "Numéro de compte (IBAN)") @PathVariable String numeroCompte,
            @Valid @RequestBody OperationRequest request) {

        checkAccountOwnership(numeroCompte);

        TransactionResponse response = transactionService.withdraw(numeroCompte, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Effectuer un virement entre deux comptes")
    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponse> transfer(@Valid @RequestBody TransferRequest request) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            User user = userRepository.findByUsername(auth.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            if (user.getClient() == null) {
                throw new OperationNotAllowedException("Vous n'avez pas de profil client associé");
            }

            Account sourceAccount = accountRepository.findByNumeroCompteWithClient(request.getCompteSource())
                    .orElseThrow(() -> new RuntimeException("Compte source non trouvé"));

            if (!sourceAccount.getProprietaire().getId().equals(user.getClient().getId())) {
                throw new OperationNotAllowedException(
                        "Vous ne pouvez pas effectuer de virement depuis un compte qui ne vous appartient pas");
            }
        }

        TransactionResponse response = transactionService.transfer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Récupérer toutes les transactions de tous les comptes")
    @GetMapping
    public ResponseEntity<List<TransactionResponse>> getAllTransactions() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            return ResponseEntity.ok(transactionService.getAllTransactions());
        }

        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (user.getClient() == null) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        // Récupérer les transactions de tous les comptes du client
        List<Account> accounts = accountRepository.findByProprietaireId(user.getClient().getId());
        List<TransactionResponse> allTransactions = new ArrayList<>();
        for (Account acc : accounts) {
            allTransactions.addAll(transactionService.getAllTransactionsByAccount(acc.getNumeroCompte()));
        }

        return ResponseEntity.ok(allTransactions);
    }

    @Operation(summary = "Récupérer l'historique des transactions d'un compte sur une période")
    @GetMapping("/{numeroCompte}/history")
    public ResponseEntity<List<TransactionResponse>> getTransactionHistory(
            @Parameter(description = "Numéro de compte (IBAN)") @PathVariable String numeroCompte,
            @Parameter(description = "Date de début (format: yyyy-MM-dd)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate debut,
            @Parameter(description = "Date de fin (format: yyyy-MM-dd)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {
        return ResponseEntity.ok(transactionService.getTransactionHistory(numeroCompte, debut, fin));
    }

    @Operation(summary = "Récupérer toutes les transactions d'un compte")
    @GetMapping("/{numeroCompte}")
    public ResponseEntity<List<TransactionResponse>> getAllTransactions(
            @PathVariable String numeroCompte) {

        // Vérification de sécurité
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            User user = userRepository.findByUsername(auth.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            Account account = accountRepository.findByNumeroCompteWithClient(numeroCompte)
                    .orElseThrow(() -> new RuntimeException("Compte non trouvé"));

            if (user.getClient() == null || !account.getProprietaire().getId().equals(user.getClient().getId())) {
                throw new OperationNotAllowedException("Vous n'avez pas accès à ce compte");
            }
        }

        return ResponseEntity.ok(transactionService.getAllTransactionsByAccount(numeroCompte));
    }

    private void checkAccountOwnership(String numeroCompte) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            User user = userRepository.findByUsername(auth.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            Account account = accountRepository.findByNumeroCompteWithClient(numeroCompte)
                    .orElseThrow(() -> new RuntimeException("Compte non trouvé"));

            if (user.getClient() == null || !account.getProprietaire().getId().equals(user.getClient().getId())) {
                throw new OperationNotAllowedException("Vous n'avez pas accès à ce compte");
            }
        }
    }
}
