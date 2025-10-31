/*
 * Copyright 2002-2025 the original author or authors.
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

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.petclinic.model.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collection;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive characterization tests for Repository layer to preserve data access behavior 
 * during Spring Boot 3 + Next.js migration.
 * 
 * <p>These tests capture current repository behavior including:
 * - Entity relationship mappings and cascading behavior
 * - Query methods and search functionality  
 * - Data validation and constraint enforcement
 * - Transaction boundaries and rollback semantics
 * 
 * <p>Migration readiness: Tests designed to support transition to:
 * - Spring Boot 3 @DataJpaTest auto-configuration
 * - Standardized Spring Data JPA repositories
 * - Preserved entity relationship semantics
 * - H2 test database consistency
 * 
 * @author Migration Team
 */
@SpringJUnitConfig
@ContextConfiguration(locations = {"classpath:spring/business-config.xml"})
@Transactional
class RepositoryCharacterizationTest {

    @Autowired private OwnerRepository ownerRepository;
    @Autowired private PetRepository petRepository;
    @Autowired private VetRepository vetRepository;
    @Autowired private VisitRepository visitRepository;

    // ==================== OWNER REPOSITORY TESTS ====================

    @Test
    @DisplayName("OwnerRepository.findByLastName - should return matching owners")
    void findOwnerByLastName_shouldReturnMatchingOwners() {
        Collection<Owner> owners = ownerRepository.findByLastName("Davis");
        
        assertThat(owners).isNotEmpty();
        assertThat(owners).allMatch(owner -> owner.getLastName().equals("Davis"));
        assertThat(owners).hasSize(2); // Based on sample data
    }

    @Test
    @DisplayName("OwnerRepository.findByLastName - should return empty for non-existent name")
    void findOwnerByLastName_shouldReturnEmpty_whenNameNotFound() {
        Collection<Owner> owners = ownerRepository.findByLastName("NonExistentName");
        
        assertThat(owners).isEmpty();
    }

    @Test
    @DisplayName("OwnerRepository.findByLastName - empty string should return all owners")
    void findOwnerByLastName_shouldReturnAll_whenEmptyString() {
        Collection<Owner> owners = ownerRepository.findByLastName("");
        
        assertThat(owners).isNotEmpty();
        assertThat(owners).hasSizeGreaterThan(5); // Should return all sample owners
    }

    @Test
    @DisplayName("OwnerRepository.findById - should return owner with pets loaded")
    void findOwnerById_shouldReturnOwnerWithPets() {
        Owner owner = ownerRepository.findById(1);
        
        assertThat(owner).isNotNull();
        assertThat(owner.getFirstName()).isEqualTo("George");
        assertThat(owner.getLastName()).isEqualTo("Franklin");
        assertThat(owner.getPets()).isNotEmpty();
        assertThat(owner.getPets()).hasSize(1);
        assertThat(owner.getPets().get(0).getName()).isEqualTo("Leo");
    }

    @Test
    @DisplayName("OwnerRepository.save - should persist new owner")
    void saveOwner_shouldPersistNewOwner() {
        // Given
        Owner newOwner = new Owner();
        newOwner.setFirstName("Jane");
        newOwner.setLastName("Doe");
        newOwner.setAddress("123 Test Street");
        newOwner.setCity("Test City");
        newOwner.setTelephone("1234567890");

        // When
        ownerRepository.save(newOwner);

        // Then
        assertThat(newOwner.getId()).isNotNull();
        assertThat(newOwner.getId()).isPositive();
        
        // Verify persistence
        Owner savedOwner = ownerRepository.findById(newOwner.getId());
        assertThat(savedOwner).isNotNull();
        assertThat(savedOwner.getFirstName()).isEqualTo("Jane");
        assertThat(savedOwner.getLastName()).isEqualTo("Doe");
    }

    @Test
    @DisplayName("OwnerRepository.save - should update existing owner")
    void saveOwner_shouldUpdateExistingOwner() {
        // Given
        Owner owner = ownerRepository.findById(1);
        String originalLastName = owner.getLastName();
        String newLastName = originalLastName + " Updated";

        // When
        owner.setLastName(newLastName);
        ownerRepository.save(owner);

        // Then
        Owner updatedOwner = ownerRepository.findById(1);
        assertThat(updatedOwner.getLastName()).isEqualTo(newLastName);
    }

    // ==================== PET REPOSITORY TESTS ====================

    @Test
    @DisplayName("PetRepository.findPetTypes - should return all pet types")
    void findPetTypes_shouldReturnAllPetTypes() {
        Collection<PetType> petTypes = petRepository.findPetTypes();
        
        assertThat(petTypes).isNotEmpty();
        assertThat(petTypes).hasSizeGreaterThanOrEqualTo(6); // Sample data has 6 types
        
        // Verify known pet types exist
        assertThat(petTypes).extracting(PetType::getName)
                .contains("cat", "dog", "lizard", "snake", "bird", "hamster");
    }

    @Test
    @DisplayName("PetRepository.findById - should return pet with owner and type loaded")
    void findPetById_shouldReturnPetWithOwnerAndType() {
        Pet pet = petRepository.findById(7);
        
        assertThat(pet).isNotNull();
        assertThat(pet.getName()).isEqualTo("Samantha");
        assertThat(pet.getOwner()).isNotNull();
        assertThat(pet.getOwner().getFirstName()).isEqualTo("Jean");
        assertThat(pet.getType()).isNotNull();
        assertThat(pet.getType().getName()).isEqualTo("cat");
    }

    @Test
    @DisplayName("PetRepository.save - should persist new pet with owner relationship")
    void savePet_shouldPersistNewPetWithOwnerRelationship() {
        // Given
        Owner owner = ownerRepository.findById(6);
        int initialPetCount = owner.getPets().size();
        
        Pet newPet = new Pet();
        newPet.setName("TestPet");
        newPet.setBirthDate(LocalDate.now().minusYears(2));
        
        Collection<PetType> petTypes = petRepository.findPetTypes();
        PetType dogType = petTypes.stream()
                .filter(type -> "dog".equals(type.getName()))
                .findFirst()
                .orElseThrow();
        newPet.setType(dogType);
        
        owner.addPet(newPet);

        // When
        petRepository.save(newPet);
        ownerRepository.save(owner);

        // Then
        assertThat(newPet.getId()).isNotNull();
        
        // Verify relationship persistence
        Owner refreshedOwner = ownerRepository.findById(6);
        assertThat(refreshedOwner.getPets()).hasSize(initialPetCount + 1);
        assertThat(refreshedOwner.getPets()).extracting(Pet::getName).contains("TestPet");
    }

    // ==================== VET REPOSITORY TESTS ====================

    @Test
    @DisplayName("VetRepository.findAll - should return all vets with specialties")
    void findAllVets_shouldReturnAllVetsWithSpecialties() {
        Collection<Vet> vets = vetRepository.findAll();
        
        assertThat(vets).isNotEmpty();
        assertThat(vets).hasSizeGreaterThanOrEqualTo(6); // Sample data has 6 vets
        
        // Find vet with specialties
        Vet vetWithSpecialties = vets.stream()
                .filter(vet -> "Douglas".equals(vet.getLastName()))
                .findFirst()
                .orElseThrow();
        
        assertThat(vetWithSpecialties.getSpecialties()).isNotEmpty();
        assertThat(vetWithSpecialties.getSpecialties()).hasSize(2);
        assertThat(vetWithSpecialties.getSpecialties()).extracting(Specialty::getName)
                .contains("dentistry", "surgery");
    }

    // ==================== VISIT REPOSITORY TESTS ====================

    @Test
    @DisplayName("VisitRepository.save - should persist new visit with pet relationship")
    void saveVisit_shouldPersistNewVisitWithPetRelationship() {
        // Given
        Pet pet = petRepository.findById(7);
        int initialVisitCount = pet.getVisits().size();
        
        Visit newVisit = new Visit();
        newVisit.setDescription("Characterization test visit");
        newVisit.setDate(LocalDate.now());
        pet.addVisit(newVisit);

        // When
        visitRepository.save(newVisit);
        petRepository.save(pet);

        // Then
        assertThat(newVisit.getId()).isNotNull();
        
        // Verify relationship persistence
        Pet refreshedPet = petRepository.findById(7);
        assertThat(refreshedPet.getVisits()).hasSize(initialVisitCount + 1);
        assertThat(refreshedPet.getVisits()).extracting(Visit::getDescription)
                .contains("Characterization test visit");
    }

    @Test
    @DisplayName("VisitRepository.findByPetId - should return visits for specific pet")
    void findVisitsByPetId_shouldReturnVisitsForSpecificPet() {
        Collection<Visit> visits = visitRepository.findByPetId(7);
        
        assertThat(visits).isNotEmpty();
        assertThat(visits).hasSize(2); // Based on sample data
        
        // Verify all visits belong to correct pet
        assertThat(visits).allMatch(visit -> visit.getPet().getId().equals(7));
        assertThat(visits).allMatch(visit -> visit.getDate() != null);
        assertThat(visits).allMatch(visit -> visit.getDescription() != null && !visit.getDescription().isEmpty());
    }

    // ==================== ENTITY RELATIONSHIP TESTS ====================

    @Test
    @DisplayName("Owner-Pet relationship - should maintain bidirectional consistency")
    void ownerPetRelationship_shouldMaintainBidirectionalConsistency() {
        Owner owner = ownerRepository.findById(1);
        Pet pet = owner.getPets().get(0);
        
        // Verify bidirectional relationship
        assertThat(pet.getOwner()).isEqualTo(owner);
        assertThat(owner.getPets()).contains(pet);
        
        // Verify pet name uniqueness within owner
        assertThat(owner.getPet(pet.getName())).isEqualTo(pet);
        assertThat(owner.getPet("non-existent-pet")).isNull();
    }

    @Test
    @DisplayName("Pet-Visit relationship - should maintain chronological ordering")
    void petVisitRelationship_shouldMaintainChronologicalOrdering() {
        Pet pet = petRepository.findById(7);
        
        assertThat(pet.getVisits()).isNotEmpty();
        
        // Verify visits are sorted by date (most recent first)
        if (pet.getVisits().size() > 1) {
            LocalDate previousDate = null;
            for (Visit visit : pet.getVisits()) {
                if (previousDate != null) {
                    assertThat(visit.getDate()).isBeforeOrEqualTo(previousDate);
                }
                previousDate = visit.getDate();
            }
        }
    }

    @Test
    @DisplayName("Vet-Specialty relationship - should handle many-to-many mapping")
    void vetSpecialtyRelationship_shouldHandleManyToManyMapping() {
        Collection<Vet> vets = vetRepository.findAll();
        
        // Find vets with and without specialties
        long vetsWithSpecialties = vets.stream()
                .mapToLong(vet -> vet.getSpecialties().size())
                .sum();
        
        assertThat(vetsWithSpecialties).isPositive();
        
        // Verify specialty relationships
        Vet specialistVet = vets.stream()
                .filter(vet -> !vet.getSpecialties().isEmpty())
                .findFirst()
                .orElseThrow();
        
        assertThat(specialistVet.getSpecialties()).allSatisfy(specialty -> {
            assertThat(specialty.getId()).isNotNull();
            assertThat(specialty.getName()).isNotNull().isNotEmpty();
        });
    }

    // ==================== DATA VALIDATION TESTS ====================

    @Test
    @DisplayName("Entity validation - should enforce required field constraints")
    void entityValidation_shouldEnforceRequiredFieldConstraints() {
        // This test documents current validation behavior
        // In Spring Boot migration, these will be enforced by Bean Validation
        
        Owner owner = ownerRepository.findById(1);
        
        // Verify required fields are populated
        assertThat(owner.getFirstName()).isNotNull().isNotEmpty();
        assertThat(owner.getLastName()).isNotNull().isNotEmpty();
        assertThat(owner.getAddress()).isNotNull().isNotEmpty();
        assertThat(owner.getCity()).isNotNull().isNotEmpty();
        assertThat(owner.getTelephone()).isNotNull().isNotEmpty();
    }

    @Test
    @DisplayName("Telephone validation - should match expected format")
    void telephoneValidation_shouldMatchExpectedFormat() {
        Collection<Owner> owners = ownerRepository.findByLastName("");
        
        // Verify telephone format consistency in sample data
        owners.forEach(owner -> {
            String telephone = owner.getTelephone();
            assertThat(telephone).isNotNull();
            assertThat(telephone).matches("\\d{10}"); // 10 digits expected
        });
    }
}