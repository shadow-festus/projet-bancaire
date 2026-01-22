package com.ega.egabank.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.ega.egabank.enums.TypeTransaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de réponse pour une transaction
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {

    private Long id;
    private TypeTransaction type;
    private String typeLibelle;
    private BigDecimal montant;
    private LocalDateTime dateTransaction;
    private String description;
    private String compteDestination;
    private BigDecimal soldeAvant;
    private BigDecimal soldeApres;
    private String numeroCompte;
}
