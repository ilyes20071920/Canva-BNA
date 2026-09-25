package tn.esprit.canvabna.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.canvabna.entity.ObjetCreditRef;

import java.util.Optional;

@Repository
public interface ObjetCreditRefRepository extends JpaRepository<ObjetCreditRef, Long> {
    Optional<ObjetCreditRef> findByCode(String code);
    boolean existsByCode(String code);
}
