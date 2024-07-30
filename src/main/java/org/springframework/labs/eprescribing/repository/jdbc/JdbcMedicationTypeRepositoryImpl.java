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
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.labs.eprescribing.model.Medication;
import org.springframework.labs.eprescribing.model.MedicationType;
import org.springframework.labs.eprescribing.model.Prescription;
import org.springframework.labs.eprescribing.repository.MedicationTypeRepository;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.stereotype.Repository;

/**
 * @author Vitaliy Fedoriv
 *
 */

@Repository
@Profile("jdbc")
public class JdbcMedicationTypeRepositoryImpl implements MedicationTypeRepository {

	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	private SimpleJdbcInsert insertMedicationType;

	@Autowired
	public JdbcMedicationTypeRepositoryImpl(DataSource dataSource) {
		this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		this.insertMedicationType = new SimpleJdbcInsert(dataSource)
	            .withTableName("types")
	            .usingGeneratedKeyColumns("id");
	}

	@Override
	public MedicationType findById(int id) {
		MedicationType medicationType;
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("id", id);
            medicationType = this.namedParameterJdbcTemplate.queryForObject(
                "SELECT id, name FROM types WHERE id= :id",
                params,
                BeanPropertyRowMapper.newInstance(MedicationType.class));
        } catch (EmptyResultDataAccessException ex) {
            throw new ObjectRetrievalFailureException(MedicationType.class, id);
        }
        return medicationType;
	}

    @Override
    public MedicationType findByName(String name) throws DataAccessException {
        MedicationType medicationType;
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("name", name);
            medicationType = this.namedParameterJdbcTemplate.queryForObject(
                "SELECT id, name FROM types WHERE name= :name",
                params,
                BeanPropertyRowMapper.newInstance(MedicationType.class));
        } catch (EmptyResultDataAccessException ex) {
            throw new ObjectRetrievalFailureException(MedicationType.class, name);
        }
        return medicationType;
    }

    @Override
	public Collection<MedicationType> findAll() throws DataAccessException {
		Map<String, Object> params = new HashMap<>();
        return this.namedParameterJdbcTemplate.query(
            "SELECT id, name FROM types",
            params,
            BeanPropertyRowMapper.newInstance(MedicationType.class));
	}

	@Override
	public void save(MedicationType medicationType) throws DataAccessException {
		BeanPropertySqlParameterSource parameterSource = new BeanPropertySqlParameterSource(medicationType);
		if (medicationType.isNew()) {
            Number newKey = this.insertMedicationType.executeAndReturnKey(parameterSource);
            medicationType.setId(newKey.intValue());
        } else {
            this.namedParameterJdbcTemplate.update("UPDATE types SET name=:name WHERE id=:id",
                parameterSource);
        }
	}

	@Override
	public void delete(MedicationType medicationType) throws DataAccessException {
		Map<String, Object> medicationtype_params = new HashMap<>();
		medicationtype_params.put("id", medicationType.getId());
		List<Medication> medications = new ArrayList<Medication>();
		medications = this.namedParameterJdbcTemplate.
    			query("SELECT medications.id, name, expiration_date, type_id, owner_id FROM medications WHERE type_id=:id",
    			medicationtype_params,
    			BeanPropertyRowMapper.newInstance(Medication.class));
		// cascade delete medications
		for (Medication medication : medications){
			Map<String, Object> medication_params = new HashMap<>();
			medication_params.put("id", medication.getId());
			List<Prescription> prescriptions = new ArrayList<Prescription>();
			prescriptions = this.namedParameterJdbcTemplate.query(
		            "SELECT id, medication_id, prescription_date, description FROM prescriptions WHERE medication_id = :id",
		            medication_params,
		            BeanPropertyRowMapper.newInstance(Prescription.class));
	        // cascade delete prescriptions
	        for (Prescription prescription : prescriptions){
	        	Map<String, Object> prescription_params = new HashMap<>();
	        	prescription_params.put("id", prescription.getId());
	        	this.namedParameterJdbcTemplate.update("DELETE FROM prescriptions WHERE id=:id", prescription_params);
	        }
	        this.namedParameterJdbcTemplate.update("DELETE FROM medications WHERE id=:id", medication_params);
        }
        this.namedParameterJdbcTemplate.update("DELETE FROM types WHERE id=:id", medicationtype_params);
	}

}
