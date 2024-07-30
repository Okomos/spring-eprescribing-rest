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
import org.springframework.labs.eprescribing.mapper.PrescriptionMapper;
import org.springframework.labs.eprescribing.model.Prescription;
import org.springframework.labs.eprescribing.service.ClinicService;
import org.springframework.labs.eprescribing.rest.api.PrescriptionsApi;
import org.springframework.labs.eprescribing.rest.dto.PrescriptionDto;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Vitaliy Fedoriv
 */

@RestController
@CrossOrigin(exposedHeaders = "errors, content-type")
@RequestMapping("api")
public class PrescriptionRestController implements PrescriptionsApi {

    private final ClinicService clinicService;

    private final PrescriptionMapper prescriptionMapper;

    public PrescriptionRestController(ClinicService clinicService, PrescriptionMapper prescriptionMapper) {
        this.clinicService = clinicService;
        this.prescriptionMapper = prescriptionMapper;
    }


    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public ResponseEntity<List<PrescriptionDto>> listPrescriptions() {
        List<Prescription> prescriptions = new ArrayList<>(this.clinicService.findAllPrescriptions());
        if (prescriptions.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(new ArrayList<>(prescriptionMapper.toPrescriptionsDto(prescriptions)), HttpStatus.OK);
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public ResponseEntity<PrescriptionDto> getPrescription( Integer prescriptionId) {
        Prescription prescription = this.clinicService.findPrescriptionById(prescriptionId);
        if (prescription == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(prescriptionMapper.toPrescriptionDto(prescription), HttpStatus.OK);
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public ResponseEntity<PrescriptionDto> addPrescription(PrescriptionDto prescriptionDto) {
        HttpHeaders headers = new HttpHeaders();
        Prescription prescription = prescriptionMapper.toPrescription(prescriptionDto);
        this.clinicService.savePrescription(prescription);
        prescriptionDto = prescriptionMapper.toPrescriptionDto(prescription);
        headers.setLocation(UriComponentsBuilder.newInstance().path("/api/prescriptions/{id}").buildAndExpand(prescription.getId()).toUri());
        return new ResponseEntity<>(prescriptionDto, headers, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public ResponseEntity<PrescriptionDto> updatePrescription(Integer prescriptionId, PrescriptionDto prescriptionDto) {
        Prescription currentPrescription = this.clinicService.findPrescriptionById(prescriptionId);
        if (currentPrescription == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        currentPrescription.setDate(prescriptionDto.getDate());
        currentPrescription.setDescription(prescriptionDto.getDescription());
        this.clinicService.savePrescription(currentPrescription);
        return new ResponseEntity<>(prescriptionMapper.toPrescriptionDto(currentPrescription), HttpStatus.NO_CONTENT);
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Transactional
    @Override
    public ResponseEntity<PrescriptionDto> deletePrescription(Integer prescriptionId) {
        Prescription prescription = this.clinicService.findPrescriptionById(prescriptionId);
        if (prescription == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        this.clinicService.deletePrescription(prescription);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
