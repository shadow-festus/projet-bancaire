package com.ega.egabank.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ega.egabank.dto.request.AccountRequest;
import com.ega.egabank.dto.response.AccountResponse;
import com.ega.egabank.dto.response.MessageResponse;
import com.ega.egabank.dto.response.PageResponse;
import com.ega.egabank.service.AccountService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import com.ega.egabank.exception.OperationNotAllowedException;
import com.ega.egabank.repository.UserRepository;
import com.ega.egabank.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.Collections;
import java.util.Collections;

import lombok.RequiredArgsConstructor;

/**
 * Contrôleur pour la gestion des comptes
 */
@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Tag(name = "Comptes", description = "Gestion des comptes bancaires")
public class AccountController {

    private final AccountService accountService;
    private final UserRepository userRepository;

    @Operation(summary = "Récupérer tous les comptes avec pagination")
    @GetMapping
    public ResponseEntity<PageResponse<AccountResponse>> getAllAccounts(
            @Parameter(description = "Numéro de page (commence à 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Taille de la page") @RequestParam(defaultValue = "10") int size) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            return ResponseEntity.ok(accountService.getAllAccounts(page, size));
        } else {
            User user = userRepository.findByUsername(auth.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            if (user.getClient() == null) {
                return ResponseEntity.ok(PageResponse.<AccountResponse>builder()
                        .content(Collections.emptyList())
                        .pageNumber(0)
                        .pageSize(size)
                        .totalElements(0)
                        .totalPages(0)
                        .last(true)
                        .first(true)
                        .build());
            }

            List<AccountResponse> accounts = accountService.getAccountsByClient(user.getClient().getId());

            return ResponseEntity.ok(PageResponse.<AccountResponse>builder()
                    .content(accounts)
                    .pageNumber(0)
                    .pageSize(size)
                    .totalElements(accounts.size())
                    .totalPages(1)
                    .last(true)
                    .first(true)
                    .build());
        }
    }

    @Operation(summary = "Récupérer un compte par son numéro IBAN")
    @GetMapping("/{numeroCompte}")
    public ResponseEntity<AccountResponse> getAccountByNumber(
            @Parameter(description = "Numéro de compte (IBAN)") @PathVariable String numeroCompte) {

        AccountResponse response = accountService.getAccountByNumber(numeroCompte);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            User user = userRepository.findByUsername(auth.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            if (user.getClient() == null || !user.getClient().getId().equals(response.getClientId())) {
                throw new OperationNotAllowedException("Vous n'avez pas accès à ce compte");
            }
        }

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Récupérer les comptes d'un client")
    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<AccountResponse>> getAccountsByClient(
            @Parameter(description = "Identifiant du client") @PathVariable Long clientId) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            User user = userRepository.findByUsername(auth.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            if (user.getClient() == null || !user.getClient().getId().equals(clientId)) {
                throw new OperationNotAllowedException("Accès refusé aux comptes d'un autre client");
            }
        }

        return ResponseEntity.ok(accountService.getAccountsByClient(clientId));
    }

    @Operation(summary = "Créer un nouveau compte")
    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody AccountRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            User user = userRepository.findByUsername(auth.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            if (user.getClient() == null) {
                throw new OperationNotAllowedException("Vous devez avoir un profil client pour créer un compte");
            }

            if (!user.getClient().getId().equals(request.getClientId())) {
                throw new OperationNotAllowedException("Vous ne pouvez pas créer de compte pour un autre client");
            }
        }

        AccountResponse response = accountService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Supprimer un compte")
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteAccount(@PathVariable Long id) {
        accountService.deleteAccount(id);
        return ResponseEntity.ok(MessageResponse.success("Compte supprimé avec succès"));
    }

    @Operation(summary = "Désactiver un compte")
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<MessageResponse> deactivateAccount(@PathVariable Long id) {
        accountService.deactivateAccount(id);
        return ResponseEntity.ok(MessageResponse.success("Compte désactivé avec succès"));
    }
}
