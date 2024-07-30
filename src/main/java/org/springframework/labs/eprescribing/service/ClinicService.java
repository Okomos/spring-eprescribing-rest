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
package org.springframework.labs.eprescribing.service;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.springframework.dao.DataAccessException;
import org.springframework.labs.eprescribing.model.Owner;
import org.springframework.labs.eprescribing.model.Medication;
import org.springframework.labs.eprescribing.model.MedicationType;
import org.springframework.labs.eprescribing.model.Specialty;
import org.springframework.labs.eprescribing.model.Prescriber;
import org.springframework.labs.eprescribing.model.Prescription;

/**
 * Mostly used as a facade so all controllers have a single point of entry
 *
 * @author Michael Isvy
 * @author Vitaliy Fedoriv
 */
public interface ClinicService {

	Medication findMedicationById(int id) throws DataAccessException;
	Collection<Medication> findAllMedications() throws DataAccessException;
	void saveMedication(Medication medication) throws DataAccessException;
	void deleteMedication(Medication medication) throws DataAccessException;

	Collection<Prescription> findPrescriptionsByMedicationId(int medicationId);
	Prescription findPrescriptionById(int prescriptionId) throws DataAccessException;
	Collection<Prescription> findAllPrescriptions() throws DataAccessException;
	void savePrescription(Prescription prescription) throws DataAccessException;
	void deletePrescription(Prescription prescription) throws DataAccessException;
	Prescriber findPrescriberById(int id) throws DataAccessException;
	Collection<Prescriber> findPrescribers() throws DataAccessException;
	Collection<Prescriber> findAllPrescribers() throws DataAccessException;
	void savePrescriber(Prescriber prescriber) throws DataAccessException;
	void deletePrescriber(Prescriber prescriber) throws DataAccessException;
	Owner findOwnerById(int id) throws DataAccessException;
	Collection<Owner> findAllOwners() throws DataAccessException;
	void saveOwner(Owner owner) throws DataAccessException;
	void deleteOwner(Owner owner) throws DataAccessException;
	Collection<Owner> findOwnerByLastName(String lastName) throws DataAccessException;

	MedicationType findMedicationTypeById(int medicationTypeId);
	Collection<MedicationType> findAllMedicationTypes() throws DataAccessException;
	Collection<MedicationType> findMedicationTypes() throws DataAccessException;
	void saveMedicationType(MedicationType medicationType) throws DataAccessException;
	void deleteMedicationType(MedicationType medicationType) throws DataAccessException;
	Specialty findSpecialtyById(int specialtyId);
	Collection<Specialty> findAllSpecialties() throws DataAccessException;
	void saveSpecialty(Specialty specialty) throws DataAccessException;
	void deleteSpecialty(Specialty specialty) throws DataAccessException;

    List<Specialty> findSpecialtiesByNameIn(Set<String> names) throws DataAccessException;

    MedicationType findMedicationTypeByName(String name) throws DataAccessException;
}
