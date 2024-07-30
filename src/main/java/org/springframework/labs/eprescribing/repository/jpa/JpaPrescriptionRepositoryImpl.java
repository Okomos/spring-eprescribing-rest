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
package org.springframework.labs.eprescribing.repository.jpa;

import java.util.Collection;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessException;
import org.springframework.labs.eprescribing.model.Prescription;
import org.springframework.labs.eprescribing.repository.PrescriptionRepository;
import org.springframework.stereotype.Repository;

/**
 * JPA implementation of the ClinicService interface using EntityManager.
 * <p/>
 * <p>The mappings are defined in "orm.xml" located in the META-INF directory.
 *
 * @author Mike Keith
 * @author Rod Johnson
 * @author Sam Brannen
 * @author Michael Isvy
 * @author Vitaliy Fedoriv
 */
@Repository
@Profile("jpa")
public class JpaPrescriptionRepositoryImpl implements PrescriptionRepository {

    @PersistenceContext
    private EntityManager em;


    @Override
    public void save(Prescription prescription) {
        if (prescription.getId() == null) {
            this.em.persist(prescription);
        } else {
            this.em.merge(prescription);
        }
    }


    @Override
    @SuppressWarnings("unchecked")
    public List<Prescription> findByPetId(Integer petId) {
        Query query = this.em.createQuery("SELECT v FROM Prescription v where v.pet.id= :id");
        query.setParameter("id", petId);
        return query.getResultList();
    }

	@Override
	public Prescription findById(int id) throws DataAccessException {
		return this.em.find(Prescription.class, id);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<Prescription> findAll() throws DataAccessException {
        return this.em.createQuery("SELECT v FROM Prescription v").getResultList();
	}

	@Override
	public void delete(Prescription prescription) throws DataAccessException {
        this.em.remove(this.em.contains(prescription) ? prescription : this.em.merge(prescription));
	}

}
