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
public class CompteValideResponse {
    private String numeroCompte;
    private LocalDate dateOuverture;
    private String typeAutorisation;
    private BigDecimal autorisation;
    private LocalDate dateAutorisation;
    private BigDecimal solde;
}
