package tn.esprit.canvabna.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import tn.esprit.canvabna.dto.StructureRequest;
import tn.esprit.canvabna.dto.StructureResponse;
import tn.esprit.canvabna.entity.Structure;

/**
 * MapStruct mapper pour {@link Structure}.
 * La liste des clients n'est pas incluse dans la réponse pour éviter la récursion JSON.
 */
@Mapper(componentModel = "spring")
public interface StructureMapper {

    StructureResponse toResponse(Structure structure);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "clients", ignore = true)
    Structure toEntity(StructureRequest request);
}
