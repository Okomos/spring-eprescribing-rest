package org.springframework.samples.eprescribing.mapper;

import org.mapstruct.Mapper;
import org.springframework.samples.eprescribing.rest.dto.SpecialtyDto;
import org.springframework.samples.eprescribing.model.Specialty;

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
