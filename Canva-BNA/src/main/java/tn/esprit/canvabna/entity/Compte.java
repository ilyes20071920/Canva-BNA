package tn.esprit.canvabna.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Représente un compte bancaire appartenant à un {@link Client}.
 * Un compte a une clé composite (codeGuichet, codeProduit, numCompte).
 * Un compte peut avoir plusieurs {@link Mandataire} associés.
 */
@Entity
@Table(name = "comptes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Compte {

    @EmbeddedId
    private CompteId id;

    @Column
    private String agence;

    /** Client propriétaire de ce compte. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    @JsonBackReference("client-comptes")
    private Client client;

    /** Liste des mandataires autorisés sur ce compte. */
    @OneToMany(mappedBy = "compte", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference("compte-mandataires")
    @Builder.Default
    private List<Mandataire> mandataires = new ArrayList<>();
}
