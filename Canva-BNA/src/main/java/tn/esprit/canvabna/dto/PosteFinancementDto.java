package tn.esprit.canvabna.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PosteFinancementDto {
    private Long id;

    @NotBlank(message = "Le libellé du financement est obligatoire")
    private String libelle;

    @NotNull(message = "Le montant sollicité est obligatoire")
    @DecimalMin(value = "0.0", inclusive = true, message = "Le montant sollicité ne peut pas être négatif")
    private BigDecimal montantSollicite;
}
