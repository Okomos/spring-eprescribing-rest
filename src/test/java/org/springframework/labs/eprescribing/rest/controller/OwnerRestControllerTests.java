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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.labs.eprescribing.mapper.OwnerMapper;
import org.springframework.labs.eprescribing.mapper.MedicationMapper;
import org.springframework.labs.eprescribing.mapper.PrescriptionMapper;
import org.springframework.labs.eprescribing.model.Owner;
import org.springframework.labs.eprescribing.model.Medication;
import org.springframework.labs.eprescribing.rest.advice.ExceptionControllerAdvice;
import org.springframework.labs.eprescribing.rest.controller.OwnerRestController;
import org.springframework.labs.eprescribing.service.ClinicService;
import org.springframework.labs.eprescribing.rest.dto.OwnerDto;
import org.springframework.labs.eprescribing.rest.dto.MedicationDto;
import org.springframework.labs.eprescribing.rest.dto.MedicationTypeDto;
import org.springframework.labs.eprescribing.rest.dto.PrescriptionDto;
import org.springframework.labs.eprescribing.service.clinicService.ApplicationTestConfig;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


/**
 * Test class for {@link OwnerRestController}
 *
 * @author Vitaliy Fedoriv
 */
@SpringBootTest
@ContextConfiguration(classes = ApplicationTestConfig.class)
@WebAppConfiguration
class OwnerRestControllerTests {

    @Autowired
    private OwnerRestController ownerRestController;

    @Autowired
    private OwnerMapper ownerMapper;

    @Autowired
    private MedicationMapper medicationMapper;

    @Autowired
    private PrescriptionMapper prescriptionMapper;

    @MockBean
    private ClinicService clinicService;

    private MockMvc mockMvc;

    private List<OwnerDto> owners;

    private List<MedicationDto> medications;

    private List<PrescriptionDto> prescriptions;

    @BeforeEach
    void initOwners() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(ownerRestController)
            .setControllerAdvice(new ExceptionControllerAdvice())
            .build();
        owners = new ArrayList<>();

        OwnerDto ownerWithMedication = new OwnerDto();
        owners.add(ownerWithMedication.id(1).firstName("George").lastName("Franklin").address("110 W. Liberty St.").city("Madison").telephone("6085551023").addMedicationsItem(getTestMedicationWithIdAndName(ownerWithMedication, 1, "Rosy")));
        OwnerDto owner = new OwnerDto();
        owners.add(owner.id(2).firstName("Betty").lastName("Davis").address("638 Cardinal Ave.").city("Sun Prairie").telephone("6085551749"));
        owner = new OwnerDto();
        owners.add(owner.id(3).firstName("Eduardo").lastName("Rodriquez").address("2693 Commerce St.").city("McFarland").telephone("6085558763"));
        owner = new OwnerDto();
        owners.add(owner.id(4).firstName("Harold").lastName("Davis").address("563 Friendly St.").city("Windsor").telephone("6085553198"));

        MedicationTypeDto medicationType = new MedicationTypeDto();
        medicationType.id(2)
            .name("dog");

        medications = new ArrayList<>();
        MedicationDto medication = new MedicationDto();
        medications.add(medication.id(3)
            .name("Rosy")
            .expirationDate(LocalDate.now())
            .type(medicationType));

        medication = new MedicationDto();
        medications.add(medication.id(4)
            .name("Jewel")
            .expirationDate(LocalDate.now())
            .type(medicationType));

        prescriptions = new ArrayList<>();
        PrescriptionDto prescription = new PrescriptionDto();
        prescription.setId(2);
        prescription.setMedicationId(medication.getId());
        prescription.setDate(LocalDate.now());
        prescription.setDescription("rabies shot");
        prescriptions.add(prescription);

        prescription = new PrescriptionDto();
        prescription.setId(3);
        prescription.setMedicationId(medication.getId());
        prescription.setDate(LocalDate.now());
        prescription.setDescription("neutered");
        prescriptions.add(prescription);
    }

    private MedicationDto getTestMedicationWithIdAndName(final OwnerDto owner, final int id, final String name) {
        MedicationTypeDto medicationType = new MedicationTypeDto();
        MedicationDto medication = new MedicationDto();
        medication.id(id).name(name).expirationDate(LocalDate.now()).type(medicationType.id(2).name("dog")).addPrescriptionsItem(getTestPrescriptionForMedication(medication, 1));
        return medication;
    }

    private PrescriptionDto getTestPrescriptionForMedication(final MedicationDto medication, final int id) {
        PrescriptionDto prescription = new PrescriptionDto();
        return prescription.id(id).date(LocalDate.now()).description("test" + id);
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void testGetOwnerSuccess() throws Exception {
        given(this.clinicService.findOwnerById(1)).willReturn(ownerMapper.toOwner(owners.get(0)));
        this.mockMvc.perform(get("/api/owners/1")
                .accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.firstName").value("George"));
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void testGetOwnerNotFound() throws Exception {
        given(this.clinicService.findOwnerById(2)).willReturn(null);
        this.mockMvc.perform(get("/api/owners/2")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void testGetOwnersListSuccess() throws Exception {
        owners.remove(0);
        owners.remove(1);
        given(this.clinicService.findOwnerByLastName("Davis")).willReturn(ownerMapper.toOwners(owners));
        this.mockMvc.perform(get("/api/owners?lastName=Davis")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.[0].id").value(2))
            .andExpect(jsonPath("$.[0].firstName").value("Betty"))
            .andExpect(jsonPath("$.[1].id").value(4))
            .andExpect(jsonPath("$.[1].firstName").value("Harold"));
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void testGetOwnersListNotFound() throws Exception {
        owners.clear();
        given(this.clinicService.findOwnerByLastName("0")).willReturn(ownerMapper.toOwners(owners));
        this.mockMvc.perform(get("/api/owners/?lastName=0")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void testGetAllOwnersSuccess() throws Exception {
        owners.remove(0);
        owners.remove(1);
        given(this.clinicService.findAllOwners()).willReturn(ownerMapper.toOwners(owners));
        this.mockMvc.perform(get("/api/owners/")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.[0].id").value(2))
            .andExpect(jsonPath("$.[0].firstName").value("Betty"))
            .andExpect(jsonPath("$.[1].id").value(4))
            .andExpect(jsonPath("$.[1].firstName").value("Harold"));
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void testGetAllOwnersNotFound() throws Exception {
        owners.clear();
        given(this.clinicService.findAllOwners()).willReturn(ownerMapper.toOwners(owners));
        this.mockMvc.perform(get("/api/owners/")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void testCreateOwnerSuccess() throws Exception {
        OwnerDto newOwnerDto = owners.get(0);
        newOwnerDto.setId(null);
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        String newOwnerAsJSON = mapper.writeValueAsString(newOwnerDto);
        this.mockMvc.perform(post("/api/owners/")
                .content(newOwnerAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void testCreateOwnerError() throws Exception {
        OwnerDto newOwnerDto = owners.get(0);
        newOwnerDto.setId(null);
        newOwnerDto.setFirstName(null);
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        String newOwnerAsJSON = mapper.writeValueAsString(newOwnerDto);
        this.mockMvc.perform(post("/api/owners/")
                .content(newOwnerAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void testUpdateOwnerSuccess() throws Exception {
        given(this.clinicService.findOwnerById(1)).willReturn(ownerMapper.toOwner(owners.get(0)));
        int ownerId = owners.get(0).getId();
        OwnerDto updatedOwnerDto = new OwnerDto();
        // body.id = ownerId which is used in url path
        updatedOwnerDto.setId(ownerId);
        updatedOwnerDto.setFirstName("GeorgeI");
        updatedOwnerDto.setLastName("Franklin");
        updatedOwnerDto.setAddress("110 W. Liberty St.");
        updatedOwnerDto.setCity("Madison");
        updatedOwnerDto.setTelephone("6085551023");
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        String newOwnerAsJSON = mapper.writeValueAsString(updatedOwnerDto);
        this.mockMvc.perform(put("/api/owners/" + ownerId)
                .content(newOwnerAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().contentType("application/json"))
            .andExpect(status().isNoContent());

        this.mockMvc.perform(get("/api/owners/" + ownerId)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.id").value(ownerId))
            .andExpect(jsonPath("$.firstName").value("GeorgeI"));

    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void testUpdateOwnerSuccessNoBodyId() throws Exception {
        given(this.clinicService.findOwnerById(1)).willReturn(ownerMapper.toOwner(owners.get(0)));
        int ownerId = owners.get(0).getId();
        OwnerDto updatedOwnerDto = new OwnerDto();
        updatedOwnerDto.setFirstName("GeorgeI");
        updatedOwnerDto.setLastName("Franklin");
        updatedOwnerDto.setAddress("110 W. Liberty St.");
        updatedOwnerDto.setCity("Madison");

        updatedOwnerDto.setTelephone("6085551023");
        ObjectMapper mapper = new ObjectMapper();
        String newOwnerAsJSON = mapper.writeValueAsString(updatedOwnerDto);
        this.mockMvc.perform(put("/api/owners/" + ownerId)
                .content(newOwnerAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().contentType("application/json"))
            .andExpect(status().isNoContent());

        this.mockMvc.perform(get("/api/owners/" + ownerId)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.id").value(ownerId))
            .andExpect(jsonPath("$.firstName").value("GeorgeI"));

    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void testUpdateOwnerError() throws Exception {
        OwnerDto newOwnerDto = owners.get(0);
        newOwnerDto.setFirstName("");
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        String newOwnerAsJSON = mapper.writeValueAsString(newOwnerDto);
        this.mockMvc.perform(put("/api/owners/1")
                .content(newOwnerAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void testDeleteOwnerSuccess() throws Exception {
        OwnerDto newOwnerDto = owners.get(0);
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        String newOwnerAsJSON = mapper.writeValueAsString(newOwnerDto);
        final Owner owner = ownerMapper.toOwner(owners.get(0));
        given(this.clinicService.findOwnerById(1)).willReturn(owner);
        this.mockMvc.perform(delete("/api/owners/1")
                .content(newOwnerAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void testDeleteOwnerError() throws Exception {
        OwnerDto newOwnerDto = owners.get(0);
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        String newOwnerAsJSON = mapper.writeValueAsString(newOwnerDto);
        given(this.clinicService.findOwnerById(999)).willReturn(null);
        this.mockMvc.perform(delete("/api/owners/999")
                .content(newOwnerAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void testCreateMedicationSuccess() throws Exception {
        MedicationDto newMedication = medications.get(0);
        newMedication.setId(999);
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        String newMedicationAsJSON = mapper.writeValueAsString(newMedication);
        System.err.println("--> newMedicationAsJSON=" + newMedicationAsJSON);
        this.mockMvc.perform(post("/api/owners/1/medications/")
                .content(newMedicationAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void testCreateMedicationError() throws Exception {
        MedicationDto newMedication = medications.get(0);
        newMedication.setId(null);
        newMedication.setName(null);
        ObjectMapper mapper = new ObjectMapper();
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.registerModule(new JavaTimeModule());
        String newMedicationAsJSON = mapper.writeValueAsString(newMedication);
        this.mockMvc.perform(post("/api/owners/1/medications/")
                .content(newMedicationAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isBadRequest()).andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void testCreatePrescriptionSuccess() throws Exception {
        PrescriptionDto newPrescription = prescriptions.get(0);
        newPrescription.setId(999);
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        String newPrescriptionAsJSON = mapper.writeValueAsString(prescriptionMapper.toPrescription(newPrescription));
        System.out.println("newPrescriptionAsJSON " + newPrescriptionAsJSON);
        this.mockMvc.perform(post("/api/owners/1/medications/1/prescriptions")
                .content(newPrescriptionAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void testGetOwnerMedicationSuccess() throws Exception {
        owners.remove(0);
        owners.remove(1);
        given(this.clinicService.findAllOwners()).willReturn(ownerMapper.toOwners(owners));
        var owner = ownerMapper.toOwner(owners.get(0));
        given(this.clinicService.findOwnerById(2)).willReturn(owner);
        var medication = medicationMapper.toMedication(medications.get(0));
        medication.setOwner(owner);
        given(this.clinicService.findMedicationById(1)).willReturn(medication);
        this.mockMvc.perform(get("/api/owners/2/medications/1")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"));
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void testGetOwnersMedicationsNotFound() throws Exception {
        owners.clear();
        given(this.clinicService.findAllOwners()).willReturn(ownerMapper.toOwners(owners));
        this.mockMvc.perform(get("/api/owners/1/medications/1")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }


}
