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
package org.springframework.samples.petclinic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Spring Boot main class for the PetClinic application.
 * 
 * Migrated from Spring MVC + XML configuration to Spring Boot 3.x
 * with preserved behavior for characterization tests.
 * 
 * @SpringBootApplication provides:
 * - @EnableAutoConfiguration: Auto-configures Spring Boot components
 * - @ComponentScan: Scans for Spring components in this package and sub-packages  
 * - @Configuration: Allows additional bean definitions
 * 
 * @EnableCaching: Preserves caching behavior from XML configuration
 * @EnableJpaRepositories: Enables only Spring Data JPA repositories 
 */
@SpringBootApplication
@EnableCaching  
@EnableJpaRepositories(basePackages = "org.springframework.samples.petclinic.repository.springdatajpa")
@ComponentScan(
    basePackages = "org.springframework.samples.petclinic",
    excludeFilters = {
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = "org\\.springframework\\.samples\\.petclinic\\.repository\\.(jdbc|jpa)\\..*")
    }
)
public class PetClinicApplication {

    public static void main(String[] args) {
        SpringApplication.run(PetClinicApplication.class, args);
    }

}