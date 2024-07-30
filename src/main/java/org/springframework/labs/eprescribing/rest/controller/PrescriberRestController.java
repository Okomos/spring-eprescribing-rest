/*
 * Copyright 2016-2018 the original author or authors.
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
import org.springframework.labs.eprescribing.mapper.SpecialtyMapper;
import org.springframework.labs.eprescribing.mapper.PrescriberMapper;
import org.springframework.labs.eprescribing.model.Specialty;
import org.springframework.labs.eprescribing.model.Prescriber;
import org.springframework.labs.eprescribing.service.ClinicService;
import org.springframework.labs.eprescribing.rest.api.PrescribersApi;
import org.springframework.labs.eprescribing.rest.dto.PrescriberDto;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Vitaliy Fedoriv
 */

@RestController
@CrossOrigin(exposedHeaders = "errors, content-type")
@RequestMapping("api")
public class PrescriberRestController implements PrescribersApi {

    private final ClinicService clinicService;
    private final PrescriberMapper prescriberMapper;
    private final SpecialtyMapper specialtyMapper;

    public PrescriberRestController(ClinicService clinicService, PrescriberMapper prescriberMapper, SpecialtyMapper specialtyMapper) {
        this.clinicService = clinicService;
        this.prescriberMapper = prescriberMapper;
        this.specialtyMapper = specialtyMapper;
    }

    @PreAuthorize("hasRole(@roles.VET_ADMIN)")
    @Override
    public ResponseEntity<List<PrescriberDto>> listPrescribers() {
        List<PrescriberDto> prescribers = new ArrayList<>();
        prescribers.addAll(prescriberMapper.toPrescriberDtos(this.clinicService.findAllPrescribers()));
        if (prescribers.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(prescribers, HttpStatus.OK);
    }

    @PreAuthorize("hasRole(@roles.VET_ADMIN)")
    @Override
    public ResponseEntity<PrescriberDto> getPrescriber(Integer prescriberId)  {
        Prescriber prescriber = this.clinicService.findPrescriberById(prescriberId);
        if (prescriber == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(prescriberMapper.toPrescriberDto(prescriber), HttpStatus.OK);
    }

    @PreAuthorize("hasRole(@roles.VET_ADMIN)")
    @Override
    public ResponseEntity<PrescriberDto> addPrescriber(PrescriberDto prescriberDto) {
        HttpHeaders headers = new HttpHeaders();
        Prescriber prescriber = prescriberMapper.toPrescriber(prescriberDto);
        if(prescriber.getNrOfSpecialties() > 0){
            List<Specialty> prescriberSpecialities = this.clinicService.findSpecialtiesByNameIn(prescriber.getSpecialties().stream().map(Specialty::getName).collect(Collectors.toSet()));
            prescriber.setSpecialties(prescriberSpecialities);
        }
        this.clinicService.savePrescriber(prescriber);
        headers.setLocation(UriComponentsBuilder.newInstance().path("/api/prescribers/{id}").buildAndExpand(prescriber.getId()).toUri());
        return new ResponseEntity<>(prescriberMapper.toPrescriberDto(prescriber), headers, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole(@roles.VET_ADMIN)")
    @Override
    public ResponseEntity<PrescriberDto> updatePrescriber(Integer prescriberId,PrescriberDto prescriberDto)  {
        Prescriber currentPrescriber = this.clinicService.findPrescriberById(prescriberId);
        if (currentPrescriber == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        currentPrescriber.setFirstName(prescriberDto.getFirstName());
        currentPrescriber.setLastName(prescriberDto.getLastName());
        currentPrescriber.clearSpecialties();
        for (Specialty spec : specialtyMapper.toSpecialtys(prescriberDto.getSpecialties())) {
            currentPrescriber.addSpecialty(spec);
        }
        if(currentPrescriber.getNrOfSpecialties() > 0){
            List<Specialty> prescriberSpecialities = this.clinicService.findSpecialtiesByNameIn(currentPrescriber.getSpecialties().stream().map(Specialty::getName).collect(Collectors.toSet()));
            currentPrescriber.setSpecialties(prescriberSpecialities);
        }
        this.clinicService.savePrescriber(currentPrescriber);
        return new ResponseEntity<>(prescriberMapper.toPrescriberDto(currentPrescriber), HttpStatus.NO_CONTENT);
    }

    @PreAuthorize("hasRole(@roles.VET_ADMIN)")
    @Transactional
    @Override
    public ResponseEntity<PrescriberDto> deletePrescriber(Integer prescriberId) {
        Prescriber prescriber = this.clinicService.findPrescriberById(prescriberId);
        if (prescriber == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        this.clinicService.deletePrescriber(prescriber);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
