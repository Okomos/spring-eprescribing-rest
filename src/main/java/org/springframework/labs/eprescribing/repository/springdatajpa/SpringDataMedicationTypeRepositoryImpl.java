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

package org.springframework.labs.eprescribing.repository.springdatajpa;

import org.springframework.context.annotation.Profile;
import org.springframework.labs.eprescribing.model.Medication;
import org.springframework.labs.eprescribing.model.MedicationType;
import org.springframework.labs.eprescribing.model.Prescription;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

/**
 * @author Vitaliy Fedoriv
 *
 */

@Profile("spring-data-jpa")
public class SpringDataMedicationTypeRepositoryImpl implements MedicationTypeRepositoryOverride {

	@PersistenceContext
    private EntityManager em;

	@SuppressWarnings("unchecked")
	@Override
	public void delete(MedicationType medicationType) {
        this.em.remove(this.em.contains(medicationType) ? medicationType : this.em.merge(medicationType));
		Integer medicationTypeId = medicationType.getId();

		List<Medication> medications = this.em.createQuery("SELECT medication FROM Medication medication WHERE type.id=" + medicationTypeId).getResultList();
		for (Medication medication : medications){
			List<Prescription> prescriptions = medication.getPrescriptions();
			for (Prescription prescription : prescriptions){
				this.em.createQuery("DELETE FROM Prescription prescription WHERE id=" + prescription.getId()).executeUpdate();
			}
			this.em.createQuery("DELETE FROM Medication medication WHERE id=" + medication.getId()).executeUpdate();
		}
		this.em.createQuery("DELETE FROM MedicationType medicationtype WHERE id=" + medicationTypeId).executeUpdate();
	}

}
