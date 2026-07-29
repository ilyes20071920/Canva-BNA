package tn.esprit.canvabna.exception;

/**
 * Exception levée lorsqu'une ressource demandée n'est pas trouvée en base de données.
 * Gérée par {@link GlobalExceptionHandler} pour retourner un HTTP 404.
 *
 * <p>Exemple d'utilisation :
 * <pre>
 *     throw new ResourceNotFoundException("Client", "id", clientId);
 * </pre>
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Crée une exception avec un message structuré indiquant le type de ressource,
     * le champ de recherche et la valeur utilisée.
     *
     * @param resourceName nom de la ressource (ex: "Client", "Compte")
     * @param fieldName    champ de recherche (ex: "id", "identifiant")
     * @param fieldValue   valeur recherchée
     */
    public ResourceNotFoundException(final String resourceName, final String fieldName, final Object fieldValue) {
        super(String.format("%s non trouvé avec %s : '%s'", resourceName, fieldName, fieldValue));
    }
}
