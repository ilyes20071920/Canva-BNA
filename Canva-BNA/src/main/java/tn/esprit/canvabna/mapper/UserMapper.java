package tn.esprit.canvabna.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import tn.esprit.canvabna.dto.UserResponse;
import tn.esprit.canvabna.entity.User;

/**
 * MapStruct mapper — converts between {@link User} entity and {@link UserResponse} DTO.
 * Spring bean is generated at compile time; no runtime reflection.
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "role", expression = "java(user.getRole().name())")
    UserResponse toResponse(User user);
}
