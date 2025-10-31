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
package org.springframework.samples.petclinic.repository;

import java.util.List;

import org.springframework.samples.petclinic.model.BaseEntity;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.PetType;

/**
 * Data access interface for Pet entities and related reference data.
 * Manages pet persistence operations and provides access to pet type classifications.
 * 
 * <p>Core functionality:
 * - Pet CRUD operations with ID-based lookup
 * - Pet type reference data access for form dropdowns and classification
 * - Persistence operations supporting both insert and update use cases
 * 
 * <p>Performance considerations:
 * - Pet types typically small reference dataset suitable for caching
 * - Pet lookups should include eager/lazy loading strategy for visits and owner
 * - Consider indexing on owner_id foreign key for pet queries by owner
 * 
 * <p>Business rules:
 * - Pet entities maintain bidirectional relationships with Owner and Visit
 * - Pet type assignments should be validated against reference data
 * - Cascade operations handled at JPA entity level for data consistency
 * 
 * <p>Integration notes:
 * - Method signatures compatible with Spring Data JPA for automatic implementation
 * - Exception handling follows Spring DAO exception hierarchy
 * - Supports both programmatic and declarative transaction management
 * 
 * @author Ken Krebs
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @author Michael Isvy
 */
public interface PetRepository {

    /**
     * Retrieve all <code>PetType</code>s from the data store.
     *
     * @return a <code>Collection</code> of <code>PetType</code>s
     */
    List<PetType> findPetTypes();

    /**
     * Retrieve a <code>Pet</code> from the data store by id.
     *
     * @param id the id to search for
     * @return the <code>Pet</code> if found
     * @throws org.springframework.dao.DataRetrievalFailureException if not found
     */
    Pet findById(int id);

    /**
     * Save a <code>Pet</code> to the data store, either inserting or updating it.
     *
     * @param pet the <code>Pet</code> to save
     * @see BaseEntity#isNew
     */
    void save(Pet pet);

}
