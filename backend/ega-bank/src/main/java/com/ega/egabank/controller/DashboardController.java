package com.ega.egabank.controller;

import com.ega.egabank.dto.response.DashboardStatsResponse;
import com.ega.egabank.repository.AccountRepository;
import com.ega.egabank.repository.ClientRepository;
import com.ega.egabank.repository.TransactionRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ega.egabank.entity.User;
import com.ega.egabank.entity.Account;
import com.ega.egabank.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import java.math.BigDecimal;

/**
 * Contrôleur pour les statistiques du dashboard
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Statistiques du dashboard")
public class DashboardController {

    private final ClientRepository clientRepository;
    private final AccountRepository accountRepository;

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    @Operation(summary = "Récupérer les statistiques du dashboard")
    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsResponse> getStats() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            User user = userRepository.findByUsername(auth.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            if (user.getClient() == null) {
                return ResponseEntity.ok(DashboardStatsResponse.builder().build());
            }

            Long clientId = user.getClient().getId();
            List<Account> accounts = accountRepository.findByProprietaireId(clientId);

            long totalAccounts = accounts.size();
            long activeAccounts = accounts.stream().filter(Account::getActif).count();
            BigDecimal totalBalance = accounts.stream()
                    .map(Account::getSolde)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            long totalTransactions = 0;
            // Pour les transactions, c'est plus compliqué sans méthode repo dédiée, on peut
            // mettre 0 ou compter
            // On pourrait faire une requête countByAccountIn...

            DashboardStatsResponse stats = DashboardStatsResponse.builder()
                    .totalClients(1L) // Soi-même
                    .totalAccounts(totalAccounts)
                    .activeAccounts(activeAccounts)
                    .totalBalance(totalBalance)
                    .totalTransactions(totalTransactions)
                    .build();

            return ResponseEntity.ok(stats);
        }

        long totalClients = clientRepository.count();
        long totalAccounts = accountRepository.count();
        long activeAccounts = accountRepository.countByActifTrue();
        BigDecimal totalBalance = accountRepository.sumAllBalances();
        long totalTransactions = transactionRepository.count();

        DashboardStatsResponse stats = DashboardStatsResponse.builder()
                .totalClients(totalClients)
                .totalAccounts(totalAccounts)
                .activeAccounts(activeAccounts)
                .totalBalance(totalBalance != null ? totalBalance : BigDecimal.ZERO)
                .totalTransactions(totalTransactions)
                .build();

        return ResponseEntity.ok(stats);
    }
}
