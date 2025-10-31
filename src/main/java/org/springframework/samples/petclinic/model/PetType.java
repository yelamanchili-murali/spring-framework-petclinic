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

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Reference data entity representing different types of pets (Cat, Dog, Hamster, etc.).
 * Provides classification system for pets to support medical protocols and administrative processes.
 * 
 * <p>Key characteristics:
 * - Simple lookup/reference entity with name-based identification
 * - Typically pre-populated with standard pet types during system setup
 * - Used in dropdown selections and pet registration forms
 * - Referenced by Pet entities for categorization
 * 
 * <p>Business usage:
 * - Enables species-specific medical protocols and treatment plans
 * - Supports reporting and analytics by pet type
 * - Facilitates veterinary specialization matching
 * - Used in insurance and billing categorization
 * 
 * <p>Data management:
 * - Reference data should be managed by administrators
 * - Changes require careful consideration due to existing pet associations
 * - Consider soft-delete pattern if types need to be retired
 * 
 * @author Juergen Hoeller
 */
@Entity
@Table(name = "types")
public class PetType extends NamedEntity {

}
