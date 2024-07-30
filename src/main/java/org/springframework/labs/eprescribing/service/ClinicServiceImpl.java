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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.labs.eprescribing.model.Owner;
import org.springframework.labs.eprescribing.model.Medication;
import org.springframework.labs.eprescribing.model.MedicationType;
import org.springframework.labs.eprescribing.model.Specialty;
import org.springframework.labs.eprescribing.model.Prescriber;
import org.springframework.labs.eprescribing.model.Prescription;
import org.springframework.labs.eprescribing.repository.OwnerRepository;
import org.springframework.labs.eprescribing.repository.MedicationRepository;
import org.springframework.labs.eprescribing.repository.MedicationTypeRepository;
import org.springframework.labs.eprescribing.repository.SpecialtyRepository;
import org.springframework.labs.eprescribing.repository.PrescriberRepository;
import org.springframework.labs.eprescribing.repository.PrescriptionRepository;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Mostly used as a facade for all eprescribing controllers
 * Also a placeholder for @Transactional and @Cacheable annotations
 *
 * @author Michael Isvy
 * @author Vitaliy Fedoriv
 */
@Service
public class ClinicServiceImpl implements ClinicService {

    private MedicationRepository medicationRepository;
    private PrescriberRepository prescriberRepository;
    private OwnerRepository ownerRepository;
    private PrescriptionRepository prescriptionRepository;
    private SpecialtyRepository specialtyRepository;
	private MedicationTypeRepository medicationTypeRepository;

    @Autowired
     public ClinicServiceImpl(
       		 MedicationRepository medicationRepository,
    		 PrescriberRepository prescriberRepository,
    		 OwnerRepository ownerRepository,
    		 PrescriptionRepository prescriptionRepository,
    		 SpecialtyRepository specialtyRepository,
			 MedicationTypeRepository medicationTypeRepository) {
        this.medicationRepository = medicationRepository;
        this.prescriberRepository = prescriberRepository;
        this.ownerRepository = ownerRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.specialtyRepository = specialtyRepository;
		this.medicationTypeRepository = medicationTypeRepository;
    }

	@Override
	@Transactional(readOnly = true)
	public Collection<Medication> findAllMedications() throws DataAccessException {
		return medicationRepository.findAll();
	}

	@Override
	@Transactional
	public void deleteMedication(Medication medication) throws DataAccessException {
		medicationRepository.delete(medication);
	}

	@Override
	@Transactional(readOnly = true)
	public Prescription findPrescriptionById(int prescriptionId) throws DataAccessException {
		Prescription prescription = null;
		try {
			prescription = prescriptionRepository.findById(prescriptionId);
		} catch (ObjectRetrievalFailureException|EmptyResultDataAccessException e) {
		// just ignore not found exceptions for Jdbc/Jpa realization
			return null;
		}
		return prescription;
	}

	@Override
	@Transactional(readOnly = true)
	public Collection<Prescription> findAllPrescriptions() throws DataAccessException {
		return prescriptionRepository.findAll();
	}

	@Override
	@Transactional
	public void deletePrescription(Prescription prescription) throws DataAccessException {
		prescriptionRepository.delete(prescription);
	}

	@Override
	@Transactional(readOnly = true)
	public Prescriber findPrescriberById(int id) throws DataAccessException {
		Prescriber prescriber = null;
		try {
			prescriber = prescriberRepository.findById(id);
		} catch (ObjectRetrievalFailureException|EmptyResultDataAccessException e) {
		// just ignore not found exceptions for Jdbc/Jpa realization
			return null;
		}
		return prescriber;
	}

	@Override
	@Transactional(readOnly = true)
	public Collection<Prescriber> findAllPrescribers() throws DataAccessException {
		return prescriberRepository.findAll();
	}

	@Override
	@Transactional
	public void savePrescriber(Prescriber prescriber) throws DataAccessException {
		prescriberRepository.save(prescriber);
	}

	@Override
	@Transactional
	public void deletePrescriber(Prescriber prescriber) throws DataAccessException {
		prescriberRepository.delete(prescriber);
	}

	@Override
	@Transactional(readOnly = true)
	public Collection<Owner> findAllOwners() throws DataAccessException {
		return ownerRepository.findAll();
	}

	@Override
	@Transactional
	public void deleteOwner(Owner owner) throws DataAccessException {
		ownerRepository.delete(owner);
	}

	@Override
    @Transactional(readOnly = true)
	public MedicationType findMedicationTypeById(int medicationTypeId) {
		MedicationType medicationType = null;
		try {
			medicationType = medicationTypeRepository.findById(medicationTypeId);
		} catch (ObjectRetrievalFailureException|EmptyResultDataAccessException e) {
		// just ignore not found exceptions for Jdbc/Jpa realization
			return null;
		}
		return medicationType;
	}

	@Override
	@Transactional(readOnly = true)
	public Collection<MedicationType> findAllMedicationTypes() throws DataAccessException {
		return medicationTypeRepository.findAll();
	}

	@Override
	@Transactional
	public void saveMedicationType(MedicationType medicationType) throws DataAccessException {
		medicationTypeRepository.save(medicationType);
	}

	@Override
	@Transactional
	public void deleteMedicationType(MedicationType medicationType) throws DataAccessException {
		medicationTypeRepository.delete(medicationType);
	}

	@Override
	@Transactional(readOnly = true)
	public Specialty findSpecialtyById(int specialtyId) {
		Specialty specialty = null;
		try {
			specialty = specialtyRepository.findById(specialtyId);
		} catch (ObjectRetrievalFailureException|EmptyResultDataAccessException e) {
		// just ignore not found exceptions for Jdbc/Jpa realization
			return null;
		}
		return specialty;
	}

	@Override
	@Transactional(readOnly = true)
	public Collection<Specialty> findAllSpecialties() throws DataAccessException {
		return specialtyRepository.findAll();
	}

	@Override
	@Transactional
	public void saveSpecialty(Specialty specialty) throws DataAccessException {
		specialtyRepository.save(specialty);
	}

	@Override
	@Transactional
	public void deleteSpecialty(Specialty specialty) throws DataAccessException {
		specialtyRepository.delete(specialty);
	}

	@Override
	@Transactional(readOnly = true)
	public Collection<MedicationType> findMedicationTypes() throws DataAccessException {
		return medicationRepository.findMedicationTypes();
	}

	@Override
	@Transactional(readOnly = true)
	public Owner findOwnerById(int id) throws DataAccessException {
		Owner owner = null;
		try {
			owner = ownerRepository.findById(id);
		} catch (ObjectRetrievalFailureException|EmptyResultDataAccessException e) {
		// just ignore not found exceptions for Jdbc/Jpa realization
			return null;
		}
		return owner;
	}

	@Override
	@Transactional(readOnly = true)
	public Medication findMedicationById(int id) throws DataAccessException {
		Medication medication = null;
		try {
			medication = medicationRepository.findById(id);
		} catch (ObjectRetrievalFailureException|EmptyResultDataAccessException e) {
		// just ignore not found exceptions for Jdbc/Jpa realization
			return null;
		}
		return medication;
	}

	@Override
	@Transactional
	public void saveMedication(Medication medication) throws DataAccessException {
		medicationRepository.save(medication);
	}

	@Override
	@Transactional
	public void savePrescription(Prescription prescription) throws DataAccessException {
		prescriptionRepository.save(prescription);

	}

	@Override
	@Transactional(readOnly = true)
	public Collection<Prescriber> findPrescribers() throws DataAccessException {
		return prescriberRepository.findAll();
	}

	@Override
	@Transactional
	public void saveOwner(Owner owner) throws DataAccessException {
		ownerRepository.save(owner);

	}

	@Override
	@Transactional(readOnly = true)
	public Collection<Owner> findOwnerByLastName(String lastName) throws DataAccessException {
		return ownerRepository.findByLastName(lastName);
	}

	@Override
	@Transactional(readOnly = true)
	public Collection<Prescription> findPrescriptionsByMedicationId(int medicationId) {
		return prescriptionRepository.findByMedicationId(medicationId);
	}

    @Override
    @Transactional(readOnly = true)
    public List<Specialty> findSpecialtiesByNameIn(Set<String> names){
        List<Specialty> specialties = new ArrayList<>();
        try {
            specialties = specialtyRepository.findSpecialtiesByNameIn(names);
        } catch (ObjectRetrievalFailureException|EmptyResultDataAccessException e) {
            // just ignore not found exceptions for Jdbc/Jpa realization
            return specialties;
        }
        return specialties;
    }

    @Override
    @Transactional(readOnly = true)
    public MedicationType findMedicationTypeByName(String name){
        MedicationType medicationType;
        try {
            medicationType = medicationTypeRepository.findByName(name);
        } catch (ObjectRetrievalFailureException|EmptyResultDataAccessException e) {
            // just ignore not found exceptions for Jdbc/Jpa realization
            return null;
        }
        return medicationType;
    }
}
