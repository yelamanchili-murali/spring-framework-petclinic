# Spring Boot Migration Plan

**Migration Target:** Spring Framework Petclinic → Spring Boot 3.x + Java 17/21  
**Migration Strategy:** Preserve behavior locked by characterization tests  
**Date:** October 31, 2025

---

## 1. Build & Packaging Changes

### 1.1 Parent POM Migration

**Current State:** Standalone Maven project with direct Spring Framework dependencies  
**Target State:** Spring Boot parent with starter dependencies

```xml
<!-- Replace existing parent section with -->
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.5.5</version>
    <relativePath/>
</parent>

<groupId>org.springframework.samples</groupId>
<artifactId>spring-framework-petclinic</artifactId>
<version>6.2.8</version>
<name>Spring Boot Petclinic</name>
<packaging>jar</packaging>
<description>A Spring Boot application based on Spring MVC, Spring Data JPA, Hibernate and JSP</description>
```

### 1.2 Dependency Consolidation

**Replace individual Spring dependencies with starters:**

```xml
<dependencies>
    <!-- Core Spring Boot starters -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-cache</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
    
    <!-- JSP Support (Phase 1 - retain existing views) -->
    <dependency>
        <groupId>org.apache.tomcat.embed</groupId>
        <artifactId>tomcat-embed-jasper</artifactId>
        <scope>provided</scope>
    </dependency>
    <dependency>
        <groupId>jakarta.servlet.jsp.jstl</groupId>
        <artifactId>jakarta.servlet.jsp.jstl-api</artifactId>
    </dependency>
    <dependency>
        <groupId>org.glassfish.web</groupId>
        <artifactId>jakarta.servlet.jsp.jstl</artifactId>
    </dependency>
    
    <!-- Database drivers (preserve existing profiles) -->
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>runtime</scope>
    </dependency>
    
    <!-- Caching -->
    <dependency>
        <groupId>com.github.ben-manes.caffeine</groupId>
        <artifactId>caffeine</artifactId>
    </dependency>
    
    <!-- WebJars (preserve existing frontend assets) -->
    <dependency>
        <groupId>org.webjars</groupId>
        <artifactId>bootstrap</artifactId>
        <version>5.3.8</version>
    </dependency>
    
    <!-- Test dependencies -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    
    <!-- JAXB support (for XML marshalling compatibility) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web-services</artifactId>
    </dependency>
</dependencies>
```

### 1.3 Plugin Configuration

```xml
<build>
    <plugins>
        <!-- Spring Boot Maven Plugin -->
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
            <configuration>
                <classifier>exec</classifier>
            </configuration>
        </plugin>
        
        <!-- Compiler configuration (preserve Java 17 requirement) -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <configuration>
                <parameters>true</parameters>
                <source>17</source>
                <target>17</target>
            </configuration>
        </plugin>
        
        <!-- WAR plugin (for optional WAR packaging) -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-war-plugin</artifactId>
            <configuration>
                <failOnMissingWebXml>false</failOnMissingWebXml>
            </configuration>
        </plugin>
    </plugins>
</build>
```

### 1.4 Profile Migration Strategy

```xml
<!-- Database profiles (preserve existing multi-DB support) -->
<profiles>
    <profile>
        <id>h2</id>
        <activation>
            <activeByDefault>true</activeByDefault>
        </activation>
        <dependencies>
            <dependency>
                <groupId>com.h2database</groupId>
                <artifactId>h2</artifactId>
                <scope>runtime</scope>
            </dependency>
        </dependencies>
    </profile>
    
    <profile>
        <id>mysql</id>
        <dependencies>
            <dependency>
                <groupId>mysql</groupId>
                <artifactId>mysql-connector-java</artifactId>
                <scope>runtime</scope>
            </dependency>
        </dependencies>
    </profile>
    
    <profile>
        <id>postgresql</id>
        <dependencies>
            <dependency>
                <groupId>org.postgresql</groupId>
                <artifactId>postgresql</artifactId>
                <scope>runtime</scope>
            </dependency>
        </dependencies>
    </profile>
    
    <!-- Optional: WAR packaging profile -->
    <profile>
        <id>war</id>
        <properties>
            <packaging.type>war</packaging.type>
        </properties>
    </profile>
</profiles>
```

---

## 2. Main Application and Bootstrapping

### 2.1 Spring Boot Application Entry Point

**Create:** `src/main/java/org/springframework/samples/petclinic/PetclinicApplication.java`

```java
package org.springframework.samples.petclinic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Spring Boot main application class for the Pet Clinic application.
 * 
 * Extends SpringBootServletInitializer to support both JAR and WAR deployment.
 * Enables caching and transaction management to preserve existing behavior.
 */
@SpringBootApplication
@EnableCaching
@EnableTransactionManagement
public class PetclinicApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(PetclinicApplication.class, args);
    }
}
```

### 2.2 Legacy Configuration Bridge

**Create:** `src/main/java/org/springframework/samples/petclinic/config/PetclinicConfiguration.java`

```java
package org.springframework.samples.petclinic.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.filter.CharacterEncodingFilter;

/**
 * Configuration class to preserve specific behavior from legacy XML configuration.
 * 
 * Only includes configuration that cannot be auto-configured by Spring Boot.
 */
@Configuration
@EnableConfigurationProperties
public class PetclinicConfiguration {

    /**
     * Character encoding filter to preserve UTF-8 support for international characters.
     * Migrated from PetclinicInitializer.getServletFilters()
     */
    @Bean
    public CharacterEncodingFilter characterEncodingFilter() {
        CharacterEncodingFilter filter = new CharacterEncodingFilter("UTF-8", true);
        return filter;
    }

    /**
     * Caffeine cache manager to preserve vets caching behavior.
     * Migrated from tools-config.xml
     */
    @Bean
    @Profile("!test")
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("vets");
        return cacheManager;
    }
}
```

### 2.3 JSP View Configuration

**Create:** `src/main/java/org/springframework/samples/petclinic/config/WebMvcConfiguration.java`

```java
package org.springframework.samples.petclinic.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

/**
 * Web MVC configuration to preserve JSP view resolution during Phase 1.
 * 
 * This configuration maintains compatibility with existing JSP views and
 * can be removed in Phase 2 when migrating to Thymeleaf or REST APIs.
 */
@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        InternalResourceViewResolver resolver = new InternalResourceViewResolver();
        resolver.setPrefix("/WEB-INF/jsp/");
        resolver.setSuffix(".jsp");
        resolver.setViewClass(JstlView.class);
        registry.viewResolver(resolver);
    }
}
```

---

## 3. Configuration Migration (properties → application.yml)

### 3.1 Application Configuration

**Create:** `src/main/resources/application.yml`

```yaml
# Spring Boot Petclinic Configuration
# Migrated from XML configuration files

spring:
  application:
    name: petclinic
    
  profiles:
    active: h2,jpa
    
  # JSP Configuration (Phase 1 - preserve existing views)
  mvc:
    view:
      prefix: /WEB-INF/jsp/
      suffix: .jsp
      
  # JPA Configuration (migrated from business-config.xml)
  jpa:
    database: H2
    show-sql: false
    hibernate:
      ddl-auto: none
    properties:
      hibernate:
        cache:
          use_second_level_cache: true
          region:
            factory_class: org.hibernate.cache.jcache.JCacheRegionFactory
        javax:
          cache:
            provider: com.github.benmanes.caffeine.jcache.spi.CaffeineCachingProvider
            
  # Database Configuration (default H2)
  datasource:
    url: jdbc:h2:mem:petclinic
    username: sa
    password: 
    driver-class-name: org.h2.Driver
    
  # SQL Initialization
  sql:
    init:
      mode: always
      schema-locations: classpath:db/h2/schema.sql
      data-locations: classpath:db/h2/data.sql
      
  # Cache Configuration (migrated from tools-config.xml)
  cache:
    type: caffeine
    caffeine:
      spec: maximumSize=10000,expireAfterAccess=600s

# Server Configuration
server:
  port: 8080
  servlet:
    context-path: /
    encoding:
      charset: UTF-8
      enabled: true
      force: true

# Management/Actuator Configuration
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: when-authorized
  info:
    env:
      enabled: true

# Application Information
info:
  app:
    name: Spring Boot Petclinic
    description: Pet Clinic application migrated to Spring Boot 3
    version: 6.2.8
    java:
      version: 17

# Logging Configuration (preserve existing behavior)
logging:
  level:
    org.springframework.samples.petclinic: INFO
    org.springframework.web: INFO
    org.hibernate.SQL: WARN
```

### 3.2 Profile-Specific Configuration

**Create:** `src/main/resources/application-mysql.yml`

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/petclinic?useUnicode=true
    username: petclinic
    password: petclinic
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    database: MYSQL
  sql:
    init:
      schema-locations: classpath:db/mysql/schema.sql
      data-locations: classpath:db/mysql/data.sql
```

**Create:** `src/main/resources/application-postgresql.yml`

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/petclinic
    username: postgres
    password: petclinic
    driver-class-name: org.postgresql.Driver
  jpa:
    database: POSTGRESQL
  sql:
    init:
      schema-locations: classpath:db/postgresql/schema.sql
      data-locations: classpath:db/postgresql/data.sql
```

**Create:** `src/main/resources/application-test.yml`

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    username: sa
    password: 
  jpa:
    show-sql: false
  cache:
    type: none
  sql:
    init:
      mode: always
      
logging:
  level:
    org.springframework.samples.petclinic: DEBUG
    org.springframework.test: INFO
```

---

## 4. Observability (Actuator Configuration)

### 4.1 Health Check Configuration

**Create:** `src/main/java/org/springframework/samples/petclinic/config/ActuatorConfiguration.java`

```java
package org.springframework.samples.petclinic.config;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.petclinic.service.ClinicService;

/**
 * Custom health indicators for Pet Clinic application.
 */
@Component
public class PetclinicHealthIndicator implements HealthIndicator {

    @Autowired
    private ClinicService clinicService;

    @Override
    public Health health() {
        try {
            // Verify database connectivity by checking pet types
            int petTypeCount = clinicService.findPetTypes().size();
            return Health.up()
                .withDetail("petTypes", petTypeCount)
                .withDetail("status", "Pet Clinic is operational")
                .build();
        } catch (Exception ex) {
            return Health.down()
                .withDetail("error", ex.getMessage())
                .build();
        }
    }
}
```

### 4.2 Application Information

**Update:** `src/main/resources/application.yml` (add to info section)

```yaml
info:
  app:
    name: Spring Boot Petclinic
    description: Pet Clinic application migrated to Spring Boot 3
    version: 6.2.8
    java:
      version: 17
  build:
    artifact: ${project.artifactId:}
    name: ${project.name:}
    time: ${maven.build.timestamp:}
    version: ${project.version:}
```

### 4.3 Metrics Configuration

**Create:** `src/main/java/org/springframework/samples/petclinic/config/MetricsConfiguration.java`

```java
package org.springframework.samples.petclinic.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Custom metrics configuration for monitoring Pet Clinic operations.
 */
@Configuration
public class MetricsConfiguration {

    @Bean
    public Counter ownerRegistrationCounter(MeterRegistry meterRegistry) {
        return Counter.builder("petclinic.owners.registered")
                .description("Number of owners registered")
                .register(meterRegistry);
    }

    @Bean
    public Counter petRegistrationCounter(MeterRegistry meterRegistry) {
        return Counter.builder("petclinic.pets.registered")
                .description("Number of pets registered")
                .register(meterRegistry);
    }
}
```

---

## 5. Test Migration

### 5.1 Spring Boot Test Configuration

**Update existing test base classes to use Spring Boot:**

```java
// Update ClinicServiceImplCharacterizationTest
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.yml")
@ActiveProfiles("test")
class ClinicServiceImplCharacterizationTest {
    // Existing test methods remain unchanged
}

// Update OwnerControllerCharacterizationTest
@WebMvcTest(OwnerController.class)
@Import(PetclinicConfiguration.class)
@ActiveProfiles("test")
class OwnerControllerCharacterizationTest {
    // Existing MockMvc tests remain unchanged
}

// Update RepositoryCharacterizationTest
@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.yml")
@ActiveProfiles("test")
class RepositoryCharacterizationTest {
    // Existing repository tests remain unchanged
}
```

### 5.2 Application Startup Test

**Create:** `src/test/java/org/springframework/samples/petclinic/PetclinicApplicationTests.java`

```java
package org.springframework.samples.petclinic;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class PetclinicApplicationTests {

    @Test
    void contextLoads() {
        // Verify Spring Boot application starts successfully
    }

    @Test
    void verifyMainMethodExists() {
        // Smoke test to ensure main method exists
        assertThat(PetclinicApplication.class.getMethod("main", String[].class))
            .isNotNull();
    }
}
```

### 5.3 Actuator Integration Tests

**Create:** `src/test/java/org/springframework/samples/petclinic/ActuatorIntegrationTest.java`

```java
package org.springframework.samples.petclinic;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ActuatorIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void healthEndpointShouldBeAccessible() {
        ResponseEntity<String> response = restTemplate.getForEntity("/actuator/health", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("UP");
    }

    @Test
    void infoEndpointShouldReturnApplicationInfo() {
        ResponseEntity<String> response = restTemplate.getForEntity("/actuator/info", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Pet Clinic");
    }
}
```

---

## 6. Risk Register & Rollback Plan

### 6.1 Migration Risks Assessment

| Risk Category | Risk Description | Probability | Impact | Mitigation Strategy |
|--------------|------------------|-------------|---------|-------------------|
| **HIGH** | JSP view rendering incompatibility with embedded Tomcat | MEDIUM | HIGH | Extensive testing with jsp-api, maintain WAR option |
| **HIGH** | Characterization tests failing due to context changes | LOW | CRITICAL | Preserve existing test annotations, gradual migration |
| **MEDIUM** | Database initialization timing issues | MEDIUM | MEDIUM | Use Spring Boot SQL initialization, test with all DB profiles |
| **MEDIUM** | Cache configuration differences | LOW | MEDIUM | Explicit Caffeine configuration, test vets caching |
| **MEDIUM** | Static resource serving changes | LOW | MEDIUM | Verify WebJars integration, test CSS/JS loading |
| **LOW** | Actuator endpoints exposing sensitive data | LOW | HIGH | Secure actuator endpoints, limit exposure in production |

### 6.2 Jakarta Namespace Migration Impact

**Already Addressed:** The current application uses Jakarta EE namespaces:
- ✅ `jakarta.servlet.*` (already present)
- ✅ `jakarta.persistence.*` (already present)  
- ✅ `jakarta.validation.*` (already present)
- ✅ `jakarta.servlet.jsp.jstl.*` (already present)

**No javax → jakarta migration required** - application is already Spring Boot 3 compatible.

### 6.3 Validation Steps

#### Phase 1: Build Validation
```bash
# 1. Update pom.xml with Spring Boot parent
# 2. Build verification
mvn clean compile
mvn dependency:tree | grep -E "(javax|spring-framework)"

# 3. Test execution
mvn test -Dtest="*CharacterizationTest"
```

#### Phase 2: Application Startup Validation
```bash
# 1. Start application
mvn spring-boot:run

# 2. Verify endpoints
curl http://localhost:8080/
curl http://localhost:8080/actuator/health
curl http://localhost:8080/owners

# 3. Database profile testing
mvn spring-boot:run -Dspring.profiles.active=h2
mvn spring-boot:run -Dspring.profiles.active=mysql
```

#### Phase 3: Behavioral Validation
```bash
# 1. Run full characterization test suite
mvn test -Dtest="*CharacterizationTest" -Dmaven.test.failure.ignore=true

# 2. Compare results with pre-migration baseline
# 3. Verify specific functionality:
#    - Owner search and registration
#    - Pet registration and management  
#    - Visit recording
#    - Vet listings (HTML and XML)
#    - Caching behavior
```

### 6.4 Rollback Plan

#### Immediate Rollback (Git-based)
```bash
# Revert to pre-migration state
git checkout pre-spring-boot-migration
git checkout -b rollback-$(date +%Y%m%d)

# Verify original functionality
mvn clean test
mvn jetty:run
```

#### Partial Rollback Strategy
1. **Keep Spring Boot parent** but revert to XML configuration
2. **Keep JAR packaging** but restore original dependency versions
3. **Keep application.yml** but restore XML context loading
4. **Gradual feature rollback** based on specific failure points

#### Production Rollback Checklist
- [ ] Database schema compatibility verified
- [ ] Static resource serving functional  
- [ ] All characterization tests passing
- [ ] Performance benchmarks within acceptable range
- [ ] Monitoring and logging operational
- [ ] Cache behavior preserved
- [ ] Multi-database profile support functional

### 6.5 Success Criteria

#### Technical Validation
- [ ] All characterization tests pass with identical behavior
- [ ] Application starts in < 30 seconds
- [ ] Memory footprint ≤ 1.2x original WAR deployment
- [ ] Response times within 10% of original performance
- [ ] All database profiles (H2, MySQL, PostgreSQL) functional

#### Functional Validation  
- [ ] Owner registration and search workflow
- [ ] Pet registration and management workflow
- [ ] Veterinary visit recording workflow
- [ ] Vet listing (HTML/XML) workflow
- [ ] Static resource loading (CSS, JS, images)
- [ ] Internationalization (EN, DE, ES messages)

#### Operational Validation
- [ ] Actuator endpoints accessible and secured
- [ ] Application logs provide adequate debugging information  
- [ ] Cache performance matches original implementation
- [ ] Graceful shutdown handling
- [ ] JMX monitoring capabilities preserved

---

## Migration Execution Timeline

### Week 1: Build & Dependencies
- [ ] Update pom.xml to Spring Boot parent
- [ ] Replace dependencies with starters  
- [ ] Validate build and dependency resolution
- [ ] Test with all database profiles

### Week 2: Application Bootstrap
- [ ] Create @SpringBootApplication main class
- [ ] Migrate XML configuration to @Configuration classes
- [ ] Implement JSP view resolver configuration
- [ ] Test application startup

### Week 3: Configuration & Testing
- [ ] Create application.yml configuration
- [ ] Migrate profile-specific properties
- [ ] Update test classes for Spring Boot
- [ ] Run characterization test suite

### Week 4: Observability & Validation
- [ ] Configure Actuator endpoints
- [ ] Implement custom health indicators
- [ ] Performance testing and validation
- [ ] Documentation and rollback preparation

**Total Estimated Duration:** 4 weeks  
**Risk Window:** Week 2-3 (application bootstrap and configuration migration)  
**Validation Period:** Week 4 + 1 week production observation

This migration plan provides a comprehensive, step-by-step approach to migrating the Spring Framework Pet Clinic to Spring Boot 3 while preserving all existing functionality and maintaining the ability to rollback if issues arise.