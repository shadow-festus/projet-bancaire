package com.ega.egabank.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.ega.egabank.enums.TypeCompte;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entité représentant un compte bancaire
 */
@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_compte", unique = true, nullable = false, length = 34)
    private String numeroCompte;

    @NotNull(message = "Le type de compte est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(name = "type_compte", nullable = false, length = 20)
    private TypeCompte typeCompte;

    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @Column(precision = 19, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal solde = BigDecimal.ZERO;

    @Column(nullable = false)
    @Builder.Default
    private Boolean actif = true;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client proprietaire;

    @OneToMany(mappedBy = "compte", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("dateTransaction DESC")
    @Builder.Default
    private List<Transaction> transactions = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        dateCreation = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (solde == null) {
            solde = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Crédite le compte du montant spécifié
     */
    public void crediter(BigDecimal montant) {
        this.solde = this.solde.add(montant);
    }

    /**
     * Débite le compte du montant spécifié
     */
    public void debiter(BigDecimal montant) {
        this.solde = this.solde.subtract(montant);
    }

    /**
     * Vérifie si le solde est suffisant pour un retrait
     */
    public boolean soldeEstSuffisant(BigDecimal montant) {
        return this.solde.compareTo(montant) >= 0;
    }
}
