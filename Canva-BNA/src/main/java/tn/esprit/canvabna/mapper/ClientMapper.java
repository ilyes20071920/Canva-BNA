package tn.esprit.canvabna.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import tn.esprit.canvabna.dto.ClientResponse;
import tn.esprit.canvabna.entity.Client;

/**
 * MapStruct mapper pour {@link Client}.
 * Utilise {@link ActionnaireMapper} et {@link CompteMapper} pour les listes imbriquées.
 * La structure est mappée vers son DTO de réponse plat via {@link StructureMapper}.
 */
@Mapper(componentModel = "spring", uses = {ActionnaireMapper.class, CompteMapper.class, StructureMapper.class})
public interface ClientMapper {

    @Mapping(target = "structure", source = "structure")
    @Mapping(target = "actionnaires", source = "actionnaires")
    @Mapping(target = "comptes", source = "comptes")
    ClientResponse toResponse(Client client);
}
