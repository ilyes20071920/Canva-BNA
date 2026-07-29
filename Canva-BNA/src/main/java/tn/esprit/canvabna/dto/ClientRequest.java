package tn.esprit.canvabna.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO de création/mise à jour d'un {@link tn.esprit.canvabna.entity.Client}.
 */
@Getter
@Setter
@NoArgsConstructor
public class ClientRequest {

    @NotBlank(message = "L'identifiant du client est obligatoire")
    private String identifiant;

    @NotBlank(message = "Le groupe est obligatoire")
    private String groupe;

    @NotBlank(message = "La relation est obligatoire")
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

    /** ID de la structure organisationnelle associée (optionnel). */
    private Long structureId;
}
