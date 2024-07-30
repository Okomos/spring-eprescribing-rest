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
package org.springframework.labs.eprescribing.repository.jdbc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.labs.eprescribing.model.Owner;
import org.springframework.labs.eprescribing.model.Medication;
import org.springframework.labs.eprescribing.model.MedicationType;
import org.springframework.labs.eprescribing.model.Prescription;
import org.springframework.labs.eprescribing.repository.OwnerRepository;
import org.springframework.labs.eprescribing.util.EntityUtils;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import jakarta.transaction.Transactional;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple JDBC-based implementation of the {@link OwnerRepository} interface.
 *
 * @author Ken Krebs
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Sam Brannen
 * @author Thomas Risberg
 * @author Mark Fisher
 * @author Antoine Rey
 * @author Vitaliy Fedoriv
 */
@Repository
@Profile("jdbc")
public class JdbcOwnerRepositoryImpl implements OwnerRepository {

    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private SimpleJdbcInsert insertOwner;

    @Autowired
    public JdbcOwnerRepositoryImpl(DataSource dataSource) {

        this.insertOwner = new SimpleJdbcInsert(dataSource)
            .withTableName("owners")
            .usingGeneratedKeyColumns("id");

        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);

    }


    /**
     * Loads {@link Owner Owners} from the data store by last name, returning all owners whose last name <i>starts</i> with
     * the given name; also loads the {@link Medication Medications} and {@link Prescription Prescriptions} for the corresponding owners, if not
     * already loaded.
     */
    @Override
    public Collection<Owner> findByLastName(String lastName) throws DataAccessException {
        Map<String, Object> params = new HashMap<>();
        params.put("lastName", lastName + "%");
        List<Owner> owners = this.namedParameterJdbcTemplate.query(
            "SELECT id, first_name, last_name, address, city, telephone FROM owners WHERE last_name like :lastName",
            params,
            BeanPropertyRowMapper.newInstance(Owner.class)
        );
        loadOwnersMedicationsAndPrescriptions(owners);
        return owners;
    }

    /**
     * Loads the {@link Owner} with the supplied <code>id</code>; also loads the {@link Medication Medications} and {@link Prescription Prescriptions}
     * for the corresponding owner, if not already loaded.
     */
    @Override
    public Owner findById(int id) throws DataAccessException {
        Owner owner;
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("id", id);
            owner = this.namedParameterJdbcTemplate.queryForObject(
                "SELECT id, first_name, last_name, address, city, telephone FROM owners WHERE id= :id",
                params,
                BeanPropertyRowMapper.newInstance(Owner.class)
            );
        } catch (EmptyResultDataAccessException ex) {
            throw new ObjectRetrievalFailureException(Owner.class, id);
        }
        loadMedicationsAndPrescriptions(owner);
        return owner;
    }

    public void loadMedicationsAndPrescriptions(final Owner owner) {
        Map<String, Object> params = new HashMap<>();
        params.put("id", owner.getId());
        final List<JdbcMedication> medications = this.namedParameterJdbcTemplate.query(
            "SELECT medications.id as medications_id, name, expiration_date, type_id, owner_id, prescriptions.id as prescription_id, prescription_date, description, prescriptions.medication_id as prescriptions_medication_id FROM medications LEFT OUTER JOIN prescriptions ON medications.id = prescriptions.medication_id WHERE owner_id=:id ORDER BY medications.id",
            params,
            new JdbcMedicationPrescriptionExtractor()
        );
        Collection<MedicationType> medicationTypes = getMedicationTypes();
        for (JdbcMedication medication : medications) {
            medication.setType(EntityUtils.getById(medicationTypes, MedicationType.class, medication.getTypeId()));
            owner.addMedication(medication);
        }
    }

    @Override
    public void save(Owner owner) throws DataAccessException {
        BeanPropertySqlParameterSource parameterSource = new BeanPropertySqlParameterSource(owner);
        if (owner.isNew()) {
            Number newKey = this.insertOwner.executeAndReturnKey(parameterSource);
            owner.setId(newKey.intValue());
        } else {
            this.namedParameterJdbcTemplate.update(
                "UPDATE owners SET first_name=:firstName, last_name=:lastName, address=:address, " +
                    "city=:city, telephone=:telephone WHERE id=:id",
                parameterSource);
        }
    }

    public Collection<MedicationType> getMedicationTypes() throws DataAccessException {
        return this.namedParameterJdbcTemplate.query(
            "SELECT id, name FROM types ORDER BY name", new HashMap<String, Object>(),
            BeanPropertyRowMapper.newInstance(MedicationType.class));
    }

    /**
     * Loads the {@link Medication} and {@link Prescription} data for the supplied {@link List} of {@link Owner Owners}.
     *
     * @param owners the list of owners for whom the medication and prescription data should be loaded
     * @see #loadMedicationsAndPrescriptions(Owner)
     */
    private void loadOwnersMedicationsAndPrescriptions(List<Owner> owners) {
        for (Owner owner : owners) {
            loadMedicationsAndPrescriptions(owner);
        }
    }

	@Override
	public Collection<Owner> findAll() throws DataAccessException {
		List<Owner> owners = this.namedParameterJdbcTemplate.query(
	            "SELECT id, first_name, last_name, address, city, telephone FROM owners",
	            new HashMap<String, Object>(),
	            BeanPropertyRowMapper.newInstance(Owner.class));
		for (Owner owner : owners) {
            loadMedicationsAndPrescriptions(owner);
        }
	    return owners;
	}

	@Override
	@Transactional
	public void delete(Owner owner) throws DataAccessException {
		Map<String, Object> owner_params = new HashMap<>();
		owner_params.put("id", owner.getId());
        List<Medication> medications = owner.getMedications();
        // cascade delete medications
        for (Medication medication : medications){
        	Map<String, Object> medication_params = new HashMap<>();
        	medication_params.put("id", medication.getId());
        	// cascade delete prescriptions
        	List<Prescription> prescriptions = medication.getPrescriptions();
            for (Prescription prescription : prescriptions){
            	Map<String, Object> prescription_params = new HashMap<>();
            	prescription_params.put("id", prescription.getId());
            	this.namedParameterJdbcTemplate.update("DELETE FROM prescriptions WHERE id=:id", prescription_params);
            }
            this.namedParameterJdbcTemplate.update("DELETE FROM medications WHERE id=:id", medication_params);
        }
        this.namedParameterJdbcTemplate.update("DELETE FROM owners WHERE id=:id", owner_params);
	}


}
