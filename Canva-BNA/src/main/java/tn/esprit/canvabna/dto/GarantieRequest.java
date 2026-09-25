package tn.esprit.canvabna.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GarantieRequest {

    @NotBlank(message = "Le libellé / nature de la garantie est obligatoire")
    private String libelle;

    private String consistance;

    @DecimalMin(value = "0.0", message = "La valeur ne peut pas être négative")
    private BigDecimal valeur;

    private LocalDate dateExpertise;

    private Integer rang;

    private String beneficiaire;

    @DecimalMin(value = "0.0", message = "Les charges ne peuvent pas être négatives")
    private BigDecimal charges;

    private String affectation;

    private String numSeqGar;

    private String idCredit;
}
