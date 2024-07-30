/*
 * Copyright 2002-2017 the original author or authors.
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
package org.springframework.labs.eprescribing.repository.springdatajpa;

import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessException;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.labs.eprescribing.model.Medication;
import org.springframework.labs.eprescribing.model.MedicationType;
import org.springframework.labs.eprescribing.repository.MedicationRepository;

/**
 * Spring Data JPA specialization of the {@link MedicationRepository} interface
 *
 * @author Michael Isvy
 * @author Vitaliy Fedoriv
 */

@Profile("spring-data-jpa")
public interface SpringDataMedicationRepository extends MedicationRepository, Repository<Medication, Integer>, MedicationRepositoryOverride {

    @Override
    @Query("SELECT ptype FROM MedicationType ptype ORDER BY ptype.name")
    List<MedicationType> findMedicationTypes() throws DataAccessException;
}