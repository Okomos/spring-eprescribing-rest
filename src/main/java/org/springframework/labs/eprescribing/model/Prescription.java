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

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import java.time.LocalDate;

/**
 * Simple JavaBean domain object representing a prescription.
 *
 * @author Ken Krebs
 */
@Entity
@Table(name = "prescriptions")
public class Prescription extends BaseEntity {

    /**
     * Holds value of property date.
     */
    @Column(name = "prescription_date", columnDefinition = "DATE")
    private LocalDate date;

    /**
     * Holds value of property description.
     */
    @NotEmpty
    @Column(name = "description")
    private String description;

    /**
     * Holds value of property medication.
     */
    @ManyToOne
    @JoinColumn(name = "medication_id")
    private Medication medication;


    /**
     * Creates a new instance of Prescription for the current date
     */
    public Prescription() {
        this.date = LocalDate.now();
    }


    /**
     * Getter for property date.
     *
     * @return Value of property date.
     */
    public LocalDate getDate() {
        return this.date;
    }

    /**
     * Setter for property date.
     *
     * @param date New value of property date.
     */
    public void setDate(LocalDate date) {
        this.date = date;
    }

    /**
     * Getter for property description.
     *
     * @return Value of property description.
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Setter for property description.
     *
     * @param description New value of property description.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Getter for property medication.
     *
     * @return Value of property medication.
     */
    public Medication getMedication() {
        return this.medication;
    }

    /**
     * Setter for property medication.
     *
     * @param medication New value of property medication.
     */
    public void setMedication(Medication medication) {
        this.medication = medication;
    }

}