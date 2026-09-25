package tn.esprit.canvabna.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "postes_financement")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PosteFinancement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String libelle;

    @Column(name = "montant_sollicite", precision = 15, scale = 3, nullable = false)
    private BigDecimal montantSollicite;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sollicitation_id", nullable = false)
    private Sollicitation sollicitation;
}
