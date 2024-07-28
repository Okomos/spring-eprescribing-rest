package org.springframework.labs.eprescribing.mapper;

import org.mapstruct.Mapper;
import org.springframework.labs.eprescribing.model.PetType;
import org.springframework.labs.eprescribing.rest.dto.PetTypeDto;
import org.springframework.labs.eprescribing.rest.dto.PetTypeFieldsDto;

import java.util.Collection;
import java.util.List;

/**
 * Map PetType & PetTypeDto using mapstruct
 */
@Mapper
public interface PetTypeMapper {

    PetType toPetType(PetTypeDto petTypeDto);

    PetType toPetType(PetTypeFieldsDto petTypeFieldsDto);

    PetTypeDto toPetTypeDto(PetType petType);

    List<PetTypeDto> toPetTypeDtos(Collection<PetType> petTypes);
}
