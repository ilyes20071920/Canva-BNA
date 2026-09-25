package tn.esprit.canvabna.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entité représentant une garantie détenue par la banque en possession du client.
 *
 * <p>Chaque garantie est directement associée à un {@link Client} (relation ManyToOne).
 * Il n'existe pas encore d'entité {@code Canevas} ni {@code Credit} formelle dans ce projet ;
 * les champs {@code idCredit} et {@code affectation} sont donc stockés en tant que chaînes
 * simples faisant référence aux crédits mock générés dynamiquement dans {@code ClientService}.
 *
 * <p>Les totaux dérivés ("Total Charges Inscrites", "Dont BNA") ne sont JAMAIS persistés ici.
 * Ils sont calculés dynamiquement par {@code GarantieService} à partir des enregistrements
 * présents en base.
 */
@Entity
@Table(name = "garanties",
        indexes = @Index(name = "idx_garanties_client_id", columnList = "client_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Garantie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Client propriétaire de la garantie. Non-null : aucune garantie orpheline ne doit exister.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    @JsonBackReference("client-garanties")
    private Client client;

    /**
     * Libellé descriptif de la garantie,
     * ex : "Hypothèque sur la totalité d'une propriété immatriculée - TUNIS/Titre Foncier[...]".
     */
    @Column(length = 1000)
    private String libelle;

    /**
     * Consistance (description du bien), ex : "J&A", "2000 m²".
     */
    @Column(length = 500)
    private String consistance;

    /**
     * Valeur expertisée du bien en Dinars Tunisiens.
     */
    @Column(precision = 15, scale = 3)
    private BigDecimal valeur;

    /**
     * Date d'expertise de la garantie.
     */
    @Column
    private LocalDate dateExpertise;

    /**
     * Rang de la garantie (1 = premier rang, 2 = deuxième rang, etc.).
     */
    @Column
    private Integer rang;

    /**
     * Bénéficiaire de la garantie, ex : "BANQUE NATIONALE AGRICOLE".
     */
    @Column(length = 255)
    private String beneficiaire;

    /**
     * Montant des charges inscrites sur la garantie.
     */
    @Column(precision = 15, scale = 3)
    private BigDecimal charges;

    /**
     * Affectation / type de crédit lié, ex : "Crédit d'investissement".
     * Stocké en String car aucune entité Credit formelle n'existe encore dans ce projet.
     */
    @Column(length = 255)
    private String affectation;

    /**
     * Numéro séquentiel de la garantie dans le système bancaire, ex : "BNa1".
     */
    @Column(length = 50)
    private String numSeqGar;

    /**
     * Identifiant du crédit associé (référence au crédit mock), ex : "01".
     * Stocké en String car aucune entité Credit formelle n'existe encore dans ce projet.
     */
    @Column(length = 50)
    private String idCredit;
}
