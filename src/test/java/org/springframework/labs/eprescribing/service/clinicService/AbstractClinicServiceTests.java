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
package org.springframework.labs.eprescribing.service.clinicService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.labs.eprescribing.model.*;
import org.springframework.labs.eprescribing.service.ClinicService;
import org.springframework.labs.eprescribing.util.EntityUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * <p> Base class for {@link ClinicService} integration tests. </p> <p> Subclasses should specify Spring context
 * configuration using {@link ContextConfiguration @ContextConfiguration} annotation </p> <p>
 * AbstractclinicServiceTests and its subclasses benefit from the following services provided by the Spring
 * TestContext Framework: </p> <ul> <li><strong>Spring IoC container caching</strong> which spares us unnecessary set up
 * time between test execution.</li> <li><strong>Dependency Injection</strong> of test fixture instances, meaning that
 * we don't need to perform application context lookups. See the use of {@link Autowired @Autowired} on the <code>{@link
 * AbstractClinicServiceTests#clinicService clinicService}</code> instance variable, which uses autowiring <em>by
 * type</em>. <li><strong>Transaction management</strong>, meaning each test method is executed in its own transaction,
 * which is automatically rolled back by default. Thus, even if tests insert or otherwise change database state, there
 * is no need for a teardown or cleanup script. <li> An {@link org.springframework.context.ApplicationContext
 * ApplicationContext} is also inherited and can be used for explicit bean lookup if necessary. </li> </ul>
 *
 * @author Ken Krebs
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @author Michael Isvy
 * @author Vitaliy Fedoriv
 */
abstract class AbstractClinicServiceTests {

    @Autowired
    protected ClinicService clinicService;

    @Test
    void shouldFindOwnersByLastName() {
        Collection<Owner> owners = this.clinicService.findOwnerByLastName("Davis");
        assertThat(owners.size()).isEqualTo(2);

        owners = this.clinicService.findOwnerByLastName("Daviss");
        assertThat(owners.isEmpty()).isTrue();
    }

    @Test
    void shouldFindSingleOwnerWithMedication() {
        Owner owner = this.clinicService.findOwnerById(1);
        assertThat(owner.getLastName()).startsWith("Franklin");
        assertThat(owner.getMedications().size()).isEqualTo(1);
        assertThat(owner.getMedications().get(0).getType()).isNotNull();
        assertThat(owner.getMedications().get(0).getType().getName()).isEqualTo("cat");
    }

    @Test
    @Transactional
    void shouldInsertOwner() {
        Collection<Owner> owners = this.clinicService.findOwnerByLastName("Schultz");
        int found = owners.size();

        Owner owner = new Owner();
        owner.setFirstName("Sam");
        owner.setLastName("Schultz");
        owner.setAddress("4, Evans Street");
        owner.setCity("Wollongong");
        owner.setTelephone("4444444444");
        this.clinicService.saveOwner(owner);
        assertThat(owner.getId().longValue()).isNotEqualTo(0);
        assertThat(owner.getMedication("null value")).isNull();
        owners = this.clinicService.findOwnerByLastName("Schultz");
        assertThat(owners.size()).isEqualTo(found + 1);
    }

    @Test
    @Transactional
    void shouldUpdateOwner() {
        Owner owner = this.clinicService.findOwnerById(1);
        String oldLastName = owner.getLastName();
        String newLastName = oldLastName + "X";

        owner.setLastName(newLastName);
        this.clinicService.saveOwner(owner);

        // retrieving new name from database
        owner = this.clinicService.findOwnerById(1);
        assertThat(owner.getLastName()).isEqualTo(newLastName);
    }

    @Test
    void shouldFindMedicationWithCorrectId() {
        Medication medication7 = this.clinicService.findMedicationById(7);
        assertThat(medication7.getName()).startsWith("Samantha");
        assertThat(medication7.getOwner().getFirstName()).isEqualTo("Jean");

    }

//    @Test
//    void shouldFindAllMedicationTypes() {
//        Collection<MedicationType> medicationTypes = this.clinicService.findMedicationTypes();
//
//        MedicationType medicationType1 = EntityUtils.getById(medicationTypes, MedicationType.class, 1);
//        assertThat(medicationType1.getName()).isEqualTo("cat");
//        MedicationType medicationType4 = EntityUtils.getById(medicationTypes, MedicationType.class, 4);
//        assertThat(medicationType4.getName()).isEqualTo("snake");
//    }

    @Test
    @Transactional
    void shouldInsertMedicationIntoDatabaseAndGenerateId() {
        Owner owner6 = this.clinicService.findOwnerById(6);
        int found = owner6.getMedications().size();

        Medication medication = new Medication();
        medication.setName("bowser");
        Collection<MedicationType> types = this.clinicService.findMedicationTypes();
        medication.setType(EntityUtils.getById(types, MedicationType.class, 2));
        medication.setExpirationDate(LocalDate.now());
        owner6.addMedication(medication);
        assertThat(owner6.getMedications().size()).isEqualTo(found + 1);

        this.clinicService.saveMedication(medication);
        this.clinicService.saveOwner(owner6);

        owner6 = this.clinicService.findOwnerById(6);
        assertThat(owner6.getMedications().size()).isEqualTo(found + 1);
        // checks that id has been generated
        assertThat(medication.getId()).isNotNull();
    }

    @Test
    @Transactional
    void shouldUpdateMedicationName() throws Exception {
        Medication medication7 = this.clinicService.findMedicationById(7);
        String oldName = medication7.getName();

        String newName = oldName + "X";
        medication7.setName(newName);
        this.clinicService.saveMedication(medication7);

        medication7 = this.clinicService.findMedicationById(7);
        assertThat(medication7.getName()).isEqualTo(newName);
    }

    @Test
    void shouldFindVets() {
        Collection<Vet> vets = this.clinicService.findVets();

        Vet vet = EntityUtils.getById(vets, Vet.class, 3);
        assertThat(vet.getLastName()).isEqualTo("Douglas");
        assertThat(vet.getNrOfSpecialties()).isEqualTo(2);
        assertThat(vet.getSpecialties().get(0).getName()).isEqualTo("dentistry");
        assertThat(vet.getSpecialties().get(1).getName()).isEqualTo("surgery");
    }

    @Test
    @Transactional
    void shouldAddNewPrescriptionForMedication() {
        Medication medication7 = this.clinicService.findMedicationById(7);
        int found = medication7.getPrescriptions().size();
        Prescription prescription = new Prescription();
        medication7.addPrescription(prescription);
        prescription.setDescription("test");
        this.clinicService.savePrescription(prescription);
        this.clinicService.saveMedication(medication7);

        medication7 = this.clinicService.findMedicationById(7);
        assertThat(medication7.getPrescriptions().size()).isEqualTo(found + 1);
        assertThat(prescription.getId()).isNotNull();
    }

    @Test
       void shouldFindPrescriptionsByMedicationId() throws Exception {
        Collection<Prescription> prescriptions = this.clinicService.findPrescriptionsByMedicationId(7);
        assertThat(prescriptions.size()).isEqualTo(2);
        Prescription[] prescriptionArr = prescriptions.toArray(new Prescription[prescriptions.size()]);
        assertThat(prescriptionArr[0].getMedication()).isNotNull();
        assertThat(prescriptionArr[0].getDate()).isNotNull();
        assertThat(prescriptionArr[0].getMedication().getId()).isEqualTo(7);
    }

    @Test
    void shouldFindAllMedications(){
        Collection<Medication> medications = this.clinicService.findAllMedications();
        Medication medication1 = EntityUtils.getById(medications, Medication.class, 1);
        assertThat(medication1.getName()).isEqualTo("Leo");
        Medication medication3 = EntityUtils.getById(medications, Medication.class, 3);
        assertThat(medication3.getName()).isEqualTo("Rosy");
    }

    @Test
    @Transactional
    void shouldDeleteMedication(){
        Medication medication = this.clinicService.findMedicationById(1);
        this.clinicService.deleteMedication(medication);
        try {
            medication = this.clinicService.findMedicationById(1);
		} catch (Exception e) {
			medication = null;
		}
        assertThat(medication).isNull();
    }

    @Test
    void shouldFindPrescriptionDyId(){
    	Prescription prescription = this.clinicService.findPrescriptionById(1);
    	assertThat(prescription.getId()).isEqualTo(1);
    	assertThat(prescription.getMedication().getName()).isEqualTo("Samantha");
    }

    @Test
    void shouldFindAllPrescriptions(){
        Collection<Prescription> prescriptions = this.clinicService.findAllPrescriptions();
        Prescription prescription1 = EntityUtils.getById(prescriptions, Prescription.class, 1);
        assertThat(prescription1.getMedication().getName()).isEqualTo("Samantha");
        Prescription prescription3 = EntityUtils.getById(prescriptions, Prescription.class, 3);
        assertThat(prescription3.getMedication().getName()).isEqualTo("Max");
    }

    @Test
    @Transactional
    void shouldInsertPrescription() {
        Collection<Prescription> prescriptions = this.clinicService.findAllPrescriptions();
        int found = prescriptions.size();

        Medication medication = this.clinicService.findMedicationById(1);

        Prescription prescription = new Prescription();
        prescription.setMedication(medication);
        prescription.setDate(LocalDate.now());
        prescription.setDescription("new prescription");


        this.clinicService.savePrescription(prescription);
        assertThat(prescription.getId().longValue()).isNotEqualTo(0);

        prescriptions = this.clinicService.findAllPrescriptions();
        assertThat(prescriptions.size()).isEqualTo(found + 1);
    }

    @Test
    @Transactional
    void shouldUpdatePrescription(){
    	Prescription prescription = this.clinicService.findPrescriptionById(1);
    	String oldDesc = prescription.getDescription();
        String newDesc = oldDesc + "X";
        prescription.setDescription(newDesc);
        this.clinicService.savePrescription(prescription);
        prescription = this.clinicService.findPrescriptionById(1);
        assertThat(prescription.getDescription()).isEqualTo(newDesc);
    }

    @Test
    @Transactional
    void shouldDeletePrescription(){
    	Prescription prescription = this.clinicService.findPrescriptionById(1);
        this.clinicService.deletePrescription(prescription);
        try {
        	prescription = this.clinicService.findPrescriptionById(1);
		} catch (Exception e) {
			prescription = null;
		}
        assertThat(prescription).isNull();
    }

    @Test
    void shouldFindVetDyId(){
    	Vet vet = this.clinicService.findVetById(1);
    	assertThat(vet.getFirstName()).isEqualTo("James");
    	assertThat(vet.getLastName()).isEqualTo("Carter");
    }

    @Test
    @Transactional
    void shouldInsertVet() {
        Collection<Vet> vets = this.clinicService.findAllVets();
        int found = vets.size();

        Vet vet = new Vet();
        vet.setFirstName("John");
        vet.setLastName("Dow");

        this.clinicService.saveVet(vet);
        assertThat(vet.getId().longValue()).isNotEqualTo(0);

        vets = this.clinicService.findAllVets();
        assertThat(vets.size()).isEqualTo(found + 1);
    }

    @Test
    @Transactional
    void shouldUpdateVet(){
    	Vet vet = this.clinicService.findVetById(1);
    	String oldLastName = vet.getLastName();
        String newLastName = oldLastName + "X";
        vet.setLastName(newLastName);
        this.clinicService.saveVet(vet);
        vet = this.clinicService.findVetById(1);
        assertThat(vet.getLastName()).isEqualTo(newLastName);
    }

    @Test
    @Transactional
    void shouldDeleteVet(){
    	Vet vet = this.clinicService.findVetById(1);
        this.clinicService.deleteVet(vet);
        try {
        	vet = this.clinicService.findVetById(1);
		} catch (Exception e) {
			vet = null;
		}
        assertThat(vet).isNull();
    }

    @Test
    void shouldFindAllOwners(){
        Collection<Owner> owners = this.clinicService.findAllOwners();
        Owner owner1 = EntityUtils.getById(owners, Owner.class, 1);
        assertThat(owner1.getFirstName()).isEqualTo("George");
        Owner owner3 = EntityUtils.getById(owners, Owner.class, 3);
        assertThat(owner3.getFirstName()).isEqualTo("Eduardo");
    }

    @Test
    @Transactional
    void shouldDeleteOwner(){
    	Owner owner = this.clinicService.findOwnerById(1);
        this.clinicService.deleteOwner(owner);
        try {
        	owner = this.clinicService.findOwnerById(1);
		} catch (Exception e) {
			owner = null;
		}
        assertThat(owner).isNull();
    }

    @Test
    void shouldFindMedicationTypeById(){
    	MedicationType medicationType = this.clinicService.findMedicationTypeById(1);
    	assertThat(medicationType.getName()).isEqualTo("cat");
    }

    @Test
    void shouldFindAllMedicationTypes(){
        Collection<MedicationType> medicationTypes = this.clinicService.findAllMedicationTypes();
        MedicationType medicationType1 = EntityUtils.getById(medicationTypes, MedicationType.class, 1);
        assertThat(medicationType1.getName()).isEqualTo("cat");
        MedicationType medicationType3 = EntityUtils.getById(medicationTypes, MedicationType.class, 3);
        assertThat(medicationType3.getName()).isEqualTo("lizard");
    }

    @Test
    @Transactional
    void shouldInsertMedicationType() {
        Collection<MedicationType> medicationTypes = this.clinicService.findAllMedicationTypes();
        int found = medicationTypes.size();

        MedicationType medicationType = new MedicationType();
        medicationType.setName("tiger");

        this.clinicService.saveMedicationType(medicationType);
        assertThat(medicationType.getId().longValue()).isNotEqualTo(0);

        medicationTypes = this.clinicService.findAllMedicationTypes();
        assertThat(medicationTypes.size()).isEqualTo(found + 1);
    }

    @Test
    @Transactional
    void shouldUpdateMedicationType(){
    	MedicationType medicationType = this.clinicService.findMedicationTypeById(1);
    	String oldLastName = medicationType.getName();
        String newLastName = oldLastName + "X";
        medicationType.setName(newLastName);
        this.clinicService.saveMedicationType(medicationType);
        medicationType = this.clinicService.findMedicationTypeById(1);
        assertThat(medicationType.getName()).isEqualTo(newLastName);
    }

    @Test
    @Transactional
    void shouldDeleteMedicationType(){
    	MedicationType medicationType = this.clinicService.findMedicationTypeById(1);
        this.clinicService.deleteMedicationType(medicationType);
        try {
        	medicationType = this.clinicService.findMedicationTypeById(1);
		} catch (Exception e) {
			medicationType = null;
		}
        assertThat(medicationType).isNull();
    }

    @Test
    void shouldFindSpecialtyById(){
    	Specialty specialty = this.clinicService.findSpecialtyById(1);
    	assertThat(specialty.getName()).isEqualTo("radiology");
    }

    @Test
    void shouldFindAllSpecialtys(){
        Collection<Specialty> specialties = this.clinicService.findAllSpecialties();
        Specialty specialty1 = EntityUtils.getById(specialties, Specialty.class, 1);
        assertThat(specialty1.getName()).isEqualTo("radiology");
        Specialty specialty3 = EntityUtils.getById(specialties, Specialty.class, 3);
        assertThat(specialty3.getName()).isEqualTo("dentistry");
    }

    @Test
    @Transactional
    void shouldInsertSpecialty() {
        Collection<Specialty> specialties = this.clinicService.findAllSpecialties();
        int found = specialties.size();

        Specialty specialty = new Specialty();
        specialty.setName("dermatologist");

        this.clinicService.saveSpecialty(specialty);
        assertThat(specialty.getId().longValue()).isNotEqualTo(0);

        specialties = this.clinicService.findAllSpecialties();
        assertThat(specialties.size()).isEqualTo(found + 1);
    }

    @Test
    @Transactional
    void shouldUpdateSpecialty(){
    	Specialty specialty = this.clinicService.findSpecialtyById(1);
    	String oldLastName = specialty.getName();
        String newLastName = oldLastName + "X";
        specialty.setName(newLastName);
        this.clinicService.saveSpecialty(specialty);
        specialty = this.clinicService.findSpecialtyById(1);
        assertThat(specialty.getName()).isEqualTo(newLastName);
    }

    @Test
    @Transactional
    void shouldDeleteSpecialty(){
        Specialty specialty = new Specialty();
        specialty.setName("test");
        this.clinicService.saveSpecialty(specialty);
        Integer specialtyId = specialty.getId();
        assertThat(specialtyId).isNotNull();
    	specialty = this.clinicService.findSpecialtyById(specialtyId);
        assertThat(specialty).isNotNull();
        this.clinicService.deleteSpecialty(specialty);
        try {
        	specialty = this.clinicService.findSpecialtyById(specialtyId);
		} catch (Exception e) {
			specialty = null;
		}
        assertThat(specialty).isNull();
    }

    @Test
    @Transactional
    void shouldFindSpecialtiesByNameIn() {
        Specialty specialty1 = new Specialty();
        specialty1.setName("radiology");
        specialty1.setId(1);
        Specialty specialty2 = new Specialty();
        specialty2.setName("surgery");
        specialty2.setId(2);
        Specialty specialty3 = new Specialty();
        specialty3.setName("dentistry");
        specialty3.setId(3);
        List<Specialty> expectedSpecialties = List.of(specialty1, specialty2, specialty3);
        Set<String> specialtyNames = expectedSpecialties.stream()
            .map(Specialty::getName)
            .collect(Collectors.toSet());
        Collection<Specialty> actualSpecialties = this.clinicService.findSpecialtiesByNameIn(specialtyNames);
        assertThat(actualSpecialties).isNotNull();
        assertThat(actualSpecialties.size()).isEqualTo(expectedSpecialties.size());
        for (Specialty expected : expectedSpecialties) {
            assertThat(actualSpecialties.stream()
                .anyMatch(
                    actual -> actual.getName().equals(expected.getName())
                    && actual.getId().equals(expected.getId()))).isTrue();
        }
    }

    @Test
    @Transactional
    void shouldFindMedicationTypeByName(){
        MedicationType medicationType = this.clinicService.findMedicationTypeByName("cat");
        assertThat(medicationType.getId()).isEqualTo(1);
    }
}
