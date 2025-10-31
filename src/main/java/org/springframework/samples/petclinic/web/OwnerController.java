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

import java.util.Collection;
import java.util.Map;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

/**
 * Spring MVC controller handling all owner-related web requests in the petclinic application.
 * Implements CRUD operations for pet owners with search capabilities and form validation.
 * 
 * <p>Supported operations:
 * - Owner registration (GET/POST /owners/new)
 * - Owner search by last name (GET /owners/find, GET /owners)
 * - Owner profile updates (GET/POST /owners/{id}/edit)
 * - Owner details display (GET /owners/{id})
 * 
 * <p>Security considerations:
 * - ID binding disabled via @InitBinder to prevent mass assignment attacks
 * - Path variable validation prevents unauthorized owner access
 * - Form validation prevents data corruption and XSS attacks
 * 
 * <p>Business logic:
 * - Search supports partial matching and empty queries (returns all)
 * - Single result searches auto-redirect to owner details
 * - Multiple results display selection page
 * - Form processing includes server-side validation with error handling
 * 
 * <p>Performance notes:
 * - Consider pagination for large owner datasets
 * - Search operations may benefit from database indexing on lastName
 * 
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Michael Isvy
 */
@Controller
public class OwnerController {

    private static final String VIEWS_OWNER_CREATE_OR_UPDATE_FORM = "owners/createOrUpdateOwnerForm";
    private final ClinicService clinicService;


    @Autowired
    public OwnerController(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    /**
     * Configures form binding security by disallowing 'id' field binding.
     * Prevents mass assignment attacks where malicious users could modify entity IDs.
     * 
     * @param dataBinder the WebDataBinder to configure
     */
    @InitBinder
    public void setAllowedFields(WebDataBinder dataBinder) {
        dataBinder.setDisallowedFields("id");
    }

    /**
     * Displays the owner registration form with an empty Owner object.
     * 
     * @param model Spring MVC model for template rendering
     * @return view name for owner creation/update form template
     */
    @GetMapping(value = "/owners/new")
    public String initCreationForm(Map<String, Object> model) {
        Owner owner = new Owner();
        model.put("owner", owner);
        return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
    }

    /**
     * Processes owner registration form submission with validation.
     * 
     * @param owner form-bound owner object with validation annotations applied
     * @param result binding and validation results
     * @return redirect to owner details on success, form view on validation errors
     * TODO: Add duplicate owner detection (same name/address combination)
     */
    @PostMapping(value = "/owners/new")
    public String processCreationForm(@Valid Owner owner, BindingResult result) {
        if (result.hasErrors()) {
            return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
        }

        this.clinicService.saveOwner(owner);
        return "redirect:/owners/" + owner.getId();
    }

    @GetMapping(value = "/owners/find")
    public String initFindForm(Map<String, Object> model) {
        model.put("owner", new Owner());
        return "owners/findOwners";
    }

    /**
     * Processes owner search requests with intelligent result handling.
     * Supports both targeted searches and "show all" functionality.
     * 
     * @param owner search criteria (typically just lastName)
     * @param result validation and error handling
     * @param model Spring MVC model for template rendering
     * @return appropriate view based on search results
     * TODO: Add pagination for large result sets to improve performance
     * TODO: Consider search result caching for frequently accessed data
     */
    @GetMapping(value = "/owners")
    public String processFindForm(Owner owner, BindingResult result, Map<String, Object> model) {

        // allow parameterless GET request for /owners to return all records
        if (owner.getLastName() == null) {
            owner.setLastName(""); // empty string signifies broadest possible search
        }

        // find owners by last name
        Collection<Owner> results = this.clinicService.findOwnerByLastName(owner.getLastName());
        if (results.isEmpty()) {
            // no owners found
            result.rejectValue("lastName", "notFound", "not found");
            return "owners/findOwners";
        } else if (results.size() == 1) {
            // 1 owner found - auto-redirect for user convenience
            owner = results.iterator().next();
            return "redirect:/owners/" + owner.getId();
        } else {
            // multiple owners found - show selection page
            model.put("selections", results);
            return "owners/ownersList";
        }
    }

    @GetMapping(value = "/owners/{ownerId}/edit")
    public String initUpdateOwnerForm(@PathVariable("ownerId") int ownerId, Model model) {
        Owner owner = this.clinicService.findOwnerById(ownerId);
        model.addAttribute(owner);
        return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
    }

    @PostMapping(value = "/owners/{ownerId}/edit")
    public String processUpdateOwnerForm(@Valid Owner owner, BindingResult result, @PathVariable("ownerId") int ownerId) {
        if (result.hasErrors()) {
            return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
        }

        owner.setId(ownerId);
        this.clinicService.saveOwner(owner);
        return "redirect:/owners/{ownerId}";
    }

    /**
     * Custom handler for displaying an owner.
     *
     * @param ownerId the ID of the owner to display
     * @return a ModelMap with the model attributes for the view
     */
    @GetMapping("/owners/{ownerId}")
    public ModelAndView showOwner(@PathVariable("ownerId") int ownerId) {
        ModelAndView mav = new ModelAndView("owners/ownerDetails");
        mav.addObject(this.clinicService.findOwnerById(ownerId));
        return mav;
    }

}
