/*
 * Copyright 2002-2022 the original author or authors.
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
package org.springframework.samples.petclinic.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.xml.bind.annotation.XmlElement;

import org.springframework.beans.support.MutableSortDefinition;
import org.springframework.beans.support.PropertyComparator;

/**
 * Domain object representing a veterinarian in the clinic system.
 * Extends Person with professional specialties and qualifications.
 * 
 * <p>Key responsibilities:
 * - Store veterinarian personal information (inherited from Person)
 * - Manage collection of medical specialties and certifications
 * - Support XML serialization for REST API endpoints
 * - Provide specialty lookup and counting capabilities
 * 
 * <p>Business invariants:
 * - Veterinarians can have zero or more specialties
 * - Specialties are managed through many-to-many relationship
 * - Specialty list is always sorted alphabetically for consistent display
 * - EAGER fetching ensures specialties available without additional queries
 * 
 * <p>Integration considerations:
 * - XML binding annotations support REST API serialization
 * - Join table 'vet_specialties' manages specialty associations
 * - Specialty data typically pre-loaded as reference data
 * 
 * @author Ken Krebs
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @author Arjen Poutsma
 */
@Entity
@Table(name = "vets")
public class Vet extends Person {

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "vet_specialties", joinColumns = @JoinColumn(name = "vet_id"),
        inverseJoinColumns = @JoinColumn(name = "specialty_id"))
    private Set<Specialty> specialties;

    protected Set<Specialty> getSpecialtiesInternal() {
        if (this.specialties == null) {
            this.specialties = new HashSet<>();
        }
        return this.specialties;
    }

    protected void setSpecialtiesInternal(Set<Specialty> specialties) {
        this.specialties = specialties;
    }

    /**
     * Returns an immutable list of this veterinarian's specialties, sorted alphabetically.
     * Used for display purposes and XML/JSON serialization in REST endpoints.
     * 
     * @return sorted, unmodifiable list of specialties (never null, may be empty)
     */
    @XmlElement
    public List<Specialty> getSpecialties() {
        List<Specialty> sortedSpecs = new ArrayList<>(getSpecialtiesInternal());
        PropertyComparator.sort(sortedSpecs, new MutableSortDefinition("name", true, true));
        return Collections.unmodifiableList(sortedSpecs);
    }

    /**
     * Returns the count of specialties for this veterinarian.
     * Useful for conditional rendering (e.g., "General Practitioner" vs specialty display).
     * 
     * @return number of specialties (0 or more)
     */
    public int getNrOfSpecialties() {
        return getSpecialtiesInternal().size();
    }

    /**
     * Adds a medical specialty to this veterinarian's qualifications.
     * 
     * @param specialty the specialty to add (must not be null)
     * TODO: Add validation to prevent duplicate specialties for same vet
     */
    public void addSpecialty(Specialty specialty) {
        getSpecialtiesInternal().add(specialty);
    }

}
