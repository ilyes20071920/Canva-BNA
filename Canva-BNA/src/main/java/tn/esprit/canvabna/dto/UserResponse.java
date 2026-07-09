package tn.esprit.canvabna.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Public-facing user representation.
 * Never exposes the password field.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private Long id;
    private Integer matricule;
    private String nom;
    private String prenom;
    private String structure;
    private String role;
    private boolean enabled;
}
