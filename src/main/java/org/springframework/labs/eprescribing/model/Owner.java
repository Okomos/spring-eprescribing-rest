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
import org.springframework.core.style.ToStringCreator;

import jakarta.persistence.*;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotEmpty;
import java.util.*;


/**
 * Simple JavaBean domain object representing an owner.
 *
 * @author Ken Krebs
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @author Michael Isvy
 */
@Entity
@Table(name = "owners")
public class Owner extends Person {
    @Column(name = "address")
    @NotEmpty
    private String address;

    @Column(name = "city")
    @NotEmpty
    private String city;

    @Column(name = "telephone")
    @NotEmpty
    @Digits(fraction = 0, integer = 10)
    private String telephone;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "owner", fetch = FetchType.EAGER)
    private Set<Medication> medications;


    public String getAddress() {
        return this.address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return this.city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getTelephone() {
        return this.telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    protected Set<Medication> getMedicationsInternal() {
        if (this.medications == null) {
            this.medications = new HashSet<>();
        }
        return this.medications;
    }

    protected void setMedicationsInternal(Set<Medication> medications) {
        this.medications = medications;
    }

    public List<Medication> getMedications() {
        List<Medication> sortedMedications = new ArrayList<>(getMedicationsInternal());
        PropertyComparator.sort(sortedMedications, new MutableSortDefinition("name", true, true));
        return Collections.unmodifiableList(sortedMedications);
    }

    public void setMedications(List<Medication> medications) {
        this.medications = new HashSet<>(medications);
    }

    public void addMedication(Medication medication) {
        getMedicationsInternal().add(medication);
        medication.setOwner(this);
    }

    /**
     * Return the Medication with the given name, or null if none found for this Owner.
     *
     * @param name to test
     * @return true if medication name is already in use
     */
    public Medication getMedication(String name) {
        return getMedication(name, false);
    }

    /**
     * Return the Medication with the given name, or null if none found for this Owner.
     *
     * @param name to test
     * @return true if medication name is already in use
     */
    public Medication getMedication(String name, boolean ignoreNew) {
        name = name.toLowerCase();
        for (Medication medication : getMedicationsInternal()) {
            if (!ignoreNew || !medication.isNew()) {
                String compName = medication.getName();
                compName = compName.toLowerCase();
                if (compName.equals(name)) {
                    return medication;
                }
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return new ToStringCreator(this)

            .append("id", this.getId())
            .append("new", this.isNew())
            .append("lastName", this.getLastName())
            .append("firstName", this.getFirstName())
            .append("address", this.address)
            .append("city", this.city)
            .append("telephone", this.telephone)
            .toString();
    }
}
