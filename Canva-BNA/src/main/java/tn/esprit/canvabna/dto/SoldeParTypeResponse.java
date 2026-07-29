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
public class SoldeParTypeResponse {
    private String typeCompte;
    private BigDecimal soldeAlgebrique;
}
