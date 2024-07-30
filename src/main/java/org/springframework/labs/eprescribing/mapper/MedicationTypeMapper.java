package org.springframework.labs.eprescribing.mapper;

import org.mapstruct.Mapper;
import org.springframework.labs.eprescribing.model.MedicationType;
import org.springframework.labs.eprescribing.rest.dto.MedicationTypeDto;
import org.springframework.labs.eprescribing.rest.dto.MedicationTypeFieldsDto;

import java.util.Collection;
import java.util.List;

/**
 * Map MedicationType & MedicationTypeDto using mapstruct
 */
@Mapper
public interface MedicationTypeMapper {

    MedicationType toMedicationType(MedicationTypeDto medicationTypeDto);

    MedicationType toMedicationType(MedicationTypeFieldsDto medicationTypeFieldsDto);

    MedicationTypeDto toMedicationTypeDto(MedicationType medicationType);

    List<MedicationTypeDto> toMedicationTypeDtos(Collection<MedicationType> medicationTypes);
}
