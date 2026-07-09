package tn.esprit.canvabna.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Core user entity persisted to the database.
 * Password is always stored as a BCrypt hash — never plain text.
 */
@Entity
@Table(name = "users",
        uniqueConstraints = @UniqueConstraint(columnNames = "matricule"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Unique login identifier — used as the Spring Security username. */
    @Column(nullable = false, unique = true)
    private Integer matricule;

    @Column
    private String nom;

    @Column
    private String prenom;

    /** BCrypt-hashed password — never stored as plain text. */
    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String structure;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;
}
