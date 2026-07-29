package tn.esprit.canvabna.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * DTO de création/mise à jour d'un {@link tn.esprit.canvabna.entity.Actionnaire}.
 */
@Getter
@Setter
@NoArgsConstructor
public class ActionnaireRequest {

    @NotBlank(message = "Le nom de l'actionnaire est obligatoire")
    private String nom;

    @NotNull(message = "Le nombre d'actions est obligatoire")
    private Integer nombreActions;

    private BigDecimal montant;
    private Double pourcentageActions;

    @NotNull(message = "L'identifiant du client est obligatoire")
    private Long clientId;
}
