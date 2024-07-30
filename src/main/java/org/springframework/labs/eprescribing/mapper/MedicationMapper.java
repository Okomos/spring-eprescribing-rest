package org.springframework.labs.eprescribing.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.labs.eprescribing.model.Medication;
import org.springframework.labs.eprescribing.model.MedicationType;
import org.springframework.labs.eprescribing.rest.dto.MedicationDto;
import org.springframework.labs.eprescribing.rest.dto.MedicationFieldsDto;
import org.springframework.labs.eprescribing.rest.dto.MedicationTypeDto;

import java.util.Collection;

/**
 * Map Medication & MedicationDto using mapstruct
 */
@Mapper
public interface MedicationMapper {

    @Mapping(source = "owner.id", target = "ownerId")
    MedicationDto toMedicationDto(Medication medication);

    Collection<MedicationDto> toMedicationsDto(Collection<Medication> medications);

    Collection<Medication> toMedications(Collection<MedicationDto> medications);

    Medication toMedication(MedicationDto medicationDto);

    Medication toMedication(MedicationFieldsDto medicationFieldsDto);

    MedicationTypeDto toMedicationTypeDto(MedicationType medicationType);

    MedicationType toMedicationType(MedicationTypeDto medicationTypeDto);

    Collection<MedicationTypeDto> toMedicationTypeDtos(Collection<MedicationType> medicationTypes);
}
