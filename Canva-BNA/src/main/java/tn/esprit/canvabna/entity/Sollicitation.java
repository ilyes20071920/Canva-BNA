package tn.esprit.canvabna.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sollicitations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sollicitation {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        /** Client auquel est rattachée la sollicitation (plusieurs possibles). */
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "client_id", nullable = false)
        private Client client;

        /** Date de la demande du client. */
        @Column(name = "date_demande", nullable = false)
        private LocalDate dateDemande;

        /** Compte de déblocage du CMLT. */
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumns({
                        @JoinColumn(name = "compte_code_guichet", referencedColumnName = "code_guichet"),
                        @JoinColumn(name = "compte_code_produit", referencedColumnName = "code_produit"),
                        @JoinColumn(name = "compte_num_compte", referencedColumnName = "num_compte")
        })
        private Compte compteDeblocage;

        /** Produit de crédit sélectionné. */
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "produit_id")
        private ProduitRef produit;

        /** Objet du crédit sélectionné. */
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "objet_credit_id")
        private ObjetCreditRef objetCredit;

        /** Montant sollicité (DT). */
        @Column(name = "montant_sollicite", precision = 15, scale = 3, nullable = false)
        private BigDecimal montantSollicite;

        /** Durée sollicitée (en mois). */
        @Column(name = "duree_sollicitee_mois", nullable = false)
        private Integer dureeSolliciteeMois;

        /** Périodicité du principal (PL), ex: "Trimestrielle". */
        @Column(name = "periodicite_pl")
        private String periodicitePl;

        /** Nombre de périodes sollicitées (PL). */
        @Column(name = "nbre_periodes_sollicitees_pl")
        private Integer nbrePeriodesSolliciteesPl;

        /** Nombre de périodes de grâce (PL). */
        @Column(name = "nbre_periodes_grace_pl")
        private Integer nbrePeriodesGracePl;

        /** Périodicité des intérêts (Int.). */
        @Column(name = "periodicite_int")
        private String periodiciteInt;

        /** Nombre de périodes sollicitées (Int.). */
        @Column(name = "nbre_periodes_sollicitees_int")
        private Integer nbrePeriodesSolliciteesInt;

        /** Nombre de périodes de grâce (Int.). */
        @Column(name = "nbre_periodes_grace_int")
        private Integer nbrePeriodesGraceInt;

        // Conditions de banque
        @Column(name = "type_taux_sollicite")
        private String typeTauxSollicite;

        @Column(precision = 7, scale = 4)
        private BigDecimal marge;

        @Column(name = "taux_sollicite", precision = 7, scale = 4)
        private BigDecimal tauxSollicite;

        @Column(name = "type_commission")
        private String typeCommission;

        @Column(name = "commission_forfaitaire_sollicitee", precision = 15, scale = 3)
        private BigDecimal commissionForfaitaireSollicitee;

        // Commentaires Phase 1
        @Column(name = "commentaire_dr", columnDefinition = "TEXT")
        private String commentaireDr;

        // Phase 2: Étude du projet
        @Column(name = "apercu_projet", columnDefinition = "TEXT")
        private String apercuProjet;

        @Column(name = "etude_marche", columnDefinition = "TEXT")
        private String etudeMarche;

        @Column(name = "synthese_rentabilite", columnDefinition = "TEXT")
        private String syntheseRentabilite;

        @Column(name = "swot_forces", columnDefinition = "TEXT")
        private String swotForces;

        @Column(name = "swot_faiblesses", columnDefinition = "TEXT")
        private String swotFaiblesses;

        @Column(name = "swot_opportunites", columnDefinition = "TEXT")
        private String swotOpportunites;

        @Column(name = "swot_menaces", columnDefinition = "TEXT")
        private String swotMenaces;

        @Column(name = "commentaire_etude_projet", columnDefinition = "TEXT")
        private String commentaireEtudeProjet;

        // ---------------------------------------------------------------
        // Workflow — Statut & Décision
        // ---------------------------------------------------------------

        /** Statut du cycle de vie de la sollicitation. */
        @Enumerated(EnumType.STRING)
        @Column(name = "statut", nullable = false)
        @Builder.Default
        private StatutSollicitation statut = StatutSollicitation.BROUILLON;

        /**
         * Décision finale : "ACCEPTE" ou "REFUSE". Null tant que la décision n'est pas
         * prise.
         */
        @Column(name = "decision")
        private String decision;

        /** Commentaire justifiant la décision du Chef de Division / Admin. */
        @Column(name = "commentaire_decision", columnDefinition = "TEXT")
        private String commentaireDecision;

        /** Date à laquelle la décision a été prise. */
        @Column(name = "date_decision")
        private LocalDate dateDecision;

        // Child Collections with orphanRemoval = true
        @OneToMany(mappedBy = "sollicitation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
        @Builder.Default
        private List<PosteInvestissement> postesInvestissement = new ArrayList<>();

        @OneToMany(mappedBy = "sollicitation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
        @Builder.Default
        private List<PosteFinancement> postesFinancement = new ArrayList<>();

        @OneToMany(mappedBy = "sollicitation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
        @Builder.Default
        private List<NouvelleGarantie> nouvellesGaranties = new ArrayList<>();
}
