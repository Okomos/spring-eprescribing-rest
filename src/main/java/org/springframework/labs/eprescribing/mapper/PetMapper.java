package org.springframework.labs.eprescribing.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.labs.eprescribing.model.Pet;
import org.springframework.labs.eprescribing.model.PetType;
import org.springframework.labs.eprescribing.rest.dto.PetDto;
import org.springframework.labs.eprescribing.rest.dto.PetFieldsDto;
import org.springframework.labs.eprescribing.rest.dto.PetTypeDto;

import java.util.Collection;

/**
 * Map Pet & PetDto using mapstruct
 */
@Mapper
public interface PetMapper {

    @Mapping(source = "owner.id", target = "ownerId")
    PetDto toPetDto(Pet pet);

    Collection<PetDto> toPetsDto(Collection<Pet> pets);

    Collection<Pet> toPets(Collection<PetDto> pets);

    Pet toPet(PetDto petDto);

    Pet toPet(PetFieldsDto petFieldsDto);

    PetTypeDto toPetTypeDto(PetType petType);

    PetType toPetType(PetTypeDto petTypeDto);

    Collection<PetTypeDto> toPetTypeDtos(Collection<PetType> petTypes);
}
