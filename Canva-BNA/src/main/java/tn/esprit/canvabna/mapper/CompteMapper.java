package tn.esprit.canvabna.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import tn.esprit.canvabna.dto.CompteResponse;
import tn.esprit.canvabna.entity.Compte;

/**
 * MapStruct mapper pour {@link Compte}.
 * Mappe la liste des mandataires via {@link MandataireMapper}.
 */
@Mapper(componentModel = "spring", uses = {MandataireMapper.class})
public interface CompteMapper {

    @Mapping(target = "codeGuichet", source = "id.codeGuichet")
    @Mapping(target = "codeProduit", source = "id.codeProduit")
    @Mapping(target = "numCompte", source = "id.numCompte")
    @Mapping(target = "numeroCompteComplet", expression = "java(compte.getId() != null ? compte.getId().getCodeGuichet() + \"-\" + compte.getId().getCodeProduit() + \"-\" + compte.getId().getNumCompte() : null)")
    @Mapping(target = "clientId", expression = "java(compte.getClient() != null ? compte.getClient().getId() : null)")
    @Mapping(target = "mandataires", source = "mandataires")
    CompteResponse toResponse(Compte compte);
}
