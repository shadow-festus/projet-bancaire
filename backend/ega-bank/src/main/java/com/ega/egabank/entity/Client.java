package com.ega.egabank.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.ega.egabank.enums.Sexe;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entité représentant un client
 */
@Entity
@Table(name = "clients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Client {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String nom;

	private String prenom;

	private LocalDate dateNaissance;

	@Enumerated(EnumType.STRING)
	private Sexe sexe;

	private String adresse;

	private String telephone;

	private String courriel;

	private String nationalite;

	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	@OneToMany(mappedBy = "proprietaire", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private List<Account> comptes = new ArrayList<>();

	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now();
	}

	public String getNomComplet() {
		String p = prenom == null ? "" : prenom.trim();
		String n = nom == null ? "" : nom.trim();
		if (p.isEmpty() && n.isEmpty())
			return "";
		if (p.isEmpty())
			return n;
		if (n.isEmpty())
			return p;
		return p + " " + n;
	}
}
