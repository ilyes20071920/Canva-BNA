package tn.esprit.canvabna.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO de réponse pour {@link tn.esprit.canvabna.entity.Actionnaire}.
 * Le client est représenté par son ID uniquement pour éviter la récursion.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActionnaireResponse {

    private Long id;
    private String nom;
    private Integer nombreActions;
    private BigDecimal montant;
    private Double pourcentageActions;

    /** ID du client propriétaire — pas l'objet complet pour éviter la récursion. */
    private Long clientId;
}
