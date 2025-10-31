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

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

/**
 * Abstract base class providing primary key identity for all persistent domain objects.
 * Implements common identity patterns used throughout the petclinic domain model.
 * 
 * <p>Key responsibilities:
 * - Provide auto-generated integer primary keys using database identity columns
 * - Implement new entity detection for persistence lifecycle management
 * - Serve as foundation for JPA entity hierarchy
 * 
 * <p>Design patterns:
 * - Uses @MappedSuperclass to share identity behavior without separate table
 * - IDENTITY generation strategy delegates key assignment to database
 * - Boxed Integer allows null values to distinguish new vs persistent entities
 * 
 * <p>Usage guidelines:
 * - All domain entities should extend this class for consistent identity handling
 * - isNew() method critical for JPA merge/persist operation decisions
 * - ID should never be manually set except in special data import scenarios
 * 
 * @author Ken Krebs
 * @author Juergen Hoeller
 */
@MappedSuperclass
public class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Integer id;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Determines if this entity is new (not yet persisted to database).
     * Critical for JPA persistence operations and form processing logic.
     * 
     * @return true if entity has no assigned ID (transient state), false if persistent
     */
    public boolean isNew() {
        return this.id == null;
    }

}
