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
public class NouvelleGarantieDto {
    private Long id;

    @NotBlank(message = "La nature de la garantie est obligatoire")
    private String natureGarantie;

    @DecimalMin(value = "0.0", inclusive = true, message = "L'estimation ne peut pas être négative")
    private BigDecimal estimation;

    private LocalDate dateExpertise;

    @DecimalMin(value = "0.0", inclusive = true, message = "Les charges ne peuvent pas être négatives")
    private BigDecimal chargesInscrites;
}
