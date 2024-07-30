/*
 * Copyright 2002-2018 the original author or authors.
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

import java.sql.ResultSet;
import java.sql.SQLException;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.labs.eprescribing.model.Specialty;
import org.springframework.labs.eprescribing.model.Prescriber;
import org.springframework.labs.eprescribing.repository.PrescriberRepository;
import org.springframework.labs.eprescribing.util.EntityUtils;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.stereotype.Repository;

/**
 * A simple JDBC-based implementation of the {@link PrescriberRepository} interface.
 *
 * @author Ken Krebs
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Sam Brannen
 * @author Thomas Risberg
 * @author Mark Fisher
 * @author Michael Isvy
 * @author Vitaliy Fedoriv
 */
@Repository
@Profile("jdbc")
public class JdbcPrescriberRepositoryImpl implements PrescriberRepository {

    private JdbcTemplate jdbcTemplate;
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
	private SimpleJdbcInsert insertPrescriber;

    @Autowired
    public JdbcPrescriberRepositoryImpl(DataSource dataSource, JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
		this.insertPrescriber = new SimpleJdbcInsert(dataSource).withTableName("prescribers").usingGeneratedKeyColumns("id");
		this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    /**
     * Refresh the cache of Prescribers that the ClinicService is holding.
     */
    @Override
    public Collection<Prescriber> findAll() throws DataAccessException {
        List<Prescriber> prescribers = new ArrayList<>();
        // Retrieve the list of all prescribers.
        prescribers.addAll(this.jdbcTemplate.query(
            "SELECT id, first_name, last_name FROM prescribers ORDER BY last_name,first_name",
            BeanPropertyRowMapper.newInstance(Prescriber.class)));

        // Retrieve the list of all possible specialties.
        final List<Specialty> specialties = this.jdbcTemplate.query(
            "SELECT id, name FROM specialties",
            BeanPropertyRowMapper.newInstance(Specialty.class));

        // Build each prescriber's list of specialties.
        for (Prescriber prescriber : prescribers) {
            final List<Integer> prescriberSpecialtiesIds = this.jdbcTemplate.query(
                "SELECT specialty_id FROM prescriber_specialties WHERE prescriber_id=?",
                new BeanPropertyRowMapper<Integer>() {
                    @Override
                    public Integer mapRow(ResultSet rs, int row) throws SQLException {
                        return rs.getInt(1);
                    }
                },
                prescriber.getId());
            for (int specialtyId : prescriberSpecialtiesIds) {
                Specialty specialty = EntityUtils.getById(specialties, Specialty.class, specialtyId);
                prescriber.addSpecialty(specialty);
            }
        }
        return prescribers;
    }
    
	@Override
	public Prescriber findById(int id) throws DataAccessException {
		Prescriber prescriber;
		try {
			Map<String, Object> prescriber_params = new HashMap<>();
			prescriber_params.put("id", id);
			prescriber = this.namedParameterJdbcTemplate.queryForObject(
					"SELECT id, first_name, last_name FROM prescribers WHERE id= :id",
					prescriber_params,
					BeanPropertyRowMapper.newInstance(Prescriber.class));

			final List<Specialty> specialties = this.namedParameterJdbcTemplate.query(
					"SELECT id, name FROM specialties", prescriber_params, BeanPropertyRowMapper.newInstance(Specialty.class));

			final List<Integer> prescriberSpecialtiesIds = this.namedParameterJdbcTemplate.query(
					"SELECT specialty_id FROM prescriber_specialties WHERE prescriber_id=:id",
					prescriber_params,
					new BeanPropertyRowMapper<Integer>() {
						@Override
						public Integer mapRow(ResultSet rs, int row) throws SQLException {
							return rs.getInt(1);
						}
					});
			for (int specialtyId : prescriberSpecialtiesIds) {
				Specialty specialty = EntityUtils.getById(specialties, Specialty.class, specialtyId);
				prescriber.addSpecialty(specialty);
			}

		} catch (EmptyResultDataAccessException ex) {
			throw new ObjectRetrievalFailureException(Prescriber.class, id);
		}
		return prescriber;
	}

	@Override
	public void save(Prescriber prescriber) throws DataAccessException {
		BeanPropertySqlParameterSource parameterSource = new BeanPropertySqlParameterSource(prescriber);
		if (prescriber.isNew()) {
			Number newKey = this.insertPrescriber.executeAndReturnKey(parameterSource);
			prescriber.setId(newKey.intValue());
			updatePrescriberSpecialties(prescriber);
		} else {
			this.namedParameterJdbcTemplate
					.update("UPDATE prescribers SET first_name=:firstName, last_name=:lastName WHERE id=:id", parameterSource);
			updatePrescriberSpecialties(prescriber);
		}
	}

	@Override
	public void delete(Prescriber prescriber) throws DataAccessException {
		Map<String, Object> params = new HashMap<>();
		params.put("id", prescriber.getId());
		this.namedParameterJdbcTemplate.update("DELETE FROM prescriber_specialties WHERE prescriber_id=:id", params);
		this.namedParameterJdbcTemplate.update("DELETE FROM prescribers WHERE id=:id", params);
	}
	
	private void updatePrescriberSpecialties(Prescriber prescriber) throws DataAccessException {
		Map<String, Object> params = new HashMap<>();
		params.put("id", prescriber.getId());
		this.namedParameterJdbcTemplate.update("DELETE FROM prescriber_specialties WHERE prescriber_id=:id", params);
		for (Specialty spec : prescriber.getSpecialties()) {
			params.put("spec_id", spec.getId());
			if(!(spec.getId() == null)) {
				this.namedParameterJdbcTemplate.update("INSERT INTO prescriber_specialties VALUES (:id, :spec_id)", params);
			}
		}
	}

}
