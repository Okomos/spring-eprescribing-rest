package org.springframework.labs.eprescribing.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.labs.eprescribing.model.Prescription;
import org.springframework.labs.eprescribing.rest.dto.PrescriptionDto;
import org.springframework.labs.eprescribing.rest.dto.PrescriptionFieldsDto;

import java.util.Collection;

/**
 * Map Prescription & PrescriptionDto using mapstruct
 */
@Mapper(uses = MedicationMapper.class)
public interface PrescriptionMapper {
    Prescription toPrescription(PrescriptionDto prescriptionDto);

    Prescription toPrescription(PrescriptionFieldsDto prescriptionFieldsDto);

    @Mapping(source = "medication.id", target = "medicationId")
    PrescriptionDto toPrescriptionDto(Prescription prescription);

    Collection<PrescriptionDto> toPrescriptionsDto(Collection<Prescription> prescriptions);

}
