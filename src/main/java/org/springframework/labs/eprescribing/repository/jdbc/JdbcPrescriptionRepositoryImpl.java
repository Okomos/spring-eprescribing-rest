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
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.labs.eprescribing.model.Owner;
import org.springframework.labs.eprescribing.model.PetType;
import org.springframework.labs.eprescribing.model.Prescription;
import org.springframework.labs.eprescribing.repository.PrescriptionRepository;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * A simple JDBC-based implementation of the {@link PrescriptionRepository} interface.
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
public class JdbcPrescriptionRepositoryImpl implements PrescriptionRepository {

    protected SimpleJdbcInsert insertPrescription;
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    public JdbcPrescriptionRepositoryImpl(DataSource dataSource) {
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);

        this.insertPrescription = new SimpleJdbcInsert(dataSource)
            .withTableName("prescriptions")
            .usingGeneratedKeyColumns("id");
    }


    /**
     * Creates a {@link MapSqlParameterSource} based on data values from the supplied {@link Prescription} instance.
     */
    protected MapSqlParameterSource createPrescriptionParameterSource(Prescription prescription) {
        return new MapSqlParameterSource()
            .addValue("id", prescription.getId())
            .addValue("prescription_date", prescription.getDate())
            .addValue("description", prescription.getDescription())
            .addValue("pet_id", prescription.getPet().getId());
    }

    @Override
    public List<Prescription> findByPetId(Integer petId) {
        Map<String, Object> params = new HashMap<>();
        params.put("id", petId);
        JdbcPet pet = this.namedParameterJdbcTemplate.queryForObject(
            "SELECT id as pets_id, name, birth_date, type_id, owner_id FROM pets WHERE id=:id",
            params,
            new JdbcPetRowMapper());

        List<Prescription> prescriptions = this.namedParameterJdbcTemplate.query(
            "SELECT id as prescription_id, prescription_date, description FROM prescriptions WHERE pet_id=:id",
            params, new JdbcPrescriptionRowMapper());

        for (Prescription prescription : prescriptions) {
            prescription.setPet(pet);
        }

        return prescriptions;
    }

    @Override
    public Prescription findById(int id) throws DataAccessException {
        Prescription prescription;
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("id", id);
            prescription = this.namedParameterJdbcTemplate.queryForObject(
                "SELECT id as prescription_id, prescriptions.pet_id as pets_id, prescription_date, description FROM prescriptions WHERE id= :id",
                params,
                new JdbcPrescriptionRowMapperExt());
        } catch (EmptyResultDataAccessException ex) {
            throw new ObjectRetrievalFailureException(Prescription.class, id);
        }
        return prescription;
    }

    @Override
    public Collection<Prescription> findAll() throws DataAccessException {
        Map<String, Object> params = new HashMap<>();
        return this.namedParameterJdbcTemplate.query(
            "SELECT id as prescription_id, pets.id as pets_id, prescription_date, description FROM prescriptions LEFT JOIN pets ON prescriptions.pet_id = pets.id",
            params, new JdbcPrescriptionRowMapperExt());
    }

    @Override
    public void save(Prescription prescription) throws DataAccessException {
        if (prescription.isNew()) {
            Number newKey = this.insertPrescription.executeAndReturnKey(createPrescriptionParameterSource(prescription));
            prescription.setId(newKey.intValue());
        } else {
            this.namedParameterJdbcTemplate.update(
                "UPDATE prescriptions SET prescription_date=:prescription_date, description=:description, pet_id=:pet_id WHERE id=:id ",
                createPrescriptionParameterSource(prescription));
        }
    }

    @Override
    public void delete(Prescription prescription) throws DataAccessException {
        Map<String, Object> params = new HashMap<>();
        params.put("id", prescription.getId());
        this.namedParameterJdbcTemplate.update("DELETE FROM prescriptions WHERE id=:id", params);
    }

    protected class JdbcPrescriptionRowMapperExt implements RowMapper<Prescription> {

        @Override
        public Prescription mapRow(ResultSet rs, int rowNum) throws SQLException {
            Prescription prescription = new Prescription();
            JdbcPet pet = new JdbcPet();
            PetType petType = new PetType();
            Owner owner = new Owner();
            prescription.setId(rs.getInt("prescription_id"));
            Date prescriptionDate = rs.getDate("prescription_date");
            prescription.setDate(new java.sql.Date(prescriptionDate.getTime()).toLocalDate());
            prescription.setDescription(rs.getString("description"));
            Map<String, Object> params = new HashMap<>();
            params.put("id", rs.getInt("pets_id"));
            pet = JdbcPrescriptionRepositoryImpl.this.namedParameterJdbcTemplate.queryForObject(
                "SELECT pets.id as pets_id, name, birth_date, type_id, owner_id FROM pets WHERE pets.id=:id",
                params,
                new JdbcPetRowMapper());
            params.put("type_id", pet.getTypeId());
            petType = JdbcPrescriptionRepositoryImpl.this.namedParameterJdbcTemplate.queryForObject(
                "SELECT id, name FROM types WHERE id= :type_id",
                params,
                BeanPropertyRowMapper.newInstance(PetType.class));
            pet.setType(petType);
            params.put("owner_id", pet.getOwnerId());
            owner = JdbcPrescriptionRepositoryImpl.this.namedParameterJdbcTemplate.queryForObject(
                "SELECT id, first_name, last_name, address, city, telephone FROM owners WHERE id= :owner_id",
                params,
                BeanPropertyRowMapper.newInstance(Owner.class));
            pet.setOwner(owner);
            prescription.setPet(pet);
            return prescription;
        }
    }

}
