package com.ega.egabank.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ega.egabank.entity.Transaction;
import com.ega.egabank.enums.TypeTransaction;

/**
 * Repository pour la gestion des transactions
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByCompteIdOrderByDateTransactionDesc(Long compteId);

    Page<Transaction> findByCompteId(Long compteId, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.compte.numeroCompte = :numero " +
            "AND t.dateTransaction BETWEEN :debut AND :fin " +
            "ORDER BY t.dateTransaction DESC")
    List<Transaction> findByCompteAndPeriod(
            @Param("numero") String numeroCompte,
            @Param("debut") LocalDateTime debut,
            @Param("fin") LocalDateTime fin);

    @Query("SELECT t FROM Transaction t WHERE t.compte.numeroCompte = :numero " +
            "ORDER BY t.dateTransaction DESC")
    Page<Transaction> findByNumeroCompte(@Param("numero") String numeroCompte, Pageable pageable);

    List<Transaction> findByType(TypeTransaction type);

    @Query("SELECT SUM(t.montant) FROM Transaction t WHERE t.compte.id = :compteId AND t.type = :type")
    java.math.BigDecimal sumByCompteAndType(
            @Param("compteId") Long compteId,
            @Param("type") TypeTransaction type);

    /**
     * Récupère toutes les transactions triées par date décroissante
     */
    List<Transaction> findAllByOrderByDateTransactionDesc();

    /**
     * Récupère toutes les transactions avec pagination
     */
    Page<Transaction> findAllByOrderByDateTransactionDesc(Pageable pageable);
}
