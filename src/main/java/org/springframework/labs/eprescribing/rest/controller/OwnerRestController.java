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
import org.springframework.labs.eprescribing.mapper.OwnerMapper;
import org.springframework.labs.eprescribing.mapper.MedicationMapper;
import org.springframework.labs.eprescribing.mapper.PrescriptionMapper;
import org.springframework.labs.eprescribing.model.Owner;
import org.springframework.labs.eprescribing.model.Medication;
import org.springframework.labs.eprescribing.model.MedicationType;
import org.springframework.labs.eprescribing.model.Prescription;
import org.springframework.labs.eprescribing.service.ClinicService;
import org.springframework.labs.eprescribing.rest.api.OwnersApi;
import org.springframework.labs.eprescribing.rest.dto.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.transaction.Transactional;

import java.util.Collection;
import java.util.List;

/**
 * @author Vitaliy Fedoriv
 */

@RestController
@CrossOrigin(exposedHeaders = "errors, content-type")
@RequestMapping("/api")
public class OwnerRestController implements OwnersApi {

    private final ClinicService clinicService;

    private final OwnerMapper ownerMapper;

    private final MedicationMapper medicationMapper;

    private final PrescriptionMapper prescriptionMapper;

    public OwnerRestController(ClinicService clinicService,
                               OwnerMapper ownerMapper,
                               MedicationMapper medicationMapper,
                               PrescriptionMapper prescriptionMapper) {
        this.clinicService = clinicService;
        this.ownerMapper = ownerMapper;
        this.medicationMapper = medicationMapper;
        this.prescriptionMapper = prescriptionMapper;
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public ResponseEntity<List<OwnerDto>> listOwners(String lastName) {
        Collection<Owner> owners;
        if (lastName != null) {
            owners = this.clinicService.findOwnerByLastName(lastName);
        } else {
            owners = this.clinicService.findAllOwners();
        }
        if (owners.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(ownerMapper.toOwnerDtoCollection(owners), HttpStatus.OK);
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public ResponseEntity<OwnerDto> getOwner(Integer ownerId) {
        Owner owner = this.clinicService.findOwnerById(ownerId);
        if (owner == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(ownerMapper.toOwnerDto(owner), HttpStatus.OK);
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public ResponseEntity<OwnerDto> addOwner(OwnerFieldsDto ownerFieldsDto) {
        HttpHeaders headers = new HttpHeaders();
        Owner owner = ownerMapper.toOwner(ownerFieldsDto);
        this.clinicService.saveOwner(owner);
        OwnerDto ownerDto = ownerMapper.toOwnerDto(owner);
        headers.setLocation(UriComponentsBuilder.newInstance()
            .path("/api/owners/{id}").buildAndExpand(owner.getId()).toUri());
        return new ResponseEntity<>(ownerDto, headers, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public ResponseEntity<OwnerDto> updateOwner(Integer ownerId, OwnerFieldsDto ownerFieldsDto) {
        Owner currentOwner = this.clinicService.findOwnerById(ownerId);
        if (currentOwner == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        currentOwner.setAddress(ownerFieldsDto.getAddress());
        currentOwner.setCity(ownerFieldsDto.getCity());
        currentOwner.setFirstName(ownerFieldsDto.getFirstName());
        currentOwner.setLastName(ownerFieldsDto.getLastName());
        currentOwner.setTelephone(ownerFieldsDto.getTelephone());
        this.clinicService.saveOwner(currentOwner);
        return new ResponseEntity<>(ownerMapper.toOwnerDto(currentOwner), HttpStatus.NO_CONTENT);
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Transactional
    @Override
    public ResponseEntity<OwnerDto> deleteOwner(Integer ownerId) {
        Owner owner = this.clinicService.findOwnerById(ownerId);
        if (owner == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        this.clinicService.deleteOwner(owner);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public ResponseEntity<MedicationDto> addMedicationToOwner(Integer ownerId, MedicationFieldsDto medicationFieldsDto) {
        HttpHeaders headers = new HttpHeaders();
        Medication medication = medicationMapper.toMedication(medicationFieldsDto);
        Owner owner = new Owner();
        owner.setId(ownerId);
        medication.setOwner(owner);
        MedicationType medicationType = this.clinicService.findMedicationTypeByName(medication.getType().getName());
        medication.setType(medicationType);
        this.clinicService.saveMedication(medication);
        MedicationDto medicationDto = medicationMapper.toMedicationDto(medication);
        headers.setLocation(UriComponentsBuilder.newInstance().path("/api/medications/{id}")
            .buildAndExpand(medication.getId()).toUri());
        return new ResponseEntity<>(medicationDto, headers, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public ResponseEntity<PrescriptionDto> addPrescriptionToOwner(Integer ownerId, Integer medicationId, PrescriptionFieldsDto prescriptionFieldsDto) {
        HttpHeaders headers = new HttpHeaders();
        Prescription prescription = prescriptionMapper.toPrescription(prescriptionFieldsDto);
        Medication medication = new Medication();
        medication.setId(medicationId);
        prescription.setMedication(medication);
        this.clinicService.savePrescription(prescription);
        PrescriptionDto prescriptionDto = prescriptionMapper.toPrescriptionDto(prescription);
        headers.setLocation(UriComponentsBuilder.newInstance().path("/api/prescriptions/{id}")
            .buildAndExpand(prescription.getId()).toUri());
        return new ResponseEntity<>(prescriptionDto, headers, HttpStatus.CREATED);
    }


    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public ResponseEntity<MedicationDto> getOwnersMedication(Integer ownerId, Integer medicationId) {
        Owner owner = this.clinicService.findOwnerById(ownerId);
        Medication medication = this.clinicService.findMedicationById(medicationId);
        if (owner == null || medication == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            if (!medication.getOwner().equals(owner)) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            } else {
                return new ResponseEntity<>(medicationMapper.toMedicationDto(medication), HttpStatus.OK);
            }
        }
    }
}
