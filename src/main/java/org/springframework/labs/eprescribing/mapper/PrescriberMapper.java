package org.springframework.labs.eprescribing.mapper;

import org.mapstruct.Mapper;
import org.springframework.labs.eprescribing.model.Prescriber;
import org.springframework.labs.eprescribing.rest.dto.PrescriberDto;
import org.springframework.labs.eprescribing.rest.dto.PrescriberFieldsDto;

import java.util.Collection;

/**
 * Map Prescriber & PrescriberoDto using mapstruct
 */
@Mapper(uses = SpecialtyMapper.class)
public interface PrescriberMapper {
    Prescriber toPrescriber(PrescriberDto prescriberDto);

    Prescriber toPrescriber(PrescriberFieldsDto prescriberFieldsDto);

    PrescriberDto toPrescriberDto(Prescriber prescriber);

    Collection<PrescriberDto> toPrescriberDtos(Collection<Prescriber> prescribers);
}
