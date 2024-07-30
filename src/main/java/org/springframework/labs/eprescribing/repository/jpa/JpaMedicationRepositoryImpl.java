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
package org.springframework.labs.eprescribing.repository.jpa;

import java.util.Collection;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessException;
import org.springframework.labs.eprescribing.model.Medication;
import org.springframework.labs.eprescribing.model.MedicationType;
import org.springframework.labs.eprescribing.repository.MedicationRepository;
import org.springframework.stereotype.Repository;

/**
 * JPA implementation of the {@link MedicationRepository} interface.
 *
 * @author Mike Keith
 * @author Rod Johnson
 * @author Sam Brannen
 * @author Michael Isvy
 * @author Vitaliy Fedoriv
 */
@Repository
@Profile("jpa")
public class JpaMedicationRepositoryImpl implements MedicationRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    @SuppressWarnings("unchecked")
    public List<MedicationType> findMedicationTypes() {
        return this.em.createQuery("SELECT ptype FROM MedicationType ptype ORDER BY ptype.name").getResultList();
    }

    @Override
    public Medication findById(int id) {
        return this.em.find(Medication.class, id);
    }

    @Override
    public void save(Medication medication) {
        if (medication.getId() == null) {
            this.em.persist(medication);
        } else {
            this.em.merge(medication);
        }
    }

	@SuppressWarnings("unchecked")
	@Override
	public Collection<Medication> findAll() throws DataAccessException {
		return this.em.createQuery("SELECT medication FROM Medication medication").getResultList();
	}

	@Override
	public void delete(Medication medication) throws DataAccessException {
		//this.em.remove(this.em.contains(medication) ? medication : this.em.merge(medication));
		String medicationId = medication.getId().toString();
		this.em.createQuery("DELETE FROM Prescription prescription WHERE medication.id=" + medicationId).executeUpdate();
		this.em.createQuery("DELETE FROM Medication medication WHERE id=" + medicationId).executeUpdate();
		if (em.contains(medication)) {
			em.remove(medication);
		}
	}

}
