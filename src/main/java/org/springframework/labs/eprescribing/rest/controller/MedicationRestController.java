/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.labs.eprescribing.rest.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.labs.eprescribing.mapper.MedicationMapper;
import org.springframework.labs.eprescribing.model.Medication;
import org.springframework.labs.eprescribing.service.ClinicService;
import org.springframework.labs.eprescribing.rest.api.MedicationsApi;
import org.springframework.labs.eprescribing.rest.dto.MedicationDto;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Vitaliy Fedoriv
 */

@RestController
@CrossOrigin(exposedHeaders = "errors, content-type")
@RequestMapping("api")
public class MedicationRestController implements MedicationsApi {

    private final ClinicService clinicService;

    private final MedicationMapper medicationMapper;

    public MedicationRestController(ClinicService clinicService, MedicationMapper medicationMapper) {
        this.clinicService = clinicService;
        this.medicationMapper = medicationMapper;
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public ResponseEntity<MedicationDto> getMedication(Integer medicationId) {
        MedicationDto medication = medicationMapper.toMedicationDto(this.clinicService.findMedicationById(medicationId));
        if (medication == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(medication, HttpStatus.OK);
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public ResponseEntity<List<MedicationDto>> listMedications() {
        List<MedicationDto> medications = new ArrayList<>(medicationMapper.toMedicationsDto(this.clinicService.findAllMedications()));
        if (medications.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(medications, HttpStatus.OK);
    }


    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public ResponseEntity<MedicationDto> updateMedication(Integer medicationId, MedicationDto medicationDto) {
        Medication currentMedication = this.clinicService.findMedicationById(medicationId);
        if (currentMedication == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        currentMedication.setExpirationDate(medicationDto.getExpirationDate());
        currentMedication.setName(medicationDto.getName());
        currentMedication.setType(medicationMapper.toMedicationType(medicationDto.getType()));
        this.clinicService.saveMedication(currentMedication);
        return new ResponseEntity<>(medicationMapper.toMedicationDto(currentMedication), HttpStatus.NO_CONTENT);
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public ResponseEntity<MedicationDto> deleteMedication(Integer medicationId) {
        Medication medication = this.clinicService.findMedicationById(medicationId);
        if (medication == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        this.clinicService.deleteMedication(medication);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public ResponseEntity<MedicationDto> addMedication(MedicationDto medicationDto) {
        this.clinicService.saveMedication(medicationMapper.toMedication(medicationDto));
        return new ResponseEntity<>(medicationDto, HttpStatus.OK);
    }
}
