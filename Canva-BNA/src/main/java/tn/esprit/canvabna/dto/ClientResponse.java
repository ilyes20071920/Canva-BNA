package tn.esprit.canvabna.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO de réponse pour {@link tn.esprit.canvabna.entity.Client}.
 *
 * <p>Inclut des listes aplaties d'actionnaires et de comptes.
 * N'expose jamais directement les entités JPA pour éviter les boucles JSON.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientResponse {

    private Long id;
    private String identifiant;
    private String groupe;
    private String relation;
    private String activite;
    private String segment;
    private String siegeSocial;
    private String secteur;
    private LocalDate dateEntreeRelation;
    private String formeJuridique;
    private BigDecimal capitalSocial;
    private String agence;
    private String directionRegionale;

    /** Référence à la structure (sans les clients pour éviter la récursion). */
    private StructureResponse structure;

    /** Actionnaires du capital social. */
    private List<ActionnaireResponse> actionnaires;

    /** Comptes bancaires liés au client. */
    private List<CompteResponse> comptes;
}
