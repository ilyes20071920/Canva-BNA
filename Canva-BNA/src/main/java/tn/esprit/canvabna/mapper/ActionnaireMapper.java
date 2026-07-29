package tn.esprit.canvabna.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import tn.esprit.canvabna.dto.ActionnaireResponse;
import tn.esprit.canvabna.entity.Actionnaire;

/**
 * MapStruct mapper pour {@link Actionnaire}.
 * Le client est représenté uniquement par son ID dans la réponse.
 */
@Mapper(componentModel = "spring")
public interface ActionnaireMapper {

    @Mapping(target = "clientId", expression = "java(actionnaire.getClient() != null ? actionnaire.getClient().getId() : null)")
    ActionnaireResponse toResponse(Actionnaire actionnaire);
}
