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
import org.springframework.labs.eprescribing.model.MedicationType;
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
            .addValue("medication_id", prescription.getMedication().getId());
    }

    @Override
    public List<Prescription> findByMedicationId(Integer medicationId) {
        Map<String, Object> params = new HashMap<>();
        params.put("id", medicationId);
        JdbcMedication medication = this.namedParameterJdbcTemplate.queryForObject(
            "SELECT id as medications_id, name, expiration_date, type_id, owner_id FROM medications WHERE id=:id",
            params,
            new JdbcMedicationRowMapper());

        List<Prescription> prescriptions = this.namedParameterJdbcTemplate.query(
            "SELECT id as prescription_id, prescription_date, description FROM prescriptions WHERE medication_id=:id",
            params, new JdbcPrescriptionRowMapper());

        for (Prescription prescription : prescriptions) {
            prescription.setMedication(medication);
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
                "SELECT id as prescription_id, prescriptions.medication_id as medications_id, prescription_date, description FROM prescriptions WHERE id= :id",
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
            "SELECT id as prescription_id, medications.id as medications_id, prescription_date, description FROM prescriptions LEFT JOIN medications ON prescriptions.medication_id = medications.id",
            params, new JdbcPrescriptionRowMapperExt());
    }

    @Override
    public void save(Prescription prescription) throws DataAccessException {
        if (prescription.isNew()) {
            Number newKey = this.insertPrescription.executeAndReturnKey(createPrescriptionParameterSource(prescription));
            prescription.setId(newKey.intValue());
        } else {
            this.namedParameterJdbcTemplate.update(
                "UPDATE prescriptions SET prescription_date=:prescription_date, description=:description, medication_id=:medication_id WHERE id=:id ",
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
            JdbcMedication medication = new JdbcMedication();
            MedicationType medicationType = new MedicationType();
            Owner owner = new Owner();
            prescription.setId(rs.getInt("prescription_id"));
            Date prescriptionDate = rs.getDate("prescription_date");
            prescription.setDate(new java.sql.Date(prescriptionDate.getTime()).toLocalDate());
            prescription.setDescription(rs.getString("description"));
            Map<String, Object> params = new HashMap<>();
            params.put("id", rs.getInt("medications_id"));
            medication = JdbcPrescriptionRepositoryImpl.this.namedParameterJdbcTemplate.queryForObject(
                "SELECT medications.id as medications_id, name, expiration_date, type_id, owner_id FROM medications WHERE medications.id=:id",
                params,
                new JdbcMedicationRowMapper());
            params.put("type_id", medication.getTypeId());
            medicationType = JdbcPrescriptionRepositoryImpl.this.namedParameterJdbcTemplate.queryForObject(
                "SELECT id, name FROM types WHERE id= :type_id",
                params,
                BeanPropertyRowMapper.newInstance(MedicationType.class));
            medication.setType(medicationType);
            params.put("owner_id", medication.getOwnerId());
            owner = JdbcPrescriptionRepositoryImpl.this.namedParameterJdbcTemplate.queryForObject(
                "SELECT id, first_name, last_name, address, city, telephone FROM owners WHERE id= :owner_id",
                params,
                BeanPropertyRowMapper.newInstance(Owner.class));
            medication.setOwner(owner);
            prescription.setMedication(medication);
            return prescription;
        }
    }

}
