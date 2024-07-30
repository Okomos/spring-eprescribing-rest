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

import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessException;
import org.springframework.labs.eprescribing.model.Prescriber;
import org.springframework.labs.eprescribing.repository.PrescriberRepository;
import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Collection;

/**
 * JPA implementation of the {@link PrescriberRepository} interface.
 *
 * @author Mike Keith
 * @author Rod Johnson
 * @author Sam Brannen
 * @author Michael Isvy
 * @author Vitaliy Fedoriv
 */
@Repository
@Profile("jpa")
public class JpaPrescriberRepositoryImpl implements PrescriberRepository {

    @PersistenceContext
    private EntityManager em;


	@Override
	public Prescriber findById(int id) throws DataAccessException {
		return this.em.find(Prescriber.class, id);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<Prescriber> findAll() throws DataAccessException {
		return this.em.createQuery("SELECT prescriber FROM Prescriber prescriber").getResultList();
	}

	@Override
	public void save(Prescriber prescriber) throws DataAccessException {
        if (prescriber.getId() == null) {
            this.em.persist(prescriber);
        } else {
            this.em.merge(prescriber);
        }
	}

	@Override
	public void delete(Prescriber prescriber) throws DataAccessException {
		this.em.remove(this.em.contains(prescriber) ? prescriber : this.em.merge(prescriber));
	}


}
