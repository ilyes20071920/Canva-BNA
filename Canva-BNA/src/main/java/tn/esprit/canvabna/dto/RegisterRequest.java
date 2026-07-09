package tn.esprit.canvabna.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tn.esprit.canvabna.entity.Role;

/**
 * Request DTO for registering a new user.
 * Only ROLE_ADMIN callers can access the register endpoint.
 */
@Getter
@Setter
@NoArgsConstructor
public class RegisterRequest {

    @NotNull(message = "Matricule is required")
    private Integer matricule;

    private String nom;
    private String prenom;

    @NotBlank(message = "Password must not be blank")
    private String password;

    @NotBlank(message = "Structure must not be blank")
    private String structure;

    @NotNull(message = "Role is required")
    private Role role;
}
