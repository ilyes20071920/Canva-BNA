package tn.esprit.canvabna.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.canvabna.entity.Actionnaire;

import java.util.List;

/**
 * Repository pour {@link Actionnaire}.
 */
public interface ActionnaireRepository extends JpaRepository<Actionnaire, Long> {

    /**
     * Retourne tous les actionnaires d'un client donné.
     */
    List<Actionnaire> findByClientId(Long clientId);
}
