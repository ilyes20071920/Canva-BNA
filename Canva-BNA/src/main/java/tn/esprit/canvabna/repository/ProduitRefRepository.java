package tn.esprit.canvabna.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.canvabna.entity.ProduitRef;

import java.util.Optional;

@Repository
public interface ProduitRefRepository extends JpaRepository<ProduitRef, Long> {
    Optional<ProduitRef> findByCode(String code);
    boolean existsByCode(String code);
}
