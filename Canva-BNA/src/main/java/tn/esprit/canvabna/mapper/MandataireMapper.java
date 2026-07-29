package tn.esprit.canvabna.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import tn.esprit.canvabna.dto.MandataireResponse;
import tn.esprit.canvabna.entity.Mandataire;

/**
 * MapStruct mapper pour {@link Mandataire}.
 */
@Mapper(componentModel = "spring")
public interface MandataireMapper {

    @Mapping(target = "compteCodeGuichet", source = "compte.id.codeGuichet")
    @Mapping(target = "compteCodeProduit", source = "compte.id.codeProduit")
    @Mapping(target = "compteNumCompte", source = "compte.id.numCompte")
    MandataireResponse toResponse(Mandataire mandataire);
}
