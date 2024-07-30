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
package org.springframework.labs.eprescribing.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.labs.eprescribing.model.BaseEntity;
import org.springframework.labs.eprescribing.model.Medication;
import org.springframework.labs.eprescribing.model.MedicationType;

/**
 * Repository class for <code>Medication</code> domain objects All method names are compliant with Spring Data naming
 * conventions so this interface can easily be extended for Spring Data See here: http://static.springsource.org/spring-data/jpa/docs/current/reference/html/jpa.repositories.html#jpa.query-methods.query-creation
 *
 * @author Ken Krebs
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @author Michael Isvy
 * @author Vitaliy Fedoriv
 */
public interface MedicationRepository {

    /**
     * Retrieve all <code>MedicationType</code>s from the data store.
     *
     * @return a <code>Collection</code> of <code>MedicationType</code>s
     */
    List<MedicationType> findMedicationTypes() throws DataAccessException;

    /**
     * Retrieve a <code>Medication</code> from the data store by id.
     *
     * @param id the id to search for
     * @return the <code>Medication</code> if found
     * @throws org.springframework.dao.DataRetrievalFailureException if not found
     */
    Medication findById(int id) throws DataAccessException;

    /**
     * Save a <code>Medication</code> to the data store, either inserting or updating it.
     *
     * @param medication the <code>Medication</code> to save
     * @see BaseEntity#isNew
     */
    void save(Medication medication) throws DataAccessException;
    
    /**
     * Retrieve <code>Medication</code>s from the data store, returning all owners 
     *
     * @return a <code>Collection</code> of <code>Medication</code>s (or an empty <code>Collection</code> if none
     * found)
     */
	Collection<Medication> findAll() throws DataAccessException;

    /**
     * Delete an <code>Medication</code> to the data store by <code>Medication</code>.
     *
     * @param medication the <code>Medication</code> to delete
     * 
     */
	void delete(Medication medication) throws DataAccessException;

}
