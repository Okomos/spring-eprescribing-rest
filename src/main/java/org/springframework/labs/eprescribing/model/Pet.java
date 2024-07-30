/*
 * Copyright 2002-2013 the original author or authors.
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
package org.springframework.labs.eprescribing.model;

import org.springframework.beans.support.MutableSortDefinition;
import org.springframework.beans.support.PropertyComparator;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.*;


/**
 * Simple business object representing a pet.
 *
 * @author Ken Krebs
 * @author Juergen Hoeller
 * @author Sam Brannen
 */
@Entity
@Table(name = "pets")
public class Pet extends NamedEntity {

    @Column(name = "birth_date", columnDefinition = "DATE")
    private LocalDate birthDate;

    @ManyToOne
    @JoinColumn(name = "type_id")
    private PetType type;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private Owner owner;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "pet", fetch = FetchType.EAGER)
    private Set<Prescription> prescriptions;

    public LocalDate getBirthDate() {
        return this.birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public PetType getType() {
        return this.type;
    }

    public void setType(PetType type) {
        this.type = type;
    }

    public Owner getOwner() {
        return this.owner;
    }

    public void setOwner(Owner owner) {
        this.owner = owner;
    }

    protected Set<Prescription> getPrescriptionsInternal() {
        if (this.prescriptions == null) {
            this.prescriptions = new HashSet<>();
        }
        return this.prescriptions;
    }

    protected void setPrescriptionsInternal(Set<Prescription> prescriptions) {
        this.prescriptions = prescriptions;
    }

    public List<Prescription> getPrescriptions() {
        List<Prescription> sortedPrescriptions = new ArrayList<>(getPrescriptionsInternal());
        PropertyComparator.sort(sortedPrescriptions, new MutableSortDefinition("date", false, false));
        return Collections.unmodifiableList(sortedPrescriptions);
    }

    public void setPrescriptions(List<Prescription> prescriptions) {
        this.prescriptions = new HashSet<>(prescriptions);
    }

    public void addPrescription(Prescription prescription) {
        getPrescriptionsInternal().add(prescription);
        prescription.setPet(this);
    }

}
