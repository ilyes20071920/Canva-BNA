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
public class ConditionsBanqueResponse {
    private String compteGroupe;
    private String finValidite;
    private List<TauxRow> taux;
    private List<CommissionRow> commissions;
    private String commentaires;
}
