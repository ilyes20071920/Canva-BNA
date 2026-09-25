package tn.esprit.canvabna.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO agrégé contenant la liste des garanties d'un client et les totaux calculés dynamiquement.
 *
 * <p><strong>IMPORTANT — Totaux dynamiques :</strong>
 * {@code totalChargesInscrites} et {@code dontBna} ne sont JAMAIS persistés en base de données.
 * Ils sont recalculés à chaque appel API par {@link tn.esprit.canvabna.service.GarantieService}
 * à partir des enregistrements {@link GarantieResponse} présents.
 *
 * <p><strong>HYPOTHÈSE TEMPORAIRE — dontBna :</strong>
 * Pour le développement, {@code dontBna} est calculé comme la somme des charges des garanties
 * dont le bénéficiaire est exactement "BANQUE NATIONALE AGRICOLE".
 * Ce calcul est une <em>approximation de développement</em> et devra être révisé lorsque
 * le vrai modèle de données des charges inscrites BNA sera disponible (il utilisera
 * probablement un champ dédié "banque créancière" ou une table de charges séparée).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GarantiesSummaryResponse {

    /** Liste des garanties du client. Vide si le client n'a aucune garantie. */
    private List<GarantieResponse> garanties;

    /**
     * Total des charges inscrites, calculé dynamiquement :
     * somme de {@code garantie.charges} pour toutes les garanties.
     */
    private BigDecimal totalChargesInscrites;

    /**
     * Montant des charges BNA, calculé dynamiquement.
     *
     * <p><em>Hypothèse de développement temporaire :</em>
     * Somme de {@code garantie.charges} pour les garanties dont le bénéficiaire
     * est "BANQUE NATIONALE AGRICOLE".
     * Ce calcul devra être affiné avec le vrai modèle métier BNA.
     */
    private BigDecimal dontBna;
}
