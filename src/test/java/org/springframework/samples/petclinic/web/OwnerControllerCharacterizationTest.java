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
package org.springframework.samples.petclinic.web;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.petclinic.model.*;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive characterization tests for {@link OwnerController} to preserve behavior
 * during Spring Boot 3 + Next.js migration.
 * 
 * @author Migration Team
 */
@SpringJUnitWebConfig(locations = {"classpath:spring/mvc-test-config.xml", "classpath:spring/mvc-core-config.xml"})
class OwnerControllerCharacterizationTest {

    @Autowired private OwnerController ownerController;
    @Autowired private ClinicService clinicService;
    
    private MockMvc mockMvc;
    private Owner sampleOwner;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(ownerController).build();
        
        sampleOwner = new Owner();
        sampleOwner.setId(1);
        sampleOwner.setFirstName("George");
        sampleOwner.setLastName("Franklin");
        sampleOwner.setAddress("110 W. Liberty St.");
        sampleOwner.setCity("Madison");
        sampleOwner.setTelephone("6085551023");
    }

    @Test
    @DisplayName("GET /owners/find - should display owner search form")
    void initFindForm_shouldDisplaySearchForm() throws Exception {
        mockMvc.perform(get("/owners/find"))
                .andExpect(status().isOk())
                .andExpect(view().name("owners/findOwners"))
                .andExpect(model().attributeExists("owner"));
    }

    @Test
    @DisplayName("GET /owners?lastName=Franklin - multiple results should show list")
    void processFindForm_shouldDisplayOwnersList_whenMultipleMatches() throws Exception {
        // Given
        Owner owner2 = new Owner();
        owner2.setId(2);
        owner2.setFirstName("Betty");
        owner2.setLastName("Franklin");
        
        given(clinicService.findOwnerByLastName("Franklin"))
                .willReturn(Lists.newArrayList(sampleOwner, owner2));

        // When & Then
        mockMvc.perform(get("/owners").param("lastName", "Franklin"))
                .andExpect(status().isOk())
                .andExpect(view().name("owners/ownersList"))
                .andExpect(model().attributeExists("selections"));
    }

    @Test
    @DisplayName("GET /owners?lastName=Franklin - single result should redirect to details")
    void processFindForm_shouldRedirectToOwnerDetails_whenSingleMatch() throws Exception {
        // Given
        given(clinicService.findOwnerByLastName("Franklin"))
                .willReturn(Lists.newArrayList(sampleOwner));

        // When & Then
        mockMvc.perform(get("/owners").param("lastName", "Franklin"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/owners/1"));
    }

    @Test
    @DisplayName("GET /owners/{id} - should display owner details")
    void showOwner_shouldDisplayOwnerDetails() throws Exception {
        // Given
        given(clinicService.findOwnerById(1)).willReturn(sampleOwner);

        // When & Then
        mockMvc.perform(get("/owners/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("owners/ownerDetails"))
                .andExpect(model().attributeExists("owner"))
                .andExpect(model().attribute("owner", hasProperty("firstName", is("George"))));
    }

    @Test
    @DisplayName("GET /owners/new - should display creation form")
    void initCreationForm_shouldDisplayNewOwnerForm() throws Exception {
        mockMvc.perform(get("/owners/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("owners/createOrUpdateOwnerForm"))
                .andExpect(model().attributeExists("owner"));
    }

    @Test
    @DisplayName("POST /owners/new - valid data should redirect")
    void processCreationForm_shouldCreateOwnerAndRedirect_whenValidData() throws Exception {
        mockMvc.perform(post("/owners/new")
                        .param("firstName", "Joe")
                        .param("lastName", "Bloggs")
                        .param("address", "123 Caramel Street")
                        .param("city", "London")
                        .param("telephone", "01316761638"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("POST /owners/new - invalid data should show validation errors")
    void processCreationForm_shouldReturnFormWithErrors_whenInvalidData() throws Exception {
        mockMvc.perform(post("/owners/new")
                        .param("firstName", "Joe")
                        .param("lastName", "Bloggs"))
                .andExpect(status().isOk())
                .andExpect(view().name("owners/createOrUpdateOwnerForm"))
                .andExpect(model().attributeHasFieldErrors("owner", "address", "city", "telephone"));
    }
}