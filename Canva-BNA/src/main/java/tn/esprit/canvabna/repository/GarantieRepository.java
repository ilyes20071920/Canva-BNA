package tn.esprit.canvabna.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.canvabna.entity.Garantie;

import java.util.List;

/**
 * Repository pour {@link Garantie}.
 *
 * <p>Les méthodes de cette interface sont utilisées par {@link tn.esprit.canvabna.service.GarantieService}
 * pour lire les garanties depuis la base de données, et par {@link tn.esprit.canvabna.config.DevGuaranteeSeeder}
 * pour vérifier l'idempotence du seeding.
 */
public interface GarantieRepository extends JpaRepository<Garantie, Long> {

    /**
     * Retourne toutes les garanties associées à un client donné, dans l'ordre d'insertion.
     *
     * @param clientId l'identifiant JPA du client
     * @return liste des garanties (vide si aucune n'existe)
     */
    List<Garantie> findByClientIdOrderById(Long clientId);

    /**
     * Vérifie si au moins une garantie existe pour un client donné.
     * Utilisée par le seeder de développement pour garantir l'idempotence :
     * si des garanties existent déjà, le seeder n'en crée pas de nouvelles.
     *
     * @param clientId l'identifiant JPA du client
     * @return {@code true} si des garanties existent déjà pour ce client
     */
    boolean existsByClientId(Long clientId);
}
