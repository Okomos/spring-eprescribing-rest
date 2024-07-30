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

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.labs.eprescribing.mapper.MedicationTypeMapper;
import org.springframework.labs.eprescribing.model.MedicationType;
import org.springframework.labs.eprescribing.service.ClinicService;
import org.springframework.labs.eprescribing.rest.api.MedicationtypesApi;
import org.springframework.labs.eprescribing.rest.dto.MedicationTypeDto;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.transaction.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RestController
@CrossOrigin(exposedHeaders = "errors, content-type")
@RequestMapping("api")
public class MedicationTypeRestController implements MedicationtypesApi {

    private final ClinicService clinicService;
    private final MedicationTypeMapper medicationTypeMapper;


    public MedicationTypeRestController(ClinicService clinicService, MedicationTypeMapper medicationTypeMapper) {
        this.clinicService = clinicService;
        this.medicationTypeMapper = medicationTypeMapper;
    }

    @PreAuthorize("hasAnyRole(@roles.OWNER_ADMIN, @roles.VET_ADMIN)")
    @Override
    public ResponseEntity<List<MedicationTypeDto>> listMedicationTypes() {
        List<MedicationType> medicationTypes = new ArrayList<>(this.clinicService.findAllMedicationTypes());
        if (medicationTypes.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(medicationTypeMapper.toMedicationTypeDtos(medicationTypes), HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole(@roles.OWNER_ADMIN, @roles.VET_ADMIN)")
    @Override
    public ResponseEntity<MedicationTypeDto> getMedicationType(Integer medicationTypeId) {
        MedicationType medicationType = this.clinicService.findMedicationTypeById(medicationTypeId);
        if (medicationType == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(medicationTypeMapper.toMedicationTypeDto(medicationType), HttpStatus.OK);
    }

    @PreAuthorize("hasRole(@roles.VET_ADMIN)")
    @Override
    public ResponseEntity<MedicationTypeDto> addMedicationType(MedicationTypeDto medicationTypeDto) {
        HttpHeaders headers = new HttpHeaders();
        if (Objects.nonNull(medicationTypeDto.getId()) && !medicationTypeDto.getId().equals(0)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } else {
            final MedicationType type = medicationTypeMapper.toMedicationType(medicationTypeDto);
            this.clinicService.saveMedicationType(type);
            headers.setLocation(UriComponentsBuilder.newInstance().path("/api/medicationtypes/{id}").buildAndExpand(type.getId()).toUri());
            return new ResponseEntity<>(medicationTypeMapper.toMedicationTypeDto(type), headers, HttpStatus.CREATED);
        }
    }

    @PreAuthorize("hasRole(@roles.VET_ADMIN)")
    @Override
    public ResponseEntity<MedicationTypeDto> updateMedicationType(Integer medicationTypeId, MedicationTypeDto medicationTypeDto) {
        MedicationType currentMedicationType = this.clinicService.findMedicationTypeById(medicationTypeId);
        if (currentMedicationType == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        currentMedicationType.setName(medicationTypeDto.getName());
        this.clinicService.saveMedicationType(currentMedicationType);
        return new ResponseEntity<>(medicationTypeMapper.toMedicationTypeDto(currentMedicationType), HttpStatus.NO_CONTENT);
    }

    @PreAuthorize("hasRole(@roles.VET_ADMIN)")
    @Transactional
    @Override
    public ResponseEntity<MedicationTypeDto> deleteMedicationType(Integer medicationTypeId) {
        MedicationType medicationType = this.clinicService.findMedicationTypeById(medicationTypeId);
        if (medicationType == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        this.clinicService.deleteMedicationType(medicationType);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
