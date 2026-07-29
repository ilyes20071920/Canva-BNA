package tn.esprit.canvabna.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * Représente un mandataire autorisé sur un {@link Compte} bancaire.
 * Correspond à la liste des mandats visible dans l'onglet "Dossier Juridique".
 */
@Entity
@Table(name = "mandataires")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Mandataire {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Numéro du mandat, ex: "1068706". */
    @Column
    private String numMandat;

    /** Numéro de demande, ex: "834-202200157". */
    @Column
    private String numDemande;

    /** Type de mandat, ex: "General". */
    @Column
    private String typeMandat;

    /** Code agence, ex: "146". */
    @Column
    private String agence;

    /** Nom du mandant (personne habilitée), ex: "BOUDOKHANE MAJDI". */
    @Column
    private String mandant;

    /** Date de création du mandat. */
    @Column
    private LocalDate dateCreation;

    /** Date de prise d'effet du mandat. */
    @Column
    private LocalDate dateDebut;

    /** Date d'expiration du mandat. */
    @Column
    private LocalDate dateFin;

    /** Compte bancaire sur lequel ce mandat s'applique. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name = "compte_code_guichet", referencedColumnName = "code_guichet"),
        @JoinColumn(name = "compte_code_produit", referencedColumnName = "code_produit"),
        @JoinColumn(name = "compte_num_compte", referencedColumnName = "num_compte")
    })
    @JsonBackReference("compte-mandataires")
    private Compte compte;
}
