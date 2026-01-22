package com.ega.egabank.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.ega.egabank.enums.TypeTransaction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entité représentant une transaction bancaire
 */
@Entity
@Table(name = "transactions", indexes = {
        @Index(name = "idx_transaction_date", columnList = "date_transaction"),
        @Index(name = "idx_transaction_compte", columnList = "account_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Le type de transaction est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(name = "type_transaction", nullable = false, length = 20)
    private TypeTransaction type;

    @NotNull(message = "Le montant est obligatoire")
    @Positive(message = "Le montant doit être positif")
    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal montant;

    @Column(name = "date_transaction", nullable = false, updatable = false)
    private LocalDateTime dateTransaction;

    @Column(length = 255)
    private String description;

    @Column(name = "compte_destination", length = 34)
    private String compteDestination;

    @Column(name = "solde_avant", precision = 19, scale = 2)
    private BigDecimal soldeAvant;

    @Column(name = "solde_apres", precision = 19, scale = 2)
    private BigDecimal soldeApres;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account compte;

    @PrePersist
    protected void onCreate() {
        dateTransaction = LocalDateTime.now();
    }

    /**
     * Retourne une description formatée de la transaction
     */
    public String getDescriptionComplete() {
        StringBuilder sb = new StringBuilder();
        sb.append(type.getLibelle());
        if (description != null && !description.isEmpty()) {
            sb.append(" - ").append(description);
        }
        if (compteDestination != null) {
            sb.append(" (vers ").append(compteDestination).append(")");
        }
        return sb.toString();
    }
}
