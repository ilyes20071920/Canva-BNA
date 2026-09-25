package tn.esprit.canvabna.entity;

/**
 * Statut du cycle de vie d'une sollicitation.
 *
 * <ul>
 *   <li>{@link #BROUILLON} — canevas en cours de saisie, non encore soumis</li>
 *   <li>{@link #EN_ATTENTE_DECISION} — canevas enregistré, en attente de la décision du Chef Division</li>
 *   <li>{@link #ACCEPTE} — sollicitation acceptée</li>
 *   <li>{@link #REFUSE} — sollicitation refusée</li>
 * </ul>
 */
public enum StatutSollicitation {
    BROUILLON,
    EN_ATTENTE_DECISION,
    ACCEPTE,
    REFUSE
}
