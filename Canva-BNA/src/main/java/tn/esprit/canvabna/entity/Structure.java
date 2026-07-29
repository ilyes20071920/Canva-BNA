package tn.esprit.canvabna.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Entité de référence représentant une structure organisationnelle bancaire.
 * Une structure peut regrouper plusieurs clients.
 */
@Entity
@Table(name = "structures")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Structure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false, unique = true)
    private String code;

    /** Liste des clients rattachés à cette structure (relation inverse). */
    @OneToMany(mappedBy = "structure", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Client> clients = new ArrayList<>();
}
