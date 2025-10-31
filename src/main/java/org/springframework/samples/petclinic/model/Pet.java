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

import org.springframework.beans.support.MutableSortDefinition;
import org.springframework.beans.support.PropertyComparator;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Domain object representing a pet in the veterinary clinic system.
 * Central entity linking owners to veterinary visits and pet type classifications.
 * 
 * <p>Key responsibilities:
 * - Store pet identification (name inherited from NamedEntity, birthDate, type)
 * - Maintain bidirectional relationship with Owner (many pets to one owner)
 * - Manage collection of veterinary visits with chronological ordering
 * - Support pet type classification for medical and administrative purposes
 * 
 * <p>Business invariants:
 * - Each pet must belong to exactly one owner
 * - Pet type is required for medical record categorization
 * - Birth date uses ISO format (yyyy/MM/dd) for consistent date handling
 * - Visit collection sorted by date (most recent first) for clinical workflow
 * 
 * <p>Performance considerations:
 * - EAGER fetch on visits may cause performance issues with pets having many visits
 * - Consider pagination for visit history in high-volume clinics
 * 
 * @author Ken Krebs
 * @author Juergen Hoeller
 * @author Sam Brannen
 */
@Entity
@Table(name = "pets")
public class Pet extends NamedEntity {

    @Column(name = "birth_date")
    @DateTimeFormat(pattern = "yyyy/MM/dd")
    private LocalDate birthDate;

    @ManyToOne
    @JoinColumn(name = "type_id")
    private PetType type;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private Owner owner;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "pet", fetch = FetchType.EAGER)
    private Set<Visit> visits; // TODO: EAGER fetch may cause N+1 queries - consider LAZY + explicit fetching


    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public LocalDate getBirthDate() {
        return this.birthDate;
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

    protected void setOwner(Owner owner) {
        this.owner = owner;
    }

    protected Set<Visit> getVisitsInternal() {
        if (this.visits == null) {
            this.visits = new HashSet<>();
        }
        return this.visits;
    }

    protected void setVisitsInternal(Set<Visit> visits) {
        this.visits = visits;
    }

    /**
     * Returns an immutable list of this pet's visits, sorted chronologically (most recent first).
     * Useful for displaying visit history in reverse chronological order for clinical review.
     * 
     * @return sorted, unmodifiable list of visits (never null, may be empty)
     * TODO: Consider caching sorted list or using database-level ordering for better performance
     */
    public List<Visit> getVisits() {
        List<Visit> sortedVisits = new ArrayList<>(getVisitsInternal());
        PropertyComparator.sort(sortedVisits, new MutableSortDefinition("date", false, false));
        return Collections.unmodifiableList(sortedVisits);
    }

    /**
     * Adds a veterinary visit to this pet's medical history, establishing bidirectional relationship.
     * 
     * @param visit the visit record to add (must not be null)
     * TODO: Add validation for visit date (should not be future dates in most cases)
     * TODO: Consider business rule validation (duplicate visits on same date, etc.)
     */
    public void addVisit(Visit visit) {
        getVisitsInternal().add(visit);
        visit.setPet(this);
    }

}
