package com.ega.egabank.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ega.egabank.service.StatementService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import com.ega.egabank.service.AccountService;
import com.ega.egabank.dto.response.AccountResponse;
import com.ega.egabank.repository.UserRepository;
import com.ega.egabank.entity.User;
import com.ega.egabank.exception.OperationNotAllowedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Contrôleur pour les relevés de compte
 */
@RestController
@RequestMapping("/api/statements")
@RequiredArgsConstructor
@Tag(name = "Relevés", description = "Génération et téléchargement des relevés de compte")
public class StatementController {

        private final StatementService statementService;
        private final AccountService accountService;
        private final UserRepository userRepository;

        @Operation(summary = "Télécharger le relevé de compte en PDF")
        @GetMapping("/{numeroCompte}")
        public ResponseEntity<byte[]> downloadStatement(
                        @Parameter(description = "Numéro de compte (IBAN)") @PathVariable String numeroCompte,
                        @Parameter(description = "Date de début (format: yyyy-MM-dd)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate debut,
                        @Parameter(description = "Date de fin (format: yyyy-MM-dd)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {

                // Vérification de sécurité
                AccountResponse account = accountService.getAccountByNumber(numeroCompte);
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                boolean isAdmin = auth.getAuthorities().stream()
                                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

                if (!isAdmin) {
                        User user = userRepository.findByUsername(auth.getName())
                                        .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

                        if (user.getClient() == null || !user.getClient().getId().equals(account.getClientId())) {
                                throw new OperationNotAllowedException("Vous n'avez pas accès à ce compte");
                        }
                }

                byte[] pdf = statementService.generateStatement(numeroCompte, debut, fin);

                String filename = String.format("releve_%s_%s_%s.pdf",
                                numeroCompte.substring(0, 8),
                                debut.toString(),
                                fin.toString());

                return ResponseEntity.ok()
                                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                                .contentType(MediaType.APPLICATION_PDF)
                                .contentLength(pdf.length)
                                .body(pdf);
        }
}
