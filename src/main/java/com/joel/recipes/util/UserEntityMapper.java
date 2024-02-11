package com.joel.recipes.util;

import com.joel.recipes.dto.UserEntityPatch;
import com.joel.recipes.model.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserEntityMapper {
    void mapUserEntityPatchToUserEntity(UserEntityPatch userEntityPatch, @MappingTarget UserEntity userEntity);
}
