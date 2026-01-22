package com.ega.egabank.service;

import java.time.LocalDate;

/**
 * Service pour la génération de relevés de compte
 */
public interface StatementService {

    byte[] generateStatement(String numeroCompte, LocalDate debut, LocalDate fin);
}
