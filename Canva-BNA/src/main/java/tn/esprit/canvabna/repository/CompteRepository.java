package tn.esprit.canvabna.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.canvabna.entity.Compte;
import tn.esprit.canvabna.entity.CompteId;

import java.util.List;
import java.util.Optional;

/**
 * Repository pour {@link Compte}.
 */
public interface CompteRepository extends JpaRepository<Compte, CompteId> {

    /**
     * Retourne tous les comptes d'un client donné.
     */
    List<Compte> findByClientId(Long clientId);

    /**
     * Trouve un compte par les attributs de sa clé composite.
     */
    Optional<Compte> findById_CodeGuichetAndId_CodeProduitAndId_NumCompte(
            String codeGuichet, String codeProduit, String numCompte);

    /**
     * Vérifie si un numéro de compte est déjà enregistré par sa clé composite.
     */
    boolean existsById_CodeGuichetAndId_CodeProduitAndId_NumCompte(
            String codeGuichet, String codeProduit, String numCompte);
}
