package tn.esprit.canvabna.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EngagementDetailResponse {
    private String numDecisionCrd;
    private String formeCredit;
    private String numCompte;
    private BigDecimal autorise;
    private BigDecimal utilise;
    private LocalDate echeance;
    private LocalDate echFinale;
    private BigDecimal aEchoirEnPrincipal;
    private BigDecimal encours;
    private BigDecimal impayes;
    private BigDecimal impayesPI;
    private BigDecimal ir;
    private String anciennete;
    private String detailsImpayes;
}
