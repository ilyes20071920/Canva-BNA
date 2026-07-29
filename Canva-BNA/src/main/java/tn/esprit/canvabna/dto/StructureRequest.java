package tn.esprit.canvabna.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO de création/mise à jour d'une {@link tn.esprit.canvabna.entity.Structure}.
 */
@Getter
@Setter
@NoArgsConstructor
public class StructureRequest {

    @NotBlank(message = "Le nom de la structure est obligatoire")
    private String nom;

    @NotBlank(message = "Le code de la structure est obligatoire")
    private String code;
}
