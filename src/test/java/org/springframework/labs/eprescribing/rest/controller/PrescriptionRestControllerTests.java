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
import org.springframework.labs.eprescribing.mapper.PrescriptionMapper;
import org.springframework.labs.eprescribing.model.Owner;
import org.springframework.labs.eprescribing.model.Medication;
import org.springframework.labs.eprescribing.model.MedicationType;
import org.springframework.labs.eprescribing.model.Prescription;
import org.springframework.labs.eprescribing.rest.advice.ExceptionControllerAdvice;
import org.springframework.labs.eprescribing.rest.controller.PrescriptionRestController;
import org.springframework.labs.eprescribing.service.ClinicService;
import org.springframework.labs.eprescribing.service.clinicService.ApplicationTestConfig;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for {@link PrescriptionRestController}
 *
 * @author Vitaliy Fedoriv
 */
@SpringBootTest
@ContextConfiguration(classes=ApplicationTestConfig.class)
@WebAppConfiguration
class PrescriptionRestControllerTests {

    @Autowired
    private PrescriptionRestController prescriptionRestController;

    @MockBean
    private ClinicService clinicService;

    @Autowired
    private PrescriptionMapper prescriptionMapper;

    private MockMvc mockMvc;

    private List<Prescription> prescriptions;

    @BeforeEach
    void initPrescriptions(){
    	this.mockMvc = MockMvcBuilders.standaloneSetup(prescriptionRestController)
    			.setControllerAdvice(new ExceptionControllerAdvice())
    			.build();

        prescriptions = new ArrayList<>();

    	Owner owner = new Owner();
    	owner.setId(1);
    	owner.setFirstName("Eduardo");
    	owner.setLastName("Rodriquez");
    	owner.setAddress("2693 Commerce St.");
    	owner.setCity("McFarland");
    	owner.setTelephone("6085558763");

    	MedicationType medicationType = new MedicationType();
    	medicationType.setId(2);
    	medicationType.setName("dog");

    	Medication medication = new Medication();
    	medication.setId(8);
    	medication.setName("Rosy");
        medication.setExpirationDate(LocalDate.now());
    	medication.setOwner(owner);
    	medication.setType(medicationType);


    	Prescription prescription = new Prescription();
    	prescription.setId(2);
    	prescription.setMedication(medication);
        prescription.setDate(LocalDate.now());
    	prescription.setDescription("rabies shot");
    	prescriptions.add(prescription);

    	prescription = new Prescription();
    	prescription.setId(3);
    	prescription.setMedication(medication);
        prescription.setDate(LocalDate.now());
    	prescription.setDescription("neutered");
    	prescriptions.add(prescription);


    }

    @Test
    @WithMockUser(roles="OWNER_ADMIN")
    void testGetPrescriptionSuccess() throws Exception {
    	given(this.clinicService.findPrescriptionById(2)).willReturn(prescriptions.get(0));
        this.mockMvc.perform(get("/api/prescriptions/2")
        	.accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.id").value(2))
            .andExpect(jsonPath("$.description").value("rabies shot"));
    }

    @Test
    @WithMockUser(roles="OWNER_ADMIN")
    void testGetPrescriptionNotFound() throws Exception {
        given(this.clinicService.findPrescriptionById(999)).willReturn(null);
        this.mockMvc.perform(get("/api/prescriptions/999")
        	.accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles="OWNER_ADMIN")
    void testGetAllPrescriptionsSuccess() throws Exception {
    	given(this.clinicService.findAllPrescriptions()).willReturn(prescriptions);
        this.mockMvc.perform(get("/api/prescriptions/")
        	.accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
        	.andExpect(jsonPath("$.[0].id").value(2))
        	.andExpect(jsonPath("$.[0].description").value("rabies shot"))
        	.andExpect(jsonPath("$.[1].id").value(3))
        	.andExpect(jsonPath("$.[1].description").value("neutered"));
    }

    @Test
    @WithMockUser(roles="OWNER_ADMIN")
    void testGetAllPrescriptionsNotFound() throws Exception {
    	prescriptions.clear();
    	given(this.clinicService.findAllPrescriptions()).willReturn(prescriptions);
        this.mockMvc.perform(get("/api/prescriptions/")
        	.accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles="OWNER_ADMIN")
    void testCreatePrescriptionSuccess() throws Exception {
    	Prescription newPrescription = prescriptions.get(0);
    	newPrescription.setId(999);
    	ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        String newPrescriptionAsJSON = mapper.writeValueAsString(prescriptionMapper.toPrescriptionDto(newPrescription));
    	System.out.println("newPrescriptionAsJSON " + newPrescriptionAsJSON);
    	this.mockMvc.perform(post("/api/prescriptions/")
    		.content(newPrescriptionAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
    		.andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles="OWNER_ADMIN")
    void testCreatePrescriptionError() throws Exception {
    	Prescription newPrescription = prescriptions.get(0);
    	newPrescription.setId(null);
        newPrescription.setDescription(null);
    	ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        String newPrescriptionAsJSON = mapper.writeValueAsString(prescriptionMapper.toPrescriptionDto(newPrescription));
    	this.mockMvc.perform(post("/api/prescriptions/")
        		.content(newPrescriptionAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
        		.andExpect(status().isBadRequest());
     }

    @Test
    @WithMockUser(roles="OWNER_ADMIN")
    void testUpdatePrescriptionSuccess() throws Exception {
    	given(this.clinicService.findPrescriptionById(2)).willReturn(prescriptions.get(0));
    	Prescription newPrescription = prescriptions.get(0);
    	newPrescription.setDescription("rabies shot test");
    	ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        String newPrescriptionAsJSON = mapper.writeValueAsString(prescriptionMapper.toPrescriptionDto(newPrescription));
    	this.mockMvc.perform(put("/api/prescriptions/2")
    		.content(newPrescriptionAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
        	.andExpect(content().contentType("application/json"))
        	.andExpect(status().isNoContent());

    	this.mockMvc.perform(get("/api/prescriptions/2")
           	.accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.id").value(2))
            .andExpect(jsonPath("$.description").value("rabies shot test"));
    }

    @Test
    @WithMockUser(roles="OWNER_ADMIN")
    void testUpdatePrescriptionError() throws Exception {
    	Prescription newPrescription = prescriptions.get(0);
        newPrescription.setDescription(null);
    	ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        String newPrescriptionAsJSON = mapper.writeValueAsString(prescriptionMapper.toPrescriptionDto(newPrescription));
    	this.mockMvc.perform(put("/api/prescriptions/2")
    		.content(newPrescriptionAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
        	.andExpect(status().isBadRequest());
     }

    @Test
    @WithMockUser(roles="OWNER_ADMIN")
    void testDeletePrescriptionSuccess() throws Exception {
    	Prescription newPrescription = prescriptions.get(0);
    	ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        String newPrescriptionAsJSON = mapper.writeValueAsString(prescriptionMapper.toPrescriptionDto(newPrescription));
    	given(this.clinicService.findPrescriptionById(2)).willReturn(prescriptions.get(0));
    	this.mockMvc.perform(delete("/api/prescriptions/2")
    		.content(newPrescriptionAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
        	.andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles="OWNER_ADMIN")
    void testDeletePrescriptionError() throws Exception {
    	Prescription newPrescription = prescriptions.get(0);
    	ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        String newPrescriptionAsJSON = mapper.writeValueAsString(prescriptionMapper.toPrescriptionDto(newPrescription));
        given(this.clinicService.findPrescriptionById(999)).willReturn(null);
        this.mockMvc.perform(delete("/api/prescriptions/999")
    		.content(newPrescriptionAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
        	.andExpect(status().isNotFound());
    }

}
