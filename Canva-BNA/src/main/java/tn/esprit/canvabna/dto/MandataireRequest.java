package tn.esprit.canvabna.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * DTO de création/mise à jour d'un {@link tn.esprit.canvabna.entity.Mandataire}.
 */
@Getter
@Setter
@NoArgsConstructor
public class MandataireRequest {

    private String numMandat;
    private String numDemande;
    private String typeMandat;
    private String agence;
    private String mandant;
    private LocalDate dateCreation;
    private LocalDate dateDebut;
    private LocalDate dateFin;

    @NotBlank(message = "Le code guichet du compte est obligatoire")
    private String compteCodeGuichet;

    @NotBlank(message = "Le code produit du compte est obligatoire")
    private String compteCodeProduit;

    @NotBlank(message = "Le numéro de compte est obligatoire")
    private String compteNumCompte;
}
