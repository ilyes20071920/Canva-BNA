package tn.esprit.canvabna.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * DTO de réponse pour {@link tn.esprit.canvabna.entity.Structure}.
 * N'expose pas la liste des clients pour éviter la récursion JSON.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StructureResponse {

    private Long id;
    private String nom;
    private String code;
}
