package tn.esprit.canvabna.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "nouvelles_garanties")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NouvelleGarantie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nature_garantie", nullable = false)
    private String natureGarantie;

    @Column(precision = 15, scale = 3)
    private BigDecimal estimation;

    @Column(name = "date_expertise")
    private LocalDate dateExpertise;

    @Column(name = "charges_inscrites", precision = 15, scale = 3)
    private BigDecimal chargesInscrites;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sollicitation_id", nullable = false)
    private Sollicitation sollicitation;
}
