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
package org.springframework.samples.petclinic.contract;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.petclinic.model.*;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;

import java.time.LocalDate;
import java.util.Collection;

import static org.assertj.core.api.Assertions.*;

/**
 * API Contract tests to define stable JSON interfaces for future Next.js migration.
 * 
 * <p>These tests establish the contract that Next.js frontend will depend on:
 * - JSON field names and types
 * - Response structure and nesting
 * - Error response formats
 * - Status code semantics
 * - Date/time serialization formats
 * 
 * <p>Migration strategy: These contracts will guide REST API development during
 * the transition from JSP views to Next.js components, ensuring consistent
 * data exchange between backend and frontend.
 * 
 * @author Migration Team
 */
@SpringJUnitWebConfig(locations = {"classpath:spring/business-config.xml"})
class ApiContractCharacterizationTest {

    @Autowired private ClinicService clinicService;
    
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules(); // Support for LocalDate serialization
    }

    // ==================== OWNER API CONTRACT ====================

    @Test
    @DisplayName("Owner JSON Contract - should serialize with stable field names")
    void ownerJsonContract_shouldSerializeWithStableFieldNames() throws Exception {
        // Given
        Owner owner = clinicService.findOwnerById(1);
        
        // When - Serialize to JSON
        String json = objectMapper.writeValueAsString(owner);
        
        // Then - Verify contract structure
        assertThat(json).contains("\"id\":");
        assertThat(json).contains("\"firstName\":");
        assertThat(json).contains("\"lastName\":");
        assertThat(json).contains("\"address\":");
        assertThat(json).contains("\"city\":");
        assertThat(json).contains("\"telephone\":");
        assertThat(json).contains("\"pets\":");
        
        // Verify field values
        assertThat(json).contains("\"firstName\":\"George\"");
        assertThat(json).contains("\"lastName\":\"Franklin\"");
        
        // Verify deserialization round-trip
        Owner deserializedOwner = objectMapper.readValue(json, Owner.class);
        assertThat(deserializedOwner.getId()).isEqualTo(owner.getId());
        assertThat(deserializedOwner.getFirstName()).isEqualTo(owner.getFirstName());
        assertThat(deserializedOwner.getLastName()).isEqualTo(owner.getLastName());
    }

    @Test
    @DisplayName("Owner List JSON Contract - should serialize collection with consistent structure")
    void ownerListJsonContract_shouldSerializeCollectionConsistently() throws Exception {
        // Given
        Collection<Owner> owners = clinicService.findOwnerByLastName("Davis");
        
        // When - Serialize to JSON
        String json = objectMapper.writeValueAsString(owners);
        
        // Then - Verify array structure
        assertThat(json).startsWith("[");
        assertThat(json).endsWith("]");
        assertThat(json).contains("\"id\":");
        assertThat(json).contains("\"firstName\":");
        
        // Verify deserialization
        Owner[] ownerArray = objectMapper.readValue(json, Owner[].class);
        assertThat(ownerArray).hasSizeGreaterThan(0);
        assertThat(ownerArray[0].getLastName()).isEqualTo("Davis");
    }

    // ==================== PET API CONTRACT ====================

    @Test
    @DisplayName("Pet JSON Contract - should serialize with owner and type relationships")
    void petJsonContract_shouldSerializeWithRelationships() throws Exception {
        // Given
        Pet pet = clinicService.findPetById(7);
        
        // When - Serialize to JSON
        String json = objectMapper.writeValueAsString(pet);
        
        // Then - Verify contract structure
        assertThat(json).contains("\"id\":");
        assertThat(json).contains("\"name\":");
        assertThat(json).contains("\"birthDate\":");
        assertThat(json).contains("\"type\":");
        assertThat(json).contains("\"owner\":");
        assertThat(json).contains("\"visits\":");
        
        // Verify nested objects
        assertThat(json).contains("\"type\":{\"id\":");
        assertThat(json).contains("\"owner\":{\"id\":");
        
        // Verify date format (ISO-8601 expected)
        assertThat(json).containsPattern("\"birthDate\":\"\\d{4}-\\d{2}-\\d{2}\"");
    }

    @Test
    @DisplayName("Pet Type JSON Contract - should serialize reference data consistently")
    void petTypeJsonContract_shouldSerializeReferenceDataConsistently() throws Exception {
        // Given
        Collection<PetType> petTypes = clinicService.findPetTypes();
        
        // When - Serialize to JSON
        String json = objectMapper.writeValueAsString(petTypes);
        
        // Then - Verify structure
        assertThat(json).startsWith("[");
        assertThat(json).contains("\"id\":");
        assertThat(json).contains("\"name\":");
        
        // Verify known types exist
        assertThat(json).contains("\"name\":\"cat\"");
        assertThat(json).contains("\"name\":\"dog\"");
        
        // Verify deserialization
        PetType[] typeArray = objectMapper.readValue(json, PetType[].class);
        assertThat(typeArray).hasSizeGreaterThanOrEqualTo(6);
    }

    // ==================== VISIT API CONTRACT ====================

    @Test
    @DisplayName("Visit JSON Contract - should serialize with pet relationship and date formatting")
    void visitJsonContract_shouldSerializeWithPetAndDateFormatting() throws Exception {
        // Given
        Collection<Visit> visits = clinicService.findVisitsByPetId(7);
        Visit visit = visits.iterator().next();
        
        // When - Serialize to JSON
        String json = objectMapper.writeValueAsString(visit);
        
        // Then - Verify contract structure
        assertThat(json).contains("\"id\":");
        assertThat(json).contains("\"date\":");
        assertThat(json).contains("\"description\":");
        assertThat(json).contains("\"pet\":");
        
        // Verify date format consistency
        assertThat(json).containsPattern("\"date\":\"\\d{4}-\\d{2}-\\d{2}\"");
        
        // Verify pet relationship
        assertThat(json).contains("\"pet\":{\"id\":");
    }

    @Test
    @DisplayName("Visit List JSON Contract - should serialize chronologically ordered visits")
    void visitListJsonContract_shouldSerializeChronologicallyOrderedVisits() throws Exception {
        // Given
        Collection<Visit> visits = clinicService.findVisitsByPetId(7);
        
        // When - Serialize to JSON
        String json = objectMapper.writeValueAsString(visits);
        
        // Then - Verify array structure
        assertThat(json).startsWith("[");
        assertThat(json).endsWith("]");
        
        // Verify deserialization maintains order
        Visit[] visitArray = objectMapper.readValue(json, Visit[].class);
        assertThat(visitArray).hasSizeGreaterThan(0);
        
        // Verify chronological order (most recent first)
        if (visitArray.length > 1) {
            LocalDate previousDate = null;
            for (Visit visit : visitArray) {
                if (previousDate != null) {
                    assertThat(visit.getDate()).isBeforeOrEqualTo(previousDate);
                }
                previousDate = visit.getDate();
            }
        }
    }

    // ==================== VET API CONTRACT ====================

    @Test
    @DisplayName("Vet JSON Contract - should serialize with specialties relationship")
    void vetJsonContract_shouldSerializeWithSpecialties() throws Exception {
        // Given
        Collection<Vet> vets = clinicService.findVets();
        Vet vetWithSpecialties = vets.stream()
                .filter(vet -> !vet.getSpecialties().isEmpty())
                .findFirst()
                .orElseThrow();
        
        // When - Serialize to JSON
        String json = objectMapper.writeValueAsString(vetWithSpecialties);
        
        // Then - Verify contract structure
        assertThat(json).contains("\"id\":");
        assertThat(json).contains("\"firstName\":");
        assertThat(json).contains("\"lastName\":");
        assertThat(json).contains("\"specialties\":");
        
        // Verify specialties array structure
        assertThat(json).contains("\"specialties\":[");
        assertThat(json).contains("\"name\":");
    }

    @Test
    @DisplayName("Vet List JSON Contract - should serialize with caching considerations")
    void vetListJsonContract_shouldSerializeWithCachingConsiderations() throws Exception {
        // Given
        Collection<Vet> vets = clinicService.findVets(); // This should be cacheable
        
        // When - Serialize to JSON
        String json = objectMapper.writeValueAsString(vets);
        
        // Then - Verify structure suitable for caching
        assertThat(json).startsWith("[");
        assertThat(json).contains("\"id\":");
        
        // Verify consistent serialization (important for caching)
        String json2 = objectMapper.writeValueAsString(vets);
        assertThat(json).isEqualTo(json2);
        
        // Verify deserialization
        Vet[] vetArray = objectMapper.readValue(json, Vet[].class);
        assertThat(vetArray).hasSizeGreaterThanOrEqualTo(6);
    }

    // ==================== ERROR RESPONSE CONTRACT ====================

    @Test
    @DisplayName("Error Response Contract - should define standard error format for Next.js")
    void errorResponseContract_shouldDefineStandardErrorFormat() throws Exception {
        // This test defines the expected error response structure for Next.js consumption
        
        // Expected error response structure:
        var errorResponse = new ErrorResponse(
                "OWNER_NOT_FOUND",
                "Owner with ID 999 not found",
                404,
                "/api/owners/999",
                System.currentTimeMillis()
        );
        
        // When - Serialize to JSON
        String json = objectMapper.writeValueAsString(errorResponse);
        
        // Then - Verify error contract
        assertThat(json).contains("\"code\":\"OWNER_NOT_FOUND\"");
        assertThat(json).contains("\"message\":\"Owner with ID 999 not found\"");
        assertThat(json).contains("\"status\":404");
        assertThat(json).contains("\"path\":\"/api/owners/999\"");
        assertThat(json).contains("\"timestamp\":");
        
        // Verify deserialization
        ErrorResponse deserializedError = objectMapper.readValue(json, ErrorResponse.class);
        assertThat(deserializedError.code()).isEqualTo("OWNER_NOT_FOUND");
        assertThat(deserializedError.status()).isEqualTo(404);
    }

    @Test
    @DisplayName("Validation Error Contract - should define field-specific error format")
    void validationErrorContract_shouldDefineFieldSpecificErrorFormat() throws Exception {
        // Expected validation error structure for Next.js form handling
        
        var validationError = new ValidationErrorResponse(
                "VALIDATION_FAILED",
                "Request validation failed",
                400,
                "/api/owners",
                System.currentTimeMillis(),
                java.util.Map.of(
                        "firstName", "First name is required",
                        "telephone", "Telephone must be 10 digits"
                )
        );
        
        // When - Serialize to JSON
        String json = objectMapper.writeValueAsString(validationError);
        
        // Then - Verify validation error contract
        assertThat(json).contains("\"code\":\"VALIDATION_FAILED\"");
        assertThat(json).contains("\"fieldErrors\":");
        assertThat(json).contains("\"firstName\":\"First name is required\"");
        assertThat(json).contains("\"telephone\":\"Telephone must be 10 digits\"");
        
        // Verify deserialization
        ValidationErrorResponse deserializedError = objectMapper.readValue(json, ValidationErrorResponse.class);
        assertThat(deserializedError.fieldErrors()).containsKey("firstName");
    }

    // ==================== SEARCH RESPONSE CONTRACT ====================

    @Test
    @DisplayName("Search Response Contract - should define pageable search results")
    void searchResponseContract_shouldDefinePageableSearchResults() throws Exception {
        // Future pagination support for large datasets
        Collection<Owner> owners = clinicService.findOwnerByLastName("Davis");
        
        var searchResponse = new SearchResponse<>(
                owners,
                owners.size(),
                0, // page
                10, // size
                1, // totalPages
                false, // hasNext
                false  // hasPrevious
        );
        
        // When - Serialize to JSON
        String json = objectMapper.writeValueAsString(searchResponse);
        
        // Then - Verify search contract
        assertThat(json).contains("\"content\":");
        assertThat(json).contains("\"totalElements\":");
        assertThat(json).contains("\"page\":");
        assertThat(json).contains("\"size\":");
        assertThat(json).contains("\"totalPages\":");
        assertThat(json).contains("\"hasNext\":");
        assertThat(json).contains("\"hasPrevious\":");
    }

    // ==================== CONTRACT HELPER CLASSES ====================

    /**
     * Standard error response structure for Next.js error handling
     */
    record ErrorResponse(
            String code,
            String message,
            int status,
            String path,
            long timestamp
    ) {}

    /**
     * Validation error response with field-specific error details
     */
    record ValidationErrorResponse(
            String code,
            String message,
            int status,
            String path,
            long timestamp,
            java.util.Map<String, String> fieldErrors
    ) {}

    /**
     * Pageable search response structure for large datasets
     */
    record SearchResponse<T>(
            Collection<T> content,
            int totalElements,
            int page,
            int size,
            int totalPages,
            boolean hasNext,
            boolean hasPrevious
    ) {}
}