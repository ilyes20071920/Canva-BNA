package tn.esprit.canvabna.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EngagementsActiviteResponse {
    private List<CompteValideResponse> comptes;
    private boolean soldesDisponibles;
    private List<SoldeParTypeResponse> soldesParType;
    private List<EngagementBnaRowResponse> engagementsBna;
}
