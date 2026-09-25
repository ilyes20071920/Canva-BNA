package tn.esprit.canvabna.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO représentant une garantie individuelle renvoyée au client Angular.
 * Ce DTO expose les champs de l'entité {@link tn.esprit.canvabna.entity.Garantie}
 * sans exposer les associations JPA.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GarantieResponse {

    private Long id;

    /** Libellé complet de la garantie (type + référence foncière). */
    private String libelle;

    /** Description du bien (consistance). */
    private String consistance;

    /** Valeur expertisée en DT. */
    private BigDecimal valeur;

    /** Date d'expertise. */
    private LocalDate dateExpertise;

    /** Rang hypothécaire ou de nantissement. */
    private Integer rang;

    /** Bénéficiaire de la garantie. */
    private String beneficiaire;

    /** Montant des charges inscrites. */
    private BigDecimal charges;

    /** Affectation (type de crédit lié). */
    private String affectation;

    /** Numéro séquentiel de la garantie dans le système BNA. */
    private String numSeqGar;

    /** Référence du crédit associé. */
    private String idCredit;
}
