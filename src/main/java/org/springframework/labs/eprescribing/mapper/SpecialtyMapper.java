package org.springframework.labs.eprescribing.mapper;

import org.mapstruct.Mapper;
import org.springframework.labs.eprescribing.model.Specialty;
import org.springframework.labs.eprescribing.rest.dto.SpecialtyDto;

import java.util.Collection;

/**
 * Map Specialty & SpecialtyDto using mapstruct
 */
@Mapper
public interface SpecialtyMapper {
    Specialty toSpecialty(SpecialtyDto specialtyDto);

    SpecialtyDto toSpecialtyDto(Specialty specialty);

    Collection<SpecialtyDto> toSpecialtyDtos(Collection<Specialty> specialties);

    Collection<Specialty> toSpecialtys(Collection<SpecialtyDto> specialties);

}
