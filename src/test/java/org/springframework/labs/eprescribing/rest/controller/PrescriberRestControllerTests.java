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
import org.springframework.labs.eprescribing.mapper.PrescriberMapper;
import org.springframework.labs.eprescribing.model.Prescriber;
import org.springframework.labs.eprescribing.rest.advice.ExceptionControllerAdvice;
import org.springframework.labs.eprescribing.rest.controller.PrescriberRestController;
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
 * Test class for {@link PrescriberRestController}
 *
 * @author Vitaliy Fedoriv
 */
@SpringBootTest
@ContextConfiguration(classes=ApplicationTestConfig.class)
@WebAppConfiguration
class PrescriberRestControllerTests {

    @Autowired
    private PrescriberRestController prescriberRestController;

    @Autowired
    private PrescriberMapper prescriberMapper;

	@MockBean
    private ClinicService clinicService;

    private MockMvc mockMvc;

    private List<Prescriber> prescribers;

    @BeforeEach
    void initPrescribers(){
    	this.mockMvc = MockMvcBuilders.standaloneSetup(prescriberRestController)
    			.setControllerAdvice(new ExceptionControllerAdvice())
    			.build();
    	prescribers = new ArrayList<Prescriber>();


    	Prescriber prescriber = new Prescriber();
    	prescriber.setId(1);
    	prescriber.setFirstName("James");
    	prescriber.setLastName("Carter");
    	prescribers.add(prescriber);

    	prescriber = new Prescriber();
    	prescriber.setId(2);
    	prescriber.setFirstName("Helen");
    	prescriber.setLastName("Leary");
    	prescribers.add(prescriber);

    	prescriber = new Prescriber();
    	prescriber.setId(3);
    	prescriber.setFirstName("Linda");
    	prescriber.setLastName("Douglas");
    	prescribers.add(prescriber);
    }

    @Test
    @WithMockUser(roles="VET_ADMIN")
    void testGetPrescriberSuccess() throws Exception {
    	given(this.clinicService.findPrescriberById(1)).willReturn(prescribers.get(0));
        this.mockMvc.perform(get("/api/prescribers/1")
        	.accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.firstName").value("James"));
    }

    @Test
    @WithMockUser(roles="VET_ADMIN")
    void testGetPrescriberNotFound() throws Exception {
    	given(this.clinicService.findPrescriberById(-1)).willReturn(null);
        this.mockMvc.perform(get("/api/prescribers/999")
        	.accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles="VET_ADMIN")
    void testGetAllPrescribersSuccess() throws Exception {
    	given(this.clinicService.findAllPrescribers()).willReturn(prescribers);
        this.mockMvc.perform(get("/api/prescribers/")
        	.accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.[0].id").value(1))
            .andExpect(jsonPath("$.[0].firstName").value("James"))
            .andExpect(jsonPath("$.[1].id").value(2))
            .andExpect(jsonPath("$.[1].firstName").value("Helen"));
    }

    @Test
    @WithMockUser(roles="VET_ADMIN")
    void testGetAllPrescribersNotFound() throws Exception {
    	prescribers.clear();
    	given(this.clinicService.findAllPrescribers()).willReturn(prescribers);
        this.mockMvc.perform(get("/api/prescribers/")
        	.accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles="VET_ADMIN")
    void testCreatePrescriberSuccess() throws Exception {
    	Prescriber newPrescriber = prescribers.get(0);
    	newPrescriber.setId(999);
    	ObjectMapper mapper = new ObjectMapper();
        String newPrescriberAsJSON = mapper.writeValueAsString(prescriberMapper.toPrescriberDto(newPrescriber));
    	this.mockMvc.perform(post("/api/prescribers/")
    		.content(newPrescriberAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
    		.andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles="VET_ADMIN")
    void testCreatePrescriberError() throws Exception {
    	Prescriber newPrescriber = prescribers.get(0);
    	newPrescriber.setId(null);
    	newPrescriber.setFirstName(null);
    	ObjectMapper mapper = new ObjectMapper();
        String newPrescriberAsJSON = mapper.writeValueAsString(prescriberMapper.toPrescriberDto(newPrescriber));
    	this.mockMvc.perform(post("/api/prescribers/")
        		.content(newPrescriberAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
        		.andExpect(status().isBadRequest());
     }

    @Test
    @WithMockUser(roles="VET_ADMIN")
    void testUpdatePrescriberSuccess() throws Exception {
    	given(this.clinicService.findPrescriberById(1)).willReturn(prescribers.get(0));
    	Prescriber newPrescriber = prescribers.get(0);
    	newPrescriber.setFirstName("James");
    	ObjectMapper mapper = new ObjectMapper();
        String newPrescriberAsJSON = mapper.writeValueAsString(prescriberMapper.toPrescriberDto(newPrescriber));
    	this.mockMvc.perform(put("/api/prescribers/1")
    		.content(newPrescriberAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
        	.andExpect(content().contentType("application/json"))
        	.andExpect(status().isNoContent());

    	this.mockMvc.perform(get("/api/prescribers/1")
           	.accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.firstName").value("James"));

    }

    @Test
    @WithMockUser(roles="VET_ADMIN")
    void testUpdatePrescriberError() throws Exception {
    	Prescriber newPrescriber = prescribers.get(0);
    	newPrescriber.setFirstName(null);
    	ObjectMapper mapper = new ObjectMapper();
        String newPrescriberAsJSON = mapper.writeValueAsString(prescriberMapper.toPrescriberDto(newPrescriber));
    	this.mockMvc.perform(put("/api/prescribers/1")
    		.content(newPrescriberAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
        	.andExpect(status().isBadRequest());
     }

    @Test
    @WithMockUser(roles="VET_ADMIN")
    void testDeletePrescriberSuccess() throws Exception {
    	Prescriber newPrescriber = prescribers.get(0);
    	ObjectMapper mapper = new ObjectMapper();
        String newPrescriberAsJSON = mapper.writeValueAsString(prescriberMapper.toPrescriberDto(newPrescriber));
    	given(this.clinicService.findPrescriberById(1)).willReturn(prescribers.get(0));
    	this.mockMvc.perform(delete("/api/prescribers/1")
    		.content(newPrescriberAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
        	.andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles="VET_ADMIN")
    void testDeletePrescriberError() throws Exception {
    	Prescriber newPrescriber = prescribers.get(0);
    	ObjectMapper mapper = new ObjectMapper();
        String newPrescriberAsJSON = mapper.writeValueAsString(prescriberMapper.toPrescriberDto(newPrescriber));
    	given(this.clinicService.findPrescriberById(-1)).willReturn(null);
    	this.mockMvc.perform(delete("/api/prescribers/999")
    		.content(newPrescriberAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
        	.andExpect(status().isNotFound());
    }

}
