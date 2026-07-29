package tn.esprit.canvabna.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.canvabna.entity.Mandataire;

import java.util.List;

/**
 * Repository pour {@link Mandataire}.
 */
public interface MandataireRepository extends JpaRepository<Mandataire, Long> {

    /**
     * Retourne tous les mandataires d'un compte donné identifié par sa clé composite.
     */
    List<Mandataire> findByCompte_Id_CodeGuichetAndCompte_Id_CodeProduitAndCompte_Id_NumCompte(
            String codeGuichet, String codeProduit, String numCompte);
}
