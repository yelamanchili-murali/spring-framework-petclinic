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
package org.springframework.samples.petclinic.service;

import java.util.Collection;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.PetType;
import org.springframework.samples.petclinic.model.Vet;
import org.springframework.samples.petclinic.model.Visit;


/**
 * Primary business service interface for the petclinic application.
 * Provides a facade over the data access layer with transactional semantics and business logic.
 * 
 * <p>Service responsibilities:
 * - Coordinate CRUD operations for all domain entities (Owner, Pet, Visit, Vet)
 * - Enforce business rules and data validation
 * - Provide transactional boundaries for multi-entity operations
 * - Abstract data access complexity from web controllers
 * 
 * <p>Transaction boundaries:
 * - All methods should be executed within transaction context
 * - Read operations typically use read-only transactions for performance
 * - Write operations ensure data consistency through proper transaction management
 * 
 * <p>Business logic patterns:
 * - Search operations support partial matching and flexible query semantics
 * - Entity persistence handles both insert and update scenarios transparently
 * - Reference data (PetTypes, Vets) cached appropriately for performance
 * 
 * <p>Performance considerations:
 * - Search operations may benefit from indexing strategies
 * - Consider pagination for large datasets
 * - Lazy loading relationships to avoid N+1 query problems
 * 
 * @author Michael Isvy
 */
public interface ClinicService {

    /**
     * Retrieves all available pet types for classification purposes.
     * Typically used to populate dropdown selections in pet registration forms.
     * 
     * @return collection of all pet types (never null, may be empty)
     */
    Collection<PetType> findPetTypes();

    /**
     * Finds an owner by their unique identifier.
     * 
     * @param id the owner's unique identifier
     * @return owner entity with the specified ID
     * @throws EntityNotFoundException if no owner exists with the given ID
     */
    Owner findOwnerById(int id);

    /**
     * Finds a pet by their unique identifier.
     * 
     * @param id the pet's unique identifier  
     * @return pet entity with the specified ID
     * @throws EntityNotFoundException if no pet exists with the given ID
     */
    Pet findPetById(int id);

    /**
     * Persists a pet entity (insert or update based on ID presence).
     * Manages bidirectional relationships with owner automatically.
     * 
     * @param pet the pet to save (must not be null)
     * TODO: Add validation for required fields and business rules
     */
    void savePet(Pet pet);

    /**
     * Persists a visit record to the pet's medical history.
     * Ensures proper association with the related pet entity.
     * 
     * @param visit the visit record to save (must not be null)
     * TODO: Add validation for visit date and description requirements
     */
    void saveVisit(Visit visit);

    /**
     * Retrieves all veterinarians with their specialties.
     * Used for displaying vet listings and assignment purposes.
     * 
     * @return collection of all veterinarians (never null, may be empty)
     */
    Collection<Vet> findVets();

    /**
     * Persists an owner entity (insert or update based on ID presence).
     * Handles cascading operations for associated pets when appropriate.
     * 
     * @param owner the owner to save (must not be null)
     * TODO: Consider duplicate detection (same name/address combinations)
     */
    void saveOwner(Owner owner);

    /**
     * Searches for owners by last name using partial matching.
     * Empty string parameter returns all owners for administrative purposes.
     * 
     * @param lastName search criteria (empty string matches all owners)
     * @return collection of matching owners (never null, may be empty)
     * TODO: Consider pagination for large result sets
     */
    Collection<Owner> findOwnerByLastName(String lastName);

    /**
     * Retrieves all visits for a specific pet, typically for medical history display.
     * 
     * @param petId the pet's unique identifier
     * @return collection of visits for the specified pet (never null, may be empty)
     */
	Collection<Visit> findVisitsByPetId(int petId);

}
