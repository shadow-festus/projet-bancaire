package com.ega.egabank.config;

import com.ega.egabank.entity.Client;
import com.ega.egabank.entity.User;
import com.ega.egabank.enums.Role;
import com.ega.egabank.enums.Sexe;
import com.ega.egabank.repository.ClientRepository;
import com.ega.egabank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            log.info("Initialisation des données par défaut...");

            // Création d'un administrateur
            User admin = User.builder()
                    .username("admin")
                    .email("admin@egabank.com")
                    .password(passwordEncoder.encode("admin1234"))
                    .role(Role.ROLE_ADMIN)
                    .enabled(true)
                    .build();
            userRepository.save(admin);
            log.info("Utilisateur admin créé (admin / admin1234)");

            // Création de quelques clients de test avec comptes utilisateurs associés
            Client jean = createClient("Jean", "Dupont", "jean.dupont@email.com", "+22890000001", "Lomé, Togo",
                    Sexe.MASCULIN);
            createUserForClient(jean, "jean", "jean123");
            log.info("Client Jean créé avec utilisateur (jean / jean123)");

            Client marie = createClient("Marie", "Curie", "marie.curie@email.com", "+22890000002", "Kara, Togo",
                    Sexe.FEMININ);
            createUserForClient(marie, "marie", "marie123");
            log.info("Client Marie créé avec utilisateur (marie / marie123)");

            log.info("Données initialisées avec succès !");
        }
    }

    private Client createClient(String prenom, String nom, String email, String telephone, String adresse, Sexe sexe) {
        Client client = Client.builder()
                .prenom(prenom)
                .nom(nom)
                .courriel(email)
                .telephone(telephone)
                .adresse(adresse)
                .dateNaissance(LocalDate.of(1990, 1, 1))
                .sexe(sexe)
                .nationalite("Togolaise")
                .build();
        return clientRepository.save(client);
    }

    private void createUserForClient(Client client, String username, String password) {
        User user = User.builder()
                .username(username)
                .email(client.getCourriel())
                .password(passwordEncoder.encode(password))
                .role(Role.ROLE_USER)
                .enabled(true)
                .client(client)
                .build();
        userRepository.save(user);
    }
}
