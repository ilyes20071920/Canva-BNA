package tn.esprit.canvabna.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO de réponse pour {@link tn.esprit.canvabna.entity.Compte}.
 * Inclut la liste des mandataires associés et l'ID du client propriétaire.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompteResponse {

    private String codeGuichet;
    private String codeProduit;
    private String numCompte;
    private String numeroCompteComplet; // Format: {codeGuichet}-{codeProduit}-{numCompte}
    private String agence;

    /** ID du client propriétaire. */
    private Long clientId;

    /** Mandataires autorisés sur ce compte. */
    private List<MandataireResponse> mandataires;
}
