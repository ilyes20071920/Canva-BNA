package tn.esprit.canvabna.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.canvabna.entity.Sollicitation;

import java.util.List;
import java.util.Optional;

@Repository
public interface SollicitationRepository extends JpaRepository<Sollicitation, Long> {
    Optional<Sollicitation> findFirstByClientIdOrderByIdDesc(Long clientId);

    List<Sollicitation> findAllByClientId(Long clientId);

    boolean existsByClientId(Long clientId);
}
