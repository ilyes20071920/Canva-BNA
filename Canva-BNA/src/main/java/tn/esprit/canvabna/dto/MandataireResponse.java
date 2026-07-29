package tn.esprit.canvabna.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO de réponse pour {@link tn.esprit.canvabna.entity.Mandataire}.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MandataireResponse {

    private Long id;
    private String numMandat;
    private String numDemande;
    private String typeMandat;
    private String agence;
    private String mandant;
    private LocalDate dateCreation;
    private LocalDate dateDebut;
    private LocalDate dateFin;

    private String compteCodeGuichet;
    private String compteCodeProduit;
    private String compteNumCompte;
}
