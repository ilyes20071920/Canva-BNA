package tn.esprit.canvabna.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Représente un actionnaire ou associé dans la structure du capital social d'un client.
 * Appartient à exactement un {@link Client}.
 */
@Entity
@Table(name = "actionnaires")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Actionnaire {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    /** Nombre de parts/actions détenues. */
    @Column(nullable = false)
    private Integer nombreActions;

    /** Valeur monétaire des actions en DT. */
    @Column(precision = 15, scale = 3)
    private BigDecimal montant;

    /** Pourcentage des actions dans le capital total. */
    @Column
    private Double pourcentageActions;

    /** Client propriétaire de ces actions. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    @JsonBackReference("client-actionnaires")
    private Client client;
}
