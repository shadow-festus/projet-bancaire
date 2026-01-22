package com.ega.egabank.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ega.egabank.entity.Client;

/**
 * Repository pour la gestion des clients
 */
@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

    Optional<Client> findByCourriel(String courriel);

    boolean existsByCourriel(String courriel);

    @Query("SELECT c FROM Client c WHERE " +
            "LOWER(c.nom) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(c.prenom) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(c.courriel) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Client> search(@Param("search") String search, Pageable pageable);

    @Query("SELECT c FROM Client c LEFT JOIN FETCH c.comptes WHERE c.id = :id")
    Optional<Client> findByIdWithAccounts(@Param("id") Long id);
}
