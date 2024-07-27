package org.springframework.samples.eprescribing.mapper;

import org.mapstruct.Mapper;
import org.springframework.samples.eprescribing.rest.dto.OwnerDto;
import org.springframework.samples.eprescribing.model.Owner;
import org.springframework.samples.eprescribing.rest.dto.OwnerFieldsDto;

import java.util.Collection;
import java.util.List;

/**
 * Maps Owner & OwnerDto using Mapstruct
 */
@Mapper(uses = PetMapper.class)
public interface OwnerMapper {

    OwnerDto toOwnerDto(Owner owner);

    Owner toOwner(OwnerDto ownerDto);

    Owner toOwner(OwnerFieldsDto ownerDto);

    List<OwnerDto> toOwnerDtoCollection(Collection<Owner> ownerCollection);

    Collection<Owner> toOwners(Collection<OwnerDto> ownerDtos);
}
