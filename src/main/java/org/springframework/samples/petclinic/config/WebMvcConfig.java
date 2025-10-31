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
package org.springframework.samples.petclinic.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.samples.petclinic.web.PetTypeFormatter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC Configuration for PetClinic Spring Boot Application
 * 
 * Migrated from mvc-core-config.xml - preserves original behavior:
 * - Root path "/" maps to "welcome" view
 * - Static resource handlers for /resources/** and /webjars/**
 * - Custom conversion service with PetTypeFormatter
 * - Default servlet handler for static content
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private ClinicService clinicService;

    /**
     * Configure view controllers that map URLs directly to view names
     * without requiring controller logic.
     * 
     * Maps "/" to "welcome" view (corresponds to WEB-INF/jsp/welcome.jsp)
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("welcome");
    }

    /**
     * Configure static resource handlers.
     * 
     * Preserves original behavior from mvc-core-config.xml:
     * - /resources/** maps to /resources/ directory (CSS, JS, images)
     * - /webjars/** maps to WebJars classpath location (Bootstrap, jQuery, etc.)
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/resources/**")
                .addResourceLocations("/resources/");
                
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    /**
     * Add custom formatters to the Spring MVC conversion service
     * Migrated from XML bean configuration - avoids circular dependency issues
     */
    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addFormatter(new PetTypeFormatter(clinicService));
    }
}