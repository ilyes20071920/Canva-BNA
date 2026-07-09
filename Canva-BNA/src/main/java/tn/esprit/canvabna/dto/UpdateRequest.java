package tn.esprit.canvabna.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tn.esprit.canvabna.entity.Role;

/**
 * Request DTO for updating an existing user.
 * Password is not included here (usually handled via a separate endpoint if needed).
 */
@Getter
@Setter
@NoArgsConstructor
public class UpdateRequest {

    private String nom;
    private String prenom;

    @NotBlank(message = "Structure must not be blank")
    private String structure;

    @NotNull(message = "Role is required")
    private Role role;

    private String password;
}

