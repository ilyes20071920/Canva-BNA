package tn.esprit.canvabna.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.canvabna.entity.Structure;

import java.util.Optional;

/**
 * Repository pour {@link Structure}.
 */
public interface StructureRepository extends JpaRepository<Structure, Long> {

    Optional<Structure> findByCode(String code);

    boolean existsByCode(String code);
}
