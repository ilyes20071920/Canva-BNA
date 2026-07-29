package tn.esprit.canvabna.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActiviteAnneeResponse {
    private Integer annee;
    private BigDecimal totalMouvement;
    private BigDecimal ca;
    private BigDecimal partConfiee;
    private BigDecimal partEngagement;
}
