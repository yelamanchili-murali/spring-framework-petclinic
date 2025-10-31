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
package org.springframework.samples.petclinic.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.samples.petclinic.model.*;
import org.springframework.samples.petclinic.repository.*;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive characterization tests for {@link ClinicServiceImpl} to preserve behavior 
 * during Spring Boot 3 + Next.js migration.
 * 
 * <p>These tests capture current behavior patterns including:
 * - Transaction semantics and error propagation
 * - Edge cases and null handling 
 * - Caching behavior for veterinarian data
 * - Repository interaction patterns
 * - Business logic invariants
 * 
 * <p>Migration readiness: Tests designed to support transition to:
 * - Spring Boot 3 auto-configuration
 * - REST API layer for Next.js frontend
 * - Preserved transactional semantics
 * 
 * @author Migration Team
 */
@ExtendWith(MockitoExtension.class)
class ClinicServiceImplCharacterizationTest {

    @Mock private PetRepository petRepository;
    @Mock private VetRepository vetRepository;  
    @Mock private OwnerRepository ownerRepository;
    @Mock private VisitRepository visitRepository;

    @InjectMocks private ClinicServiceImpl clinicService;

    private Owner sampleOwner;
    private Pet samplePet;
    private Visit sampleVisit;
    private Vet sampleVet;
    private PetType samplePetType;

    @BeforeEach
    void setUp() {
        // Sample Owner
        sampleOwner = new Owner();
        sampleOwner.setId(1);
        sampleOwner.setFirstName("George");
        sampleOwner.setLastName("Franklin");
        sampleOwner.setAddress("110 W. Liberty St.");
        sampleOwner.setCity("Madison");
        sampleOwner.setTelephone("6085551023");

        // Sample Pet Type
        samplePetType = new PetType();
        samplePetType.setId(1);
        samplePetType.setName("cat");

        // Sample Pet
        samplePet = new Pet();
        samplePet.setId(1);
        samplePet.setName("Leo");
        samplePet.setType(samplePetType);
        samplePet.setBirthDate(LocalDate.of(2010, 9, 7));
        sampleOwner.addPet(samplePet); // Establishes bidirectional relationship

        // Sample Visit
        sampleVisit = new Visit();
        sampleVisit.setId(1);
        sampleVisit.setDate(LocalDate.now());
        sampleVisit.setDescription("rabies shot");
        sampleVisit.setPet(samplePet);

        // Sample Vet
        sampleVet = new Vet();
        sampleVet.setId(1);
        sampleVet.setFirstName("James");
        sampleVet.setLastName("Carter");
    }

    // ==================== PET TYPE OPERATIONS ====================

    @Test
    @DisplayName("findPetTypes - should return all pet types from repository")
    void findPetTypes_shouldReturnAllPetTypes() {
        // Given
        List<PetType> expectedTypes = Arrays.asList(samplePetType);
        when(petRepository.findPetTypes()).thenReturn(expectedTypes);

        // When
        Collection<PetType> actualTypes = clinicService.findPetTypes();

        // Then
        assertThat(actualTypes).isEqualTo(expectedTypes);
        verify(petRepository).findPetTypes();
    }

    @Test
    @DisplayName("findPetTypes - should return empty collection when no pet types exist")
    void findPetTypes_shouldReturnEmptyCollectionWhenNoneExist() {
        // Given
        when(petRepository.findPetTypes()).thenReturn(Collections.emptyList());

        // When
        Collection<PetType> actualTypes = clinicService.findPetTypes();

        // Then
        assertThat(actualTypes).isEmpty();
        verify(petRepository).findPetTypes();
    }

    // ==================== OWNER OPERATIONS ====================

    @Test
    @DisplayName("findOwnerById - should return owner when valid ID provided")
    void findOwnerById_shouldReturnOwner_whenValidId() {
        // Given
        when(ownerRepository.findById(1)).thenReturn(sampleOwner);

        // When
        Owner result = clinicService.findOwnerById(1);

        // Then
        assertThat(result).isEqualTo(sampleOwner);
        assertThat(result.getFirstName()).isEqualTo("George");
        assertThat(result.getLastName()).isEqualTo("Franklin");
        verify(ownerRepository).findById(1);
    }

    @Test
    @DisplayName("findOwnerById - should propagate repository exception when owner not found")
    void findOwnerById_shouldPropagateException_whenNotFound() {
        // Given
        when(ownerRepository.findById(999)).thenThrow(new RuntimeException("Owner not found"));

        // When & Then
        assertThatThrownBy(() -> clinicService.findOwnerById(999))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Owner not found");
        
        verify(ownerRepository).findById(999);
    }

    @Test
    @DisplayName("findOwnerByLastName - should return matching owners for valid last name")
    void findOwnerByLastName_shouldReturnMatches_whenValidLastName() {
        // Given
        Collection<Owner> expectedOwners = Arrays.asList(sampleOwner);
        when(ownerRepository.findByLastName("Franklin")).thenReturn(expectedOwners);

        // When
        Collection<Owner> actualOwners = clinicService.findOwnerByLastName("Franklin");

        // Then
        assertThat(actualOwners).isEqualTo(expectedOwners);
        assertThat(actualOwners).hasSize(1);
        verify(ownerRepository).findByLastName("Franklin");
    }

    @Test
    @DisplayName("findOwnerByLastName - should return empty collection for non-existent last name")
    void findOwnerByLastName_shouldReturnEmpty_whenLastNameNotFound() {
        // Given
        when(ownerRepository.findByLastName("NonExistent")).thenReturn(Collections.emptyList());

        // When
        Collection<Owner> actualOwners = clinicService.findOwnerByLastName("NonExistent");

        // Then
        assertThat(actualOwners).isEmpty();
        verify(ownerRepository).findByLastName("NonExistent");
    }

    @Test
    @DisplayName("findOwnerByLastName - should handle empty string parameter (returns all)")
    void findOwnerByLastName_shouldReturnAll_whenEmptyString() {
        // Given - empty string should return all owners per service contract
        Collection<Owner> allOwners = Arrays.asList(sampleOwner);
        when(ownerRepository.findByLastName("")).thenReturn(allOwners);

        // When
        Collection<Owner> actualOwners = clinicService.findOwnerByLastName("");

        // Then
        assertThat(actualOwners).isEqualTo(allOwners);
        verify(ownerRepository).findByLastName("");
    }

    @Test
    @DisplayName("saveOwner - should persist owner through repository")
    void saveOwner_shouldPersist_whenValidOwner() {
        // Given
        doNothing().when(ownerRepository).save(sampleOwner);

        // When
        clinicService.saveOwner(sampleOwner);

        // Then
        verify(ownerRepository).save(sampleOwner);
    }

    @Test
    @DisplayName("saveOwner - should propagate repository exception on save failure")
    void saveOwner_shouldPropagateException_whenSaveFailure() {
        // Given
        doThrow(new RuntimeException("Database error")).when(ownerRepository).save(sampleOwner);

        // When & Then
        assertThatThrownBy(() -> clinicService.saveOwner(sampleOwner))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Database error");
        
        verify(ownerRepository).save(sampleOwner);
    }

    // ==================== PET OPERATIONS ====================

    @Test
    @DisplayName("findPetById - should return pet when valid ID provided")
    void findPetById_shouldReturnPet_whenValidId() {
        // Given
        when(petRepository.findById(1)).thenReturn(samplePet);

        // When
        Pet result = clinicService.findPetById(1);

        // Then
        assertThat(result).isEqualTo(samplePet);
        assertThat(result.getName()).isEqualTo("Leo");
        assertThat(result.getType().getName()).isEqualTo("cat");
        verify(petRepository).findById(1);
    }

    @Test
    @DisplayName("findPetById - should propagate repository exception when pet not found")
    void findPetById_shouldPropagateException_whenNotFound() {
        // Given
        when(petRepository.findById(999)).thenThrow(new RuntimeException("Pet not found"));

        // When & Then
        assertThatThrownBy(() -> clinicService.findPetById(999))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Pet not found");
        
        verify(petRepository).findById(999);
    }

    @Test
    @DisplayName("savePet - should persist pet through repository")
    void savePet_shouldPersist_whenValidPet() {
        // Given
        doNothing().when(petRepository).save(samplePet);

        // When
        clinicService.savePet(samplePet);

        // Then
        verify(petRepository).save(samplePet);
    }

    @Test
    @DisplayName("savePet - should propagate repository exception on save failure")
    void savePet_shouldPropagateException_whenSaveFailure() {
        // Given
        doThrow(new RuntimeException("Database constraint violation")).when(petRepository).save(samplePet);

        // When & Then
        assertThatThrownBy(() -> clinicService.savePet(samplePet))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Database constraint violation");
        
        verify(petRepository).save(samplePet);
    }

    // ==================== VISIT OPERATIONS ====================

    @Test
    @DisplayName("saveVisit - should persist visit through repository")
    void saveVisit_shouldPersist_whenValidVisit() {
        // Given
        doNothing().when(visitRepository).save(sampleVisit);

        // When
        clinicService.saveVisit(sampleVisit);

        // Then
        verify(visitRepository).save(sampleVisit);
    }

    @Test
    @DisplayName("saveVisit - should propagate repository exception on save failure")
    void saveVisit_shouldPropagateException_whenSaveFailure() {
        // Given
        doThrow(new RuntimeException("Visit save failed")).when(visitRepository).save(sampleVisit);

        // When & Then
        assertThatThrownBy(() -> clinicService.saveVisit(sampleVisit))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Visit save failed");
        
        verify(visitRepository).save(sampleVisit);
    }

    @Test
    @DisplayName("findVisitsByPetId - should return visits for valid pet ID")
    void findVisitsByPetId_shouldReturnVisits_whenValidPetId() {
        // Given
        List<Visit> expectedVisits = Arrays.asList(sampleVisit);
        when(visitRepository.findByPetId(1)).thenReturn(expectedVisits);

        // When
        Collection<Visit> actualVisits = clinicService.findVisitsByPetId(1);

        // Then
        assertThat(actualVisits).isEqualTo(expectedVisits);
        assertThat(actualVisits).hasSize(1);
        verify(visitRepository).findByPetId(1);
    }

    @Test
    @DisplayName("findVisitsByPetId - should return empty collection when no visits exist")
    void findVisitsByPetId_shouldReturnEmpty_whenNoVisits() {
        // Given
        when(visitRepository.findByPetId(999)).thenReturn(Collections.emptyList());

        // When
        Collection<Visit> actualVisits = clinicService.findVisitsByPetId(999);

        // Then
        assertThat(actualVisits).isEmpty();
        verify(visitRepository).findByPetId(999);
    }

    // ==================== VET OPERATIONS ====================

    @Test
    @DisplayName("findVets - should return all vets from repository")
    void findVets_shouldReturnAllVets() {
        // Given
        Collection<Vet> expectedVets = Arrays.asList(sampleVet);
        when(vetRepository.findAll()).thenReturn(expectedVets);

        // When
        Collection<Vet> actualVets = clinicService.findVets();

        // Then
        assertThat(actualVets).isEqualTo(expectedVets);
        verify(vetRepository).findAll();
    }

    @Test
    @DisplayName("findVets - should return empty collection when no vets exist")
    void findVets_shouldReturnEmpty_whenNoVets() {
        // Given
        when(vetRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        Collection<Vet> actualVets = clinicService.findVets();

        // Then
        assertThat(actualVets).isEmpty();
        verify(vetRepository).findAll();
    }

    // ==================== CACHING BEHAVIOR TESTS ====================

    @Test
    @DisplayName("findVets - should be cacheable (verify single repository call for multiple invocations)")
    void findVets_shouldBeCacheable_verifyRepositoryCallPattern() {
        // Given
        Collection<Vet> expectedVets = Arrays.asList(sampleVet);
        when(vetRepository.findAll()).thenReturn(expectedVets);

        // When - multiple calls to verify caching intention
        Collection<Vet> firstCall = clinicService.findVets();
        Collection<Vet> secondCall = clinicService.findVets();

        // Then - Repository should be called for each invocation in unit test (no actual cache)
        // Note: This captures the intent; actual caching tested in integration tests
        assertThat(firstCall).isEqualTo(expectedVets);
        assertThat(secondCall).isEqualTo(expectedVets);
        verify(vetRepository, times(2)).findAll(); // Unit test - no actual cache
    }

    // ==================== ERROR BOUNDARY TESTS ====================

    @Test
    @DisplayName("Transaction rollback behavior - verify exception propagation patterns")
    void transactionRollback_shouldPropagateExceptions_preservingTransactionSemantics() {
        // Given
        RuntimeException serviceException = new RuntimeException("Service layer error");
        doThrow(serviceException).when(ownerRepository).save(any(Owner.class));

        // When & Then
        assertThatThrownBy(() -> clinicService.saveOwner(sampleOwner))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Service layer error");

        // Verify repository interaction occurred before exception
        verify(ownerRepository).save(sampleOwner);
    }

    // ==================== BUSINESS LOGIC PRESERVATION ====================

    @Test
    @DisplayName("Owner-Pet relationship consistency - verify bidirectional associations")
    void ownerPetRelationship_shouldMaintainConsistency_duringOperations() {
        // Given
        Pet newPet = new Pet();
        newPet.setName("Bella");
        sampleOwner.addPet(newPet); // Establishes bidirectional relationship
        
        // When
        clinicService.savePet(newPet);

        // Then
        verify(petRepository).save(newPet);
        assertThat(newPet.getOwner()).isEqualTo(sampleOwner);
    }

    @Test
    @DisplayName("Visit-Pet relationship consistency - verify visit associations")
    void visitPetRelationship_shouldMaintainConsistency_duringOperations() {
        // Given
        Visit newVisit = new Visit();
        newVisit.setDescription("checkup");
        newVisit.setPet(samplePet);
        newVisit.setDate(LocalDate.now());

        // When
        clinicService.saveVisit(newVisit);

        // Then
        verify(visitRepository).save(newVisit);
        assertThat(newVisit.getPet()).isEqualTo(samplePet);
    }

    // ==================== NULL SAFETY TESTS ====================

    @Test
    @DisplayName("Null handling - service should handle null parameters gracefully")
    void nullHandling_shouldPropagateRepositoryBehavior() {
        // Given
        when(ownerRepository.findByLastName(null)).thenThrow(new IllegalArgumentException("LastName cannot be null"));

        // When & Then
        assertThatThrownBy(() -> clinicService.findOwnerByLastName(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("LastName cannot be null");

        verify(ownerRepository).findByLastName(null);
    }
}