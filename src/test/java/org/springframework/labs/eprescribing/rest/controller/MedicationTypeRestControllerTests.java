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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.labs.eprescribing.mapper.MedicationTypeMapper;
import org.springframework.labs.eprescribing.model.MedicationType;
import org.springframework.labs.eprescribing.rest.advice.ExceptionControllerAdvice;
import org.springframework.labs.eprescribing.service.ClinicService;
import org.springframework.labs.eprescribing.service.clinicService.ApplicationTestConfig;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


/**
 * Test class for {@link MedicationTypeRestController}
 *
 * @author Vitaliy Fedoriv
 */
@SpringBootTest
@ContextConfiguration(classes=ApplicationTestConfig.class)
@WebAppConfiguration
class MedicationTypeRestControllerTests {

    @Autowired
    private MedicationTypeRestController medicationTypeRestController;

    @Autowired
    private MedicationTypeMapper medicationTypeMapper;

    @MockBean
    private ClinicService clinicService;

    private MockMvc mockMvc;

    private List<MedicationType> medicationTypes;

    @BeforeEach
    void initMedicationTypes(){
    	this.mockMvc = MockMvcBuilders.standaloneSetup(medicationTypeRestController)
    			.setControllerAdvice(new ExceptionControllerAdvice())
    			.build();
    	medicationTypes = new ArrayList<MedicationType>();

    	MedicationType medicationType = new MedicationType();
    	medicationType.setId(1);
    	medicationType.setName("cat");
    	medicationTypes.add(medicationType);

    	medicationType = new MedicationType();
    	medicationType.setId(2);
    	medicationType.setName("dog");
    	medicationTypes.add(medicationType);

    	medicationType = new MedicationType();
    	medicationType.setId(3);
    	medicationType.setName("lizard");
    	medicationTypes.add(medicationType);

    	medicationType = new MedicationType();
    	medicationType.setId(4);
    	medicationType.setName("snake");
    	medicationTypes.add(medicationType);
    }

    @Test
    @WithMockUser(roles="OWNER_ADMIN")
    void testGetMedicationTypeSuccessAsOwnerAdmin() throws Exception {
    	given(this.clinicService.findMedicationTypeById(1)).willReturn(medicationTypes.get(0));
        this.mockMvc.perform(get("/api/medicationtypes/1")
        	.accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("cat"));
    }

    @Test
    @WithMockUser(roles="VET_ADMIN")
    void testGetMedicationTypeSuccessAsVetAdmin() throws Exception {
        given(this.clinicService.findMedicationTypeById(1)).willReturn(medicationTypes.get(0));
        this.mockMvc.perform(get("/api/medicationtypes/1")
            .accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("cat"));
    }

    @Test
    @WithMockUser(roles="OWNER_ADMIN")
    void testGetMedicationTypeNotFound() throws Exception {
    	given(this.clinicService.findMedicationTypeById(999)).willReturn(null);
        this.mockMvc.perform(get("/api/medicationtypes/999")
        	.accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles="OWNER_ADMIN")
    void testGetAllMedicationTypesSuccessAsOwnerAdmin() throws Exception {
    	medicationTypes.remove(0);
    	medicationTypes.remove(1);
    	given(this.clinicService.findAllMedicationTypes()).willReturn(medicationTypes);
        this.mockMvc.perform(get("/api/medicationtypes/")
        	.accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
        	.andExpect(jsonPath("$.[0].id").value(2))
        	.andExpect(jsonPath("$.[0].name").value("dog"))
        	.andExpect(jsonPath("$.[1].id").value(4))
        	.andExpect(jsonPath("$.[1].name").value("snake"));
    }

    @Test
    @WithMockUser(roles="VET_ADMIN")
    void testGetAllMedicationTypesSuccessAsVetAdmin() throws Exception {
        medicationTypes.remove(0);
        medicationTypes.remove(1);
        given(this.clinicService.findAllMedicationTypes()).willReturn(medicationTypes);
        this.mockMvc.perform(get("/api/medicationtypes/")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.[0].id").value(2))
            .andExpect(jsonPath("$.[0].name").value("dog"))
            .andExpect(jsonPath("$.[1].id").value(4))
            .andExpect(jsonPath("$.[1].name").value("snake"));
    }

    @Test
    @WithMockUser(roles="VET_ADMIN")
    void testGetAllMedicationTypesNotFound() throws Exception {
    	medicationTypes.clear();
    	given(this.clinicService.findAllMedicationTypes()).willReturn(medicationTypes);
        this.mockMvc.perform(get("/api/medicationtypes/")
        	.accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles="VET_ADMIN")
    void testCreateMedicationTypeSuccess() throws Exception {
    	MedicationType newMedicationType = medicationTypes.get(0);
    	newMedicationType.setId(null);
    	ObjectMapper mapper = new ObjectMapper();
        String newMedicationTypeAsJSON = mapper.writeValueAsString(medicationTypeMapper.toMedicationTypeDto(newMedicationType));
    	this.mockMvc.perform(post("/api/medicationtypes/")
    		.content(newMedicationTypeAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
    		.andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles="VET_ADMIN")
    void testCreateMedicationTypeError() throws Exception {
    	MedicationType newMedicationType = medicationTypes.get(0);
    	newMedicationType.setId(null);
    	newMedicationType.setName(null);
    	ObjectMapper mapper = new ObjectMapper();
        String newMedicationTypeAsJSON = mapper.writeValueAsString(medicationTypeMapper.toMedicationTypeDto(newMedicationType));
    	this.mockMvc.perform(post("/api/medicationtypes/")
        		.content(newMedicationTypeAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
        		.andExpect(status().isBadRequest());
     }
    @Test
    @WithMockUser(roles="VET_ADMIN")
    void testCreateMedicationTypeErrorWithId() throws Exception {
        MedicationType newMedicationType = medicationTypes.get(0);
        newMedicationType.setId(1);
        ObjectMapper mapper = new ObjectMapper();
        String newMedicationTypeAsJSON = mapper.writeValueAsString(medicationTypeMapper.toMedicationTypeDto(newMedicationType));
        this.mockMvc.perform(post("/api/medicationtypes/")
                .content(newMedicationTypeAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isBadRequest());
    }
    @Test
    @WithMockUser(roles="VET_ADMIN")
    void testUpdateMedicationTypeSuccess() throws Exception {
    	given(this.clinicService.findMedicationTypeById(2)).willReturn(medicationTypes.get(1));
    	MedicationType newMedicationType = medicationTypes.get(1);
    	newMedicationType.setName("dog I");
    	ObjectMapper mapper = new ObjectMapper();
        String newMedicationTypeAsJSON = mapper.writeValueAsString(medicationTypeMapper.toMedicationTypeDto(newMedicationType));
    	this.mockMvc.perform(put("/api/medicationtypes/2")
    		.content(newMedicationTypeAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
        	.andExpect(content().contentType("application/json"))
        	.andExpect(status().isNoContent());

    	this.mockMvc.perform(get("/api/medicationtypes/2")
           	.accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.id").value(2))
            .andExpect(jsonPath("$.name").value("dog I"));
    }

    @Test
    @WithMockUser(roles="VET_ADMIN")
    void testUpdateMedicationTypeError() throws Exception {
    	MedicationType newMedicationType = medicationTypes.get(0);
    	newMedicationType.setName("");
    	ObjectMapper mapper = new ObjectMapper();
        String newMedicationTypeAsJSON = mapper.writeValueAsString(medicationTypeMapper.toMedicationTypeDto(newMedicationType));
    	this.mockMvc.perform(put("/api/medicationtypes/1")
    		.content(newMedicationTypeAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
        	.andExpect(status().isBadRequest());
     }

    @Test
    @WithMockUser(roles="VET_ADMIN")
    void testDeleteMedicationTypeSuccess() throws Exception {
    	MedicationType newMedicationType = medicationTypes.get(0);
    	ObjectMapper mapper = new ObjectMapper();
    	String newMedicationTypeAsJSON = mapper.writeValueAsString(newMedicationType);
    	given(this.clinicService.findMedicationTypeById(1)).willReturn(medicationTypes.get(0));
    	this.mockMvc.perform(delete("/api/medicationtypes/1")
    		.content(newMedicationTypeAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
        	.andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles="VET_ADMIN")
    void testDeleteMedicationTypeError() throws Exception {
    	MedicationType newMedicationType = medicationTypes.get(0);
    	ObjectMapper mapper = new ObjectMapper();
        String newMedicationTypeAsJSON = mapper.writeValueAsString(medicationTypeMapper.toMedicationTypeDto(newMedicationType));
    	given(this.clinicService.findMedicationTypeById(999)).willReturn(null);
    	this.mockMvc.perform(delete("/api/medicationtypes/999")
    		.content(newMedicationTypeAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
        	.andExpect(status().isNotFound());
    }

}
