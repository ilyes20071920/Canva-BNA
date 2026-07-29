package tn.esprit.canvabna.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompteId implements Serializable {

    @Column(name = "code_guichet", nullable = false)
    private String codeGuichet;

    @Column(name = "code_produit", nullable = false)
    private String codeProduit;

    @Column(name = "num_compte", nullable = false)
    private String numCompte;
}
