package tn.esprit.canvabna.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload de la décision finale sur une sollicitation.
 * Envoyé par le Chef de Division ou l'Admin via POST /api/clients/{id}/sollicitation/decision.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DecisionRequest {

    /** La décision prise : "ACCEPTE" ou "REFUSE". */
    @NotBlank(message = "La décision est obligatoire")
    @Pattern(regexp = "ACCEPTE|REFUSE", message = "La décision doit être 'ACCEPTE' ou 'REFUSE'")
    private String decision;

    /** Commentaire obligatoire justifiant la décision. */
    @NotBlank(message = "Le commentaire de décision est obligatoire")
    private String commentaireDecision;
}
