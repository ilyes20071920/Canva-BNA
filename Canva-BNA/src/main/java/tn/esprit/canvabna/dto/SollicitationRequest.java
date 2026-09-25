package tn.esprit.canvabna.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SollicitationRequest {

    @NotNull(message = "La date de la demande est obligatoire")
    private LocalDate dateDemande;

    /** Code Guichet du compte de déblocage. */
    private String compteCodeGuichet;

    /** Code Produit du compte de déblocage. */
    private String compteCodeProduit;

    /** Numéro de compte du compte de déblocage. */
    private String compteNumCompte;

    /** ID du Produit de crédit de référence. */
    @NotNull(message = "Le produit est obligatoire")
    private Long produitId;

    /** ID de l'Objet du crédit de référence. */
    @NotNull(message = "L'objet du crédit est obligatoire")
    private Long objetCreditId;

    @NotNull(message = "Le montant sollicité est obligatoire")
    @DecimalMin(value = "0.0", inclusive = true, message = "Le montant sollicité ne peut pas être négatif")
    private BigDecimal montantSollicite;

    @NotNull(message = "La durée sollicitée est obligatoire")
    @Min(value = 0, message = "La durée ne peut pas être négative")
    private Integer dureeSolliciteeMois;

    private String periodicitePl;

    @Min(value = 0, message = "Le nombre de périodes sollicitées (PL) ne peut pas être négatif")
    private Integer nbrePeriodesSolliciteesPl;

    @Min(value = 0, message = "Le nombre de périodes de grâce (PL) ne peut pas être négatif")
    private Integer nbrePeriodesGracePl;

    private String periodiciteInt;

    @Min(value = 0, message = "Le nombre de périodes sollicitées (Int.) ne peut pas être négatif")
    private Integer nbrePeriodesSolliciteesInt;

    @Min(value = 0, message = "Le nombre de périodes de grâce (Int.) ne peut pas être négatif")
    private Integer nbrePeriodesGraceInt;

    private String typeTauxSollicite;

    @DecimalMin(value = "0.0", inclusive = true, message = "La marge ne peut pas être négative")
    private BigDecimal marge;

    @DecimalMin(value = "0.0", inclusive = true, message = "Le taux sollicité ne peut pas être négatif")
    private BigDecimal tauxSollicite;

    private String typeCommission;

    @DecimalMin(value = "0.0", inclusive = true, message = "La commission ne peut pas être négative")
    private BigDecimal commissionForfaitaireSollicitee;

    private String commentaireDr;

    // Phase 2: Étude du projet
    private String apercuProjet;
    private String etudeMarche;
    private String syntheseRentabilite;
    private String swotForces;
    private String swotFaiblesses;
    private String swotOpportunites;
    private String swotMenaces;
    private String commentaireEtudeProjet;

    @Valid
    @Builder.Default
    private List<PosteInvestissementDto> postesInvestissement = new ArrayList<>();

    @Valid
    @Builder.Default
    private List<PosteFinancementDto> postesFinancement = new ArrayList<>();

    @Valid
    @Builder.Default
    private List<NouvelleGarantieDto> nouvellesGaranties = new ArrayList<>();
}
