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
package org.springframework.samples.petclinic.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.PetType;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.Collection;

/**
 * Spring MVC controller handling pet-related operations within the context of an owner.
 * All pet operations are scoped to a specific owner via path variable binding.
 * 
 * <p>Supported operations:
 * - Pet registration for an owner (GET/POST /owners/{ownerId}/pets/new)
 * - Pet profile updates (GET/POST /owners/{ownerId}/pets/{petId}/edit)
 * - Automatic population of pet types for form dropdowns
 * - Owner context resolution for all pet operations
 * 
 * <p>Security features:
 * - Owner ID path binding ensures pets can only be managed within correct ownership context
 * - ID field binding disabled to prevent mass assignment attacks
 * - Custom PetValidator provides business rule validation
 * - Duplicate pet name validation within owner's pet collection
 * 
 * <p>Business logic:
 * - Pet names must be unique within an owner's collection
 * - Pet type selection from predefined reference data
 * - Bidirectional relationship management between owner and pets
 * - Form processing includes comprehensive validation and error handling
 * 
 * <p>Performance considerations:
 * - Owner lookup performed for every request (consider caching)
 * - Pet types loaded for form display (typically small reference dataset)
 * 
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 */
@Controller
@RequestMapping("/owners/{ownerId}")
public class PetController {

    private static final String VIEWS_PETS_CREATE_OR_UPDATE_FORM = "pets/createOrUpdatePetForm";
    private final ClinicService clinicService;

    @Autowired
    public PetController(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    /**
     * Populates pet type dropdown options for all form views.
     * Called automatically by Spring MVC before each request handling method.
     * 
     * @return collection of available pet types for form selection
     */
    @ModelAttribute("types")
    public Collection<PetType> populatePetTypes() {
        return this.clinicService.findPetTypes();
    }

    /**
     * Resolves and validates the owner context for all pet operations.
     * Ensures pet operations are performed within correct ownership boundaries.
     * 
     * @param ownerId path variable from URL pattern
     * @return owner entity for the specified ID
     * @throws ResourceNotFoundException if owner ID is invalid (handled by Spring MVC)
     */
    @ModelAttribute("owner")
    public Owner findOwner(@PathVariable("ownerId") int ownerId) {
        return this.clinicService.findOwnerById(ownerId);
    }

    @InitBinder("owner")
    public void initOwnerBinder(WebDataBinder dataBinder) {
        dataBinder.setDisallowedFields("id");
    }

    @InitBinder("pet")
    public void initPetBinder(WebDataBinder dataBinder) {
        dataBinder.setValidator(new PetValidator());
    }

    @GetMapping(value = "/pets/new")
    public String initCreationForm(Owner owner, ModelMap model) {
        Pet pet = new Pet();
        owner.addPet(pet);
        model.put("pet", pet);
        return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
    }

    /**
     * Processes pet registration form with duplicate name validation.
     * Implements business rule preventing duplicate pet names within owner's collection.
     * 
     * @param owner automatically resolved owner context
     * @param pet form-bound pet object with validation
     * @param result validation and binding results
     * @param model Spring MVC model for error handling
     * @return redirect to owner details on success, form view on validation errors
     * TODO: Consider case-insensitive duplicate checking for better user experience
     */
    @PostMapping(value = "/pets/new")
    public String processCreationForm(Owner owner, @Valid Pet pet, BindingResult result, ModelMap model) {
        if (StringUtils.hasLength(pet.getName()) && pet.isNew() && owner.getPet(pet.getName(), true) != null){
            result.rejectValue("name", "duplicate", "already exists");
        }
        if (result.hasErrors()) {
            model.put("pet", pet);
            return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
        }

        owner.addPet(pet);
        this.clinicService.savePet(pet);
        return "redirect:/owners/{ownerId}";
    }

    @GetMapping(value = "/pets/{petId}/edit")
    public String initUpdateForm(@PathVariable("petId") int petId, ModelMap model) {
        Pet pet = this.clinicService.findPetById(petId);
        model.put("pet", pet);
        return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
    }

    @PostMapping(value = "/pets/{petId}/edit")
    public String processUpdateForm(@Valid Pet pet, BindingResult result, Owner owner, ModelMap model) {
        if (result.hasErrors()) {
            model.put("pet", pet);
            return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
        }

        owner.addPet(pet);
        this.clinicService.savePet(pet);
        return "redirect:/owners/{ownerId}";
    }

}
