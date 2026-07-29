package tn.esprit.canvabna.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.canvabna.entity.Client;

import java.util.List;
import java.util.Optional;

/**
 * Repository pour {@link Client}.
 * Fournit les méthodes de recherche utiles pour les services métier.
 */
public interface ClientRepository extends JpaRepository<Client, Long> {

    /**
     * Trouve un client par son identifiant unique (ex: "1042986R").
     */
    Optional<Client> findByIdentifiant(String identifiant);

    /**
     * Vérifie si un identifiant est déjà enregistré.
     */
    boolean existsByIdentifiant(String identifiant);

    /**
     * Recherche par groupe (peut retourner plusieurs clients liés à un même groupe).
     */
    List<Client> findByGroupeContainingIgnoreCase(String groupe);

    /**
     * Recherche par segment de clientèle, ex: "Corporates".
     */
    List<Client> findBySegment(String segment);

    /**
     * Tous les clients rattachés à une structure donnée.
     */
    List<Client> findByStructureId(Long structureId);
}
