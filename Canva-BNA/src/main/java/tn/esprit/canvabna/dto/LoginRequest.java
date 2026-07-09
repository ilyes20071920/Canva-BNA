package tn.esprit.canvabna.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request DTO for the login endpoint.
 * Validated automatically by Spring before reaching the controller method.
 */
@Getter
@Setter
@NoArgsConstructor
public class LoginRequest {

    @NotNull(message = "Matricule is required")
    private Integer matricule;

    @NotBlank(message = "Password must not be blank")
    private String password;
}
