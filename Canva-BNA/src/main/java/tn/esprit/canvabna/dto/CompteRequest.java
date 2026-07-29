package tn.esprit.canvabna.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO de création/mise à jour d'un {@link tn.esprit.canvabna.entity.Compte}.
 */
@Getter
@Setter
@NoArgsConstructor
public class CompteRequest {

    @NotBlank(message = "Le code guichet est obligatoire")
    private String codeGuichet;

    @NotBlank(message = "Le code produit est obligatoire")
    private String codeProduit;

    @NotBlank(message = "Le numéro de compte est obligatoire")
    private String numCompte;

    private String agence;

    @NotNull(message = "L'identifiant du client est obligatoire")
    private Long clientId;
}
