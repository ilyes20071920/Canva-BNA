package tn.esprit.canvabna.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.canvabna.entity.User;

import java.util.Optional;

/**
 * Repository for {@link User} entities.
 * Provides query methods needed by the authentication layer.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by their unique matricule (login identifier).
     *
     * @param matricule the matricule to search for
     * @return an Optional containing the user if found
     */
    Optional<User> findByMatricule(Integer matricule);

    /**
     * Checks whether a matricule is already registered.
     *
     * @param matricule the matricule to check
     * @return true if the matricule exists
     */
    boolean existsByMatricule(Integer matricule);
}
