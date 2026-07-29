package tn.esprit.canvabna.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Entité centrale représentant la fiche signalétique d'un client entreprise.
 * Contient les informations générales, la structure du capital social,
 * les comptes bancaires et les actionnaires/associés.
 *
 * <p>Toutes les relations vers les entités enfants utilisent {@code @JsonManagedReference}
 * pour éviter les boucles infinies de sérialisation JSON.
 * Les réponses API exposent uniquement des DTOs — jamais cette entité directement.
 */
@Entity
@Table(name = "clients",
        uniqueConstraints = @UniqueConstraint(columnNames = "identifiant"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Identifiant unique du client, ex: "1042986R". */
    @Column(nullable = false, unique = true)
    private String identifiant;

    /** Groupe auquel appartient le client, ex: "BOUDOKHANE MOKHTAR". */
    @Column(nullable = false)
    private String groupe;

    /** Dénomination sociale / relation bancaire, ex: "STE ENT. BOUDOKHANE MOKHTAR". */
    @Column(nullable = false)
    private String relation;

    @Column
    private String activite;

    /** Segment de clientèle, ex: "Corporates". */
    @Column
    private String segment;

    /** Adresse du siège social, ex: "BLOC 11 CITE OLYMPIQUE TUN 1003". */
    @Column
    private String siegeSocial;

    /** Secteur d'activité, ex: "Sociétés non financières". */
    @Column
    private String secteur;

    /** Date d'entrée en relation bancaire. */
    @Column
    private LocalDate dateEntreeRelation;

    /** Forme juridique, ex: "Societe A Responsabilite Limitee". */
    @Column
    private String formeJuridique;

    /** Capital social en dinars tunisiens (DT). */
    @Column(precision = 15, scale = 3)
    private BigDecimal capitalSocial;

    @Column
    private String agence;

    @Column
    private String directionRegionale;

    /** Structure organisationnelle de référence (optionnel). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "structure_id")
    private Structure structure;

    /** Liste des actionnaires/associés du capital social. */
    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference("client-actionnaires")
    @Builder.Default
    private List<Actionnaire> actionnaires = new ArrayList<>();

    /** Liste des comptes bancaires liés à ce client. */
    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference("client-comptes")
    @Builder.Default
    private List<Compte> comptes = new ArrayList<>();
}
