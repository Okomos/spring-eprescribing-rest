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
 * Simple business object representing a medication.
 *
 * @author Ken Krebs
 * @author Juergen Hoeller
 * @author Sam Brannen
 */
@Entity
@Table(name = "medications")
public class Medication extends NamedEntity {

    @Column(name = "expiration_date", columnDefinition = "DATE")
    private LocalDate expirationDate;

    @ManyToOne
    @JoinColumn(name = "type_id")
    private MedicationType type;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private Owner owner;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "medication", fetch = FetchType.EAGER)
    private Set<Prescription> prescriptions;

    public LocalDate getExpirationDate() {
        return this.expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    public MedicationType getType() {
        return this.type;
    }

    public void setType(MedicationType type) {
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
        prescription.setMedication(this);
    }

}
