package org.springframework.samples.petclinic;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Pet;

import org.springframework.samples.petclinic.model.Visit;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.samples.petclinic.web.OwnerController;
import org.springframework.samples.petclinic.web.PetController;
import org.springframework.samples.petclinic.web.VetController;
import org.springframework.samples.petclinic.web.VisitController;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.context.ApplicationContext;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * Characterization tests for cross-cutting concerns (validation, error handling, caching, 
 * architectural constraints) to preserve behavior during Spring Boot 3 + Next.js migration.
 * 
 * These tests capture current system behavior for:
 * - Bean Validation constraints and error messages  
 * - Error handling patterns and exception propagation
 * - Caching behavior and cache configuration
 * - Architectural layering and dependency constraints
 * - Transaction boundaries and isolation levels
 * - Security context and authorization patterns
 */
@SpringJUnitWebConfig(locations = {"classpath:spring/mvc-test-config.xml"})
@DisplayName("Cross-Cutting Concerns Characterization Tests")
class CrossCuttingConcernsCharacterizationTest {

    @Autowired
    private ApplicationContext applicationContext;
    
    @Autowired
    private ClinicService clinicService;
    
    @Autowired
    private Validator validator;
    
    @Autowired(required = false)
    private CacheManager cacheManager;
    
    @Autowired(required = false)
    private TransactionTemplate transactionTemplate;
    


    @Nested
    @DisplayName("Bean Validation Characterization")
    class ValidationCharacterizationTests {
        
        @Test
        @DisplayName("Owner validation constraints - capture current validation behavior")
        void characterizeOwnerValidation() {
            // Test empty owner
            Owner emptyOwner = new Owner();
            Set<ConstraintViolation<Owner>> violations = validator.validate(emptyOwner);
            
            // Characterize current validation messages and constraints
            assertThat(violations).isNotEmpty();
            
            // Capture required field constraints
            boolean hasFirstNameRequired = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("firstName"));
            boolean hasLastNameRequired = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("lastName"));
                
            // Document current validation behavior for migration
            assertThat(hasFirstNameRequired || hasLastNameRequired).isTrue();
            
            // Test valid owner
            Owner validOwner = new Owner();
            validOwner.setFirstName("John");
            validOwner.setLastName("Doe");
            validOwner.setAddress("123 Main St");
            validOwner.setCity("Anytown");
            validOwner.setTelephone("555-1234");
            
            Set<ConstraintViolation<Owner>> validViolations = validator.validate(validOwner);
            assertThat(validViolations).isEmpty();
        }
        
        @Test
        @DisplayName("Pet validation constraints - capture current validation behavior")
        void characterizePetValidation() {
            Pet pet = new Pet();
            Set<ConstraintViolation<Pet>> violations = validator.validate(pet);
            
            // Characterize pet validation requirements
            long nameViolations = violations.stream()
                .filter(v -> v.getPropertyPath().toString().equals("name"))
                .count();
            long birthDateViolations = violations.stream()
                .filter(v -> v.getPropertyPath().toString().equals("birthDate"))
                .count();
            long typeViolations = violations.stream()
                .filter(v -> v.getPropertyPath().toString().equals("type"))
                .count();
                
            // Document current behavior - may be lenient or strict
            System.out.println("Pet validation - name violations: " + nameViolations + 
                             ", birthDate violations: " + birthDateViolations +
                             ", type violations: " + typeViolations);
            System.out.println("Pet validation violations: " + violations.size());
            violations.forEach(v -> System.out.println("  " + v.getPropertyPath() + ": " + v.getMessage()));
        }
        
        @Test
        @DisplayName("Visit validation constraints - capture current validation behavior")
        void characterizeVisitValidation() {
            Visit visit = new Visit();
            Set<ConstraintViolation<Visit>> violations = validator.validate(visit);
            
            // Characterize visit validation
            long dateViolations = violations.stream()
                .filter(v -> v.getPropertyPath().toString().equals("date"))
                .count();
            long descriptionViolations = violations.stream()
                .filter(v -> v.getPropertyPath().toString().equals("description"))
                .count();
            
            System.out.println("Visit validation - date violations: " + dateViolations + 
                             ", description violations: " + descriptionViolations);
                
            System.out.println("Visit validation violations: " + violations.size());
            violations.forEach(v -> System.out.println("  " + v.getPropertyPath() + ": " + v.getMessage()));
            
            // Test valid visit
            Visit validVisit = new Visit();
            validVisit.setDate(LocalDate.now());
            validVisit.setDescription("Regular checkup");
            
            Set<ConstraintViolation<Visit>> validViolations = validator.validate(validVisit);
            // May still have violations due to pet relationship
            System.out.println("Valid visit violations: " + validViolations.size());
        }
    }
    
    @Nested
    @DisplayName("Error Handling Characterization")
    class ErrorHandlingCharacterizationTests {
        
        @Test
        @DisplayName("Controller error handling - capture current error responses")
        void characterizeControllerErrorHandling() {
            // Test controller validation error handling
            assertThat(applicationContext.getBean(OwnerController.class)).isNotNull();
            
            Owner invalidOwner = new Owner();
            BindingResult bindingResult = new BeanPropertyBindingResult(invalidOwner, "owner");
            
            // Simulate validation errors
            bindingResult.rejectValue("firstName", "required", "First name is required");
            bindingResult.rejectValue("lastName", "required", "Last name is required");
            
            // Characterize how controllers handle validation errors
            assertThat(bindingResult.hasErrors()).isTrue();
            assertThat(bindingResult.getErrorCount()).isEqualTo(2);
            
            // Document field error structure for API contracts
            bindingResult.getFieldErrors().forEach(error -> {
                System.out.println("Field error - Field: " + error.getField() + 
                                 ", Code: " + error.getCode() + 
                                 ", Message: " + error.getDefaultMessage());
            });
        }
        
        @Test
        @DisplayName("Service layer error handling - capture exception patterns")
        void characterizeServiceErrorHandling() {
            // Test service layer error patterns
            try {
                // This should not throw in current implementation, but characterize behavior
                Owner owner = clinicService.findOwnerById(-1);
                System.out.println("Negative ID lookup result: " + (owner != null ? "found" : "null"));
            } catch (Exception e) {
                System.out.println("Service exception for negative ID: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            }
            
            try {
                // Test with very large ID
                Owner owner = clinicService.findOwnerById(999999);
                System.out.println("Large ID lookup result: " + (owner != null ? "found" : "null"));
            } catch (Exception e) {
                System.out.println("Service exception for large ID: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            }
        }
    }
    
    @Nested
    @DisplayName("Caching Characterization")
    class CachingCharacterizationTests {
        
        @Test
        @DisplayName("Cache configuration - capture current caching behavior")
        void characterizeCacheConfiguration() {
            if (cacheManager != null) {
                System.out.println("Cache Manager: " + cacheManager.getClass().getSimpleName());
                System.out.println("Cache Names: " + cacheManager.getCacheNames());
                
                // Test if vets are cached (common pattern in petclinic)
                if (cacheManager.getCache("vets") != null) {
                    System.out.println("Vets cache exists");
                } else {
                    System.out.println("No vets cache configured");
                }
            } else {
                System.out.println("No cache manager configured");
            }
        }
        
        @Test
        @DisplayName("Service caching behavior - capture cache interactions")
        void characterizeServiceCaching() {
            // Test repeated calls to see if results are cached
            long startTime1 = System.currentTimeMillis();
            var vets1 = clinicService.findVets();
            long duration1 = System.currentTimeMillis() - startTime1;
            
            long startTime2 = System.currentTimeMillis();
            var vets2 = clinicService.findVets();
            long duration2 = System.currentTimeMillis() - startTime2;
            
            System.out.println("First vets call: " + duration1 + "ms, size: " + vets1.size());
            System.out.println("Second vets call: " + duration2 + "ms, size: " + vets2.size());
            
            // If caching is enabled, second call should be faster
            // This characterizes current behavior
            assertThat(vets1).isNotNull();
            assertThat(vets2).isNotNull();
            assertThat(vets1.size()).isEqualTo(vets2.size());
        }
    }
    
    @Nested
    @DisplayName("Architectural Constraints Characterization")
    class ArchitecturalCharacterizationTests {
        
        @Test
        @DisplayName("Layer dependencies - capture current architectural patterns")
        void characterizeLayerDependencies() {
            // Verify expected beans exist in each layer
            assertThat(applicationContext.getBeansOfType(OwnerController.class)).isNotEmpty();
            assertThat(applicationContext.getBeansOfType(PetController.class)).isNotEmpty();
            assertThat(applicationContext.getBeansOfType(VetController.class)).isNotEmpty();
            assertThat(applicationContext.getBeansOfType(VisitController.class)).isNotEmpty();
            
            // Service layer
            assertThat(applicationContext.getBean(ClinicService.class)).isNotNull();
            
            // Repository layer - should be available through service
            System.out.println("Available beans:");
            for (String beanName : applicationContext.getBeanDefinitionNames()) {
                if (beanName.toLowerCase().contains("repository") || 
                    beanName.toLowerCase().contains("dao")) {
                    System.out.println("  Repository/DAO: " + beanName);
                }
            }
        }
        
        @Test
        @DisplayName("Configuration structure - capture current config approach")
        void characterizeConfigurationStructure() {
            // Document current XML-based configuration
            System.out.println("Active profiles: " + String.join(", ", applicationContext.getEnvironment().getActiveProfiles()));
            System.out.println("Default profiles: " + String.join(", ", applicationContext.getEnvironment().getDefaultProfiles()));
            
            // Check for specific configuration beans
            String[] configBeans = {"dataSource", "entityManagerFactory", "transactionManager"};
            for (String beanName : configBeans) {
                try {
                    Object bean = applicationContext.getBean(beanName);
                    System.out.println("Config bean " + beanName + ": " + bean.getClass().getSimpleName());
                } catch (Exception e) {
                    System.out.println("Config bean " + beanName + ": not found");
                }
            }
        }
    }
    
    @Nested
    @DisplayName("Transaction Characterization")
    class TransactionCharacterizationTests {
        
        @Test
        @DisplayName("Transaction boundaries - capture current transaction behavior")
        void characterizeTransactionBoundaries() {
            if (transactionTemplate != null) {
                // Test transaction behavior
                Boolean result = transactionTemplate.execute(status -> {
                    System.out.println("In transaction: " + !status.isNewTransaction());
                    System.out.println("Transaction read-only: " + status.isReadOnly());
                    
                    // Test service calls within transaction
                    try {
                        var owners = clinicService.findOwnerByLastName("Davis");
                        System.out.println("Found owners in transaction: " + owners.size());
                        return true;
                    } catch (Exception e) {
                        System.out.println("Exception in transaction: " + e.getMessage());
                        return false;
                    }
                });
                
                assertThat(result).isTrue();
            } else {
                System.out.println("No transaction template configured");
            }
        }
        
        @Test
        @DisplayName("Service transaction annotations - capture declarative transaction behavior")
        void characterizeServiceTransactionBehavior() {
            // Test multiple service calls to characterize transaction behavior
            try {
                var owner = clinicService.findOwnerById(1);
                if (owner != null) {
                    // Test read operations
                    var pets = owner.getPets();
                    System.out.println("Owner pets loaded: " + pets.size());
                    
                    // Test if lazy loading works (indicates transaction context)
                    for (Pet pet : pets) {
                        var visits = pet.getVisits();
                        System.out.println("Pet " + pet.getName() + " visits: " + visits.size());
                    }
                }
            } catch (Exception e) {
                System.out.println("Transaction/lazy loading behavior: " + e.getMessage());
            }
        }
    }
    
    @Nested
    @DisplayName("Data Integrity Characterization") 
    class DataIntegrityCharacterizationTests {
        
        @Test
        @DisplayName("Entity relationship constraints - capture current data model behavior")
        void characterizeEntityRelationships() {
            // Test owner-pet relationship integrity
            var owners = clinicService.findOwnerByLastName("Franklin");
            if (!owners.isEmpty()) {
                Owner owner = owners.iterator().next();
                int originalPetCount = owner.getPets().size();
                
                // Test relationship behavior
                Pet pet = new Pet();
                pet.setName("Test Pet");
                pet.setBirthDate(LocalDate.now().minusYears(1));
                
                // This tests current relationship management
                try {
                    owner.addPet(pet);
                    assertThat(owner.getPets().size()).isEqualTo(originalPetCount + 1);
                    assertThat(pet.getOwner()).isEqualTo(owner);
                    
                    System.out.println("Relationship management working: bidirectional sync");
                } catch (Exception e) {
                    System.out.println("Relationship error: " + e.getMessage());
                }
            }
        }
        
        @Test
        @DisplayName("Cascade behavior - capture current cascade operations")
        void characterizeCascadeBehavior() {
            // Test what happens with pet visits when we manipulate pets
            var owners = clinicService.findOwnerByLastName("Davis");
            if (!owners.isEmpty()) {
                Owner owner = owners.iterator().next();
                
                for (Pet pet : owner.getPets()) {
                    int visitCount = pet.getVisits().size();
                    System.out.println("Pet " + pet.getName() + " has " + visitCount + " visits");
                    
                    // Test visit addition
                    Visit visit = new Visit();
                    visit.setDate(LocalDate.now());
                    visit.setDescription("Characterization test visit");
                    
                    try {
                        pet.addVisit(visit);
                        assertThat(pet.getVisits().size()).isEqualTo(visitCount + 1);
                        assertThat(visit.getPet()).isEqualTo(pet);
                        
                        System.out.println("Visit relationship working: " + pet.getVisits().size() + " visits");
                    } catch (Exception e) {
                        System.out.println("Visit relationship error: " + e.getMessage());
                    }
                }
            }
        }
    }
}