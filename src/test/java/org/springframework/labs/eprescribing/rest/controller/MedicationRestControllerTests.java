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
import org.springframework.labs.eprescribing.mapper.MedicationMapper;
import org.springframework.labs.eprescribing.model.Medication;
import org.springframework.labs.eprescribing.rest.advice.ExceptionControllerAdvice;
import org.springframework.labs.eprescribing.rest.controller.MedicationRestController;
import org.springframework.labs.eprescribing.service.ClinicService;
import org.springframework.labs.eprescribing.rest.dto.OwnerDto;
import org.springframework.labs.eprescribing.rest.dto.MedicationDto;
import org.springframework.labs.eprescribing.rest.dto.MedicationTypeDto;
import org.springframework.labs.eprescribing.service.clinicService.ApplicationTestConfig;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


/**
 * Test class for {@link MedicationRestController}
 *
 * @author Vitaliy Fedoriv
 */

@SpringBootTest
@ContextConfiguration(classes = ApplicationTestConfig.class)
@WebAppConfiguration
class MedicationRestControllerTests {

    @MockBean
    protected ClinicService clinicService;
    @Autowired
    private MedicationRestController medicationRestController;
    @Autowired
    private MedicationMapper medicationMapper;
    private MockMvc mockMvc;

    private List<MedicationDto> medications;

    @BeforeEach
    void initMedications() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(medicationRestController)
            .setControllerAdvice(new ExceptionControllerAdvice())
            .build();
        medications = new ArrayList<>();

        OwnerDto owner = new OwnerDto();
        owner.id(1).firstName("Eduardo")
            .lastName("Rodriquez")
            .address("2693 Commerce St.")
            .city("McFarland")
            .telephone("6085558763");

        MedicationTypeDto medicationType = new MedicationTypeDto();
        medicationType.id(2)
            .name("dog");

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
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void testGetMedicationSuccess() throws Exception {
        given(this.clinicService.findMedicationById(3)).willReturn(medicationMapper.toMedication(medications.get(0)));
        this.mockMvc.perform(get("/api/medications/3")
                .accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.id").value(3))
            .andExpect(jsonPath("$.name").value("Rosy"));
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void testGetMedicationNotFound() throws Exception {
        given(medicationMapper.toMedicationDto(this.clinicService.findMedicationById(-1))).willReturn(null);
        this.mockMvc.perform(get("/api/medications/999")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void testGetAllMedicationsSuccess() throws Exception {
        final Collection<Medication> medications = medicationMapper.toMedications(this.medications);
        System.err.println(medications);
        when(this.clinicService.findAllMedications()).thenReturn(medications);
        //given(this.clinicService.findAllMedications()).willReturn(medicationMapper.toMedications(medications));
        this.mockMvc.perform(get("/api/medications/")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.[0].id").value(3))
            .andExpect(jsonPath("$.[0].name").value("Rosy"))
            .andExpect(jsonPath("$.[1].id").value(4))
            .andExpect(jsonPath("$.[1].name").value("Jewel"));
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void testGetAllMedicationsNotFound() throws Exception {
        medications.clear();
        given(this.clinicService.findAllMedications()).willReturn(medicationMapper.toMedications(medications));
        this.mockMvc.perform(get("/api/medications/")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void testUpdateMedicationSuccess() throws Exception {
        given(this.clinicService.findMedicationById(3)).willReturn(medicationMapper.toMedication(medications.get(0)));
        MedicationDto newMedication = medications.get(0);
        newMedication.setName("Rosy I");
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        String newMedicationAsJSON = mapper.writeValueAsString(newMedication);
        this.mockMvc.perform(put("/api/medications/3")
                .content(newMedicationAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().contentType("application/json"))
            .andExpect(status().isNoContent());

        this.mockMvc.perform(get("/api/medications/3")
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.id").value(3))
            .andExpect(jsonPath("$.name").value("Rosy I"));

    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void testUpdateMedicationError() throws Exception {
        MedicationDto newMedication = medications.get(0);
        newMedication.setName(null);
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        String newMedicationAsJSON = mapper.writeValueAsString(newMedication);

        this.mockMvc.perform(put("/api/medications/3")
                .content(newMedicationAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void testDeleteMedicationSuccess() throws Exception {
        MedicationDto newMedication = medications.get(0);
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        String newMedicationAsJSON = mapper.writeValueAsString(newMedication);
        given(this.clinicService.findMedicationById(3)).willReturn(medicationMapper.toMedication(medications.get(0)));
        this.mockMvc.perform(delete("/api/medications/3")
                .content(newMedicationAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void testDeleteMedicationError() throws Exception {
        MedicationDto newMedication = medications.get(0);
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        String newMedicationAsJSON = mapper.writeValueAsString(newMedication);
        given(this.clinicService.findMedicationById(999)).willReturn(null);
        this.mockMvc.perform(delete("/api/medications/999")
                .content(newMedicationAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void testAddMedicationSuccess() throws Exception {
        MedicationDto newMedication = medications.get(0);
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        String newMedicationAsJSON = mapper.writeValueAsString(newMedication);
        given(this.clinicService.findMedicationById(3)).willReturn(medicationMapper.toMedication(medications.get(0)));
        this.mockMvc.perform(post("/api/medications")
                .content(newMedicationAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void testAddMedicationError() throws Exception {
        MedicationDto newMedication = medications.get(0);
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        String newMedicationAsJSON = mapper.writeValueAsString(newMedication);
        given(this.clinicService.findMedicationById(999)).willReturn(null);
        this.mockMvc.perform(post("/api/medications")
                .content(new String()).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isBadRequest());
    }
}
