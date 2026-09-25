package tn.esprit.canvabna.dto;

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
public class SollicitationResponse {
    private Long id;
    private Long clientId;
    private String clientIdentifiant;
    private String clientRelation;
    private String clientGroupe;
    private LocalDate dateDemande;

    private String compteCodeGuichet;
    private String compteCodeProduit;
    private String compteNumCompte;
    private String compteNumeroComplet;

    private ReferenceItemDto produit;
    private ReferenceItemDto objetCredit;

    private BigDecimal montantSollicite;
    private Integer dureeSolliciteeMois;

    private String periodicitePl;
    private Integer nbrePeriodesSolliciteesPl;
    private Integer nbrePeriodesGracePl;

    private String periodiciteInt;
    private Integer nbrePeriodesSolliciteesInt;
    private Integer nbrePeriodesGraceInt;

    private String typeTauxSollicite;
    private BigDecimal marge;
    private BigDecimal tauxSollicite;

    private String typeCommission;
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

    // Workflow — Statut & Décision
    private String statut;
    private String decision;
    private String commentaireDecision;
    private java.time.LocalDate dateDecision;

    private BigDecimal totalInvestissementSollicite;
    private BigDecimal totalFinancementSollicite;

    @Builder.Default
    private List<PosteInvestissementDto> postesInvestissement = new ArrayList<>();

    @Builder.Default
    private List<PosteFinancementDto> postesFinancement = new ArrayList<>();

    @Builder.Default
    private List<NouvelleGarantieDto> nouvellesGaranties = new ArrayList<>();
}
