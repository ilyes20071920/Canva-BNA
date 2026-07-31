package tn.esprit.canvabna.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EngagementBnaRowResponse {
    private String code;
    private String forme;
    private BigDecimal autorise;
    private LocalDate echeance;
    private BigDecimal encours;
    private BigDecimal impayes;
    private BigDecimal ir;
    private String ancienneteImpayes;
    private String detailsTitle;
    private String subtotalLabel;
    private List<EngagementDetailResponse> details;
}
