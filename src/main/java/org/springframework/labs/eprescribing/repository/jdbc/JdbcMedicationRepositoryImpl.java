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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.labs.eprescribing.model.Owner;
import org.springframework.labs.eprescribing.model.Medication;
import org.springframework.labs.eprescribing.model.MedicationType;
import org.springframework.labs.eprescribing.model.Prescription;
import org.springframework.labs.eprescribing.repository.OwnerRepository;
import org.springframework.labs.eprescribing.repository.MedicationRepository;
import org.springframework.labs.eprescribing.repository.PrescriptionRepository;
import org.springframework.labs.eprescribing.util.EntityUtils;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.stereotype.Repository;

/**
 * @author Ken Krebs
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Sam Brannen
 * @author Thomas Risberg
 * @author Mark Fisher
 * @author Vitaliy Fedoriv
 */
@Repository
@Profile("jdbc")
public class JdbcMedicationRepositoryImpl implements MedicationRepository {

    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private SimpleJdbcInsert insertMedication;

    private OwnerRepository ownerRepository;

    private PrescriptionRepository prescriptionRepository;


    @Autowired
    public JdbcMedicationRepositoryImpl(DataSource dataSource,
    		OwnerRepository ownerRepository,
    		PrescriptionRepository prescriptionRepository) {
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);

        this.insertMedication = new SimpleJdbcInsert(dataSource)
            .withTableName("medications")
            .usingGeneratedKeyColumns("id");

        this.ownerRepository = ownerRepository;
        this.prescriptionRepository = prescriptionRepository;
    }

    @Override
    public List<MedicationType> findMedicationTypes() throws DataAccessException {
        Map<String, Object> params = new HashMap<>();
        return this.namedParameterJdbcTemplate.query(
            "SELECT id, name FROM types ORDER BY name",
            params,
            BeanPropertyRowMapper.newInstance(MedicationType.class));
    }

    @Override
    public Medication findById(int id) throws DataAccessException {
        Integer ownerId;
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("id", id);
            ownerId = this.namedParameterJdbcTemplate.queryForObject("SELECT owner_id FROM medications WHERE id=:id", params, Integer.class);
        } catch (EmptyResultDataAccessException ex) {
            throw new ObjectRetrievalFailureException(Medication.class, id);
        }
        Owner owner = this.ownerRepository.findById(ownerId);
        return EntityUtils.getById(owner.getMedications(), Medication.class, id);
    }

    @Override
    public void save(Medication medication) throws DataAccessException {
        if (medication.isNew()) {
            Number newKey = this.insertMedication.executeAndReturnKey(
                createMedicationParameterSource(medication));
            medication.setId(newKey.intValue());
        } else {
            this.namedParameterJdbcTemplate.update(
                "UPDATE medications SET name=:name, expiration_date=:expiration_date, type_id=:type_id, " +
                    "owner_id=:owner_id WHERE id=:id",
                createMedicationParameterSource(medication));
        }
    }

    /**
     * Creates a {@link MapSqlParameterSource} based on data values from the supplied {@link Medication} instance.
     */
    private MapSqlParameterSource createMedicationParameterSource(Medication medication) {
        return new MapSqlParameterSource()
            .addValue("id", medication.getId())
            .addValue("name", medication.getName())
            .addValue("expiration_date", medication.getExpirationDate())
            .addValue("type_id", medication.getType().getId())
            .addValue("owner_id", medication.getOwner().getId());
    }
    
	@Override
	public Collection<Medication> findAll() throws DataAccessException {
		Map<String, Object> params = new HashMap<>();
		Collection<Medication> medications = new ArrayList<Medication>();
		Collection<JdbcMedication> jdbcMedications = new ArrayList<JdbcMedication>();
		jdbcMedications = this.namedParameterJdbcTemplate
				.query("SELECT medications.id as medications_id, name, expiration_date, type_id, owner_id FROM medications",
				params,
				new JdbcMedicationRowMapper());
		Collection<MedicationType> medicationTypes = this.namedParameterJdbcTemplate.query("SELECT id, name FROM types ORDER BY name",
				new HashMap<String,
				Object>(), BeanPropertyRowMapper.newInstance(MedicationType.class));
		Collection<Owner> owners = this.namedParameterJdbcTemplate.query(
				"SELECT id, first_name, last_name, address, city, telephone FROM owners ORDER BY last_name",
				new HashMap<String, Object>(),
				BeanPropertyRowMapper.newInstance(Owner.class));
		for (JdbcMedication jdbcMedication : jdbcMedications) {
			jdbcMedication.setType(EntityUtils.getById(medicationTypes, MedicationType.class, jdbcMedication.getTypeId()));
			jdbcMedication.setOwner(EntityUtils.getById(owners, Owner.class, jdbcMedication.getOwnerId()));
			// TODO add prescriptions
			medications.add(jdbcMedication);
		}
		return medications;
	}

	@Override
	public void delete(Medication medication) throws DataAccessException {
		Map<String, Object> medication_params = new HashMap<>();
		medication_params.put("id", medication.getId());
		List<Prescription> prescriptions = medication.getPrescriptions();
		// cascade delete prescriptions
		for (Prescription prescription : prescriptions) {
			Map<String, Object> prescription_params = new HashMap<>();
			prescription_params.put("id", prescription.getId());
			this.namedParameterJdbcTemplate.update("DELETE FROM prescriptions WHERE id=:id", prescription_params);
		}
		this.namedParameterJdbcTemplate.update("DELETE FROM medications WHERE id=:id", medication_params);
	}

}
