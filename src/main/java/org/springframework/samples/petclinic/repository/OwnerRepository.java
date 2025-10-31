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

import java.util.Collection;

import org.springframework.samples.petclinic.model.BaseEntity;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Data access interface for Owner entities with Spring Data JPA compatible method signatures.
 * Provides CRUD operations and specialized query methods for owner management.
 * 
 * <p>Query capabilities:
 * - Owner lookup by ID with exception handling for missing entities
 * - Last name search with partial matching (prefix-based search)
 * - Persistence operations supporting both insert and update scenarios
 * 
 * <p>Performance considerations:
 * - findByLastName() may benefit from database index on lastName column
 * - Consider pagination support for large owner datasets
 * - Lazy loading of associated pets to avoid N+1 query problems
 * 
 * <p>Integration patterns:
 * - Method names follow Spring Data JPA naming conventions for automatic implementation
 * - Can be extended with @Query annotations for complex queries
 * - Exception handling delegates to Spring's DataAccessException hierarchy
 * 
 * <p>Data integrity:
 * - save() method handles both new entity creation and existing entity updates
 * - Cascading behavior for pet relationships managed at JPA entity level
 * - Optimistic locking support through JPA version fields if configured
 * 
 * @author Ken Krebs
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @author Michael Isvy
 */
public interface OwnerRepository {

    /**
     * Retrieve <code>Owner</code>s from the data store by last name, returning all owners whose last name <i>starts</i>
     * with the given name.
     *
     * @param lastName Value to search for
     * @return a <code>Collection</code> of matching <code>Owner</code>s (or an empty <code>Collection</code> if none
     * found)
     */
    Collection<Owner> findByLastName(String lastName);

    /**
     * Retrieve an <code>Owner</code> from the data store by id.
     *
     * @param id the id to search for
     * @return the <code>Owner</code> if found
     * @throws org.springframework.dao.DataRetrievalFailureException if not found
     */
    Owner findById(int id);


    /**
     * Save an <code>Owner</code> to the data store, either inserting or updating it.
     *
     * @param owner the <code>Owner</code> to save
     * @see BaseEntity#isNew
     */
    void save(Owner owner);


}
