# Repository Recon & Modernisation Brief

**Date:** October 31, 2025  
**Project:** Spring Framework Petclinic Legacy Application  
**Target:** Spring Boot 3 + Java 17 Backend → Next.js Frontend Migration

## 1. Inventory

### Project Structure
- **Module**: Single Maven WAR project (`spring-framework-petclinic`)
- **Packaging**: WAR deployment for Jetty 11+ / Tomcat 10+
- **Java Version**: Java 17 (already modern baseline)
- **Build Tool**: Maven 3.8.4+ with reproducible builds
- **Container**: Jetty 11.0-JDK17 (Docker via Jib plugin)

### Framework Versions (Current State)
```
Core Frameworks:
├── Spring Framework 6.2.11 (Jakarta EE namespace)
├── Spring Data 2025.0.3 BOM
├── Hibernate 6.6.4.Final + Hibernate Validator 8.0.2.Final
├── Jakarta Servlet API 6.1.0 (provided scope)
└── JSTL API 3.0.2 + Implementation 3.0.1

Database Support:
├── H2 2.3.232 (default, in-memory)
├── MySQL 8.1.0 (optional profile)
└── PostgreSQL 42.7.8 (optional profile)

View Technology:
├── JSP + JSTL (legacy, replacement target)
├── Bootstrap 5.3.8 (WebJars)
└── SCSS compilation support

Testing Stack:
├── JUnit Jupiter 5.13.2
├── Mockito 5.17.0
├── AssertJ 3.27.3
└── Spring Test Context
```

### Persistence Layer Profiles
- **JPA Profile**: Hibernate EntityManager + JPA annotations (default)
- **JDBC Profile**: Spring JdbcTemplate + NamedParameterJdbcTemplate
- **Spring Data JPA Profile**: Repository abstraction layer
- **Multi-DB Support**: H2 (default) | HSQLDB | MySQL | PostgreSQL

## 2. Risk Heatmap

### 🔴 **HIGH RISK** - Legacy Namespace Usage
```java
Location: src/main/java/org/springframework/samples/petclinic/repository/jdbc/
Files: JdbcPetRepositoryImpl.java, JdbcOwnerRepositoryImpl.java
Issue: javax.sql.DataSource (line 33)
Impact: Spring Boot 3 migration blocker
```

### 🟠 **MEDIUM RISK** - XML Configuration Hotspots
```xml
Critical XML Files:
├── src/main/resources/spring/business-config.xml (67 lines)
├── src/main/resources/spring/mvc-view-config.xml (41 lines)
├── src/main/resources/spring/datasource-config.xml
├── src/main/resources/spring/mvc-core-config.xml
└── src/main/resources/spring/tools-config.xml

Migration Target: @Configuration + @EnableAutoConfiguration
```

### 🟡 **MEDIUM RISK** - JSP View Coupling
```
JSP Dependencies (18 files):
├── /WEB-INF/jsp/owners/*.jsp (4 files) → React components
├── /WEB-INF/jsp/pets/*.jsp (2 files) → React components  
├── /WEB-INF/jsp/vets/*.jsp (1 file) → React components
├── /WEB-INF/tags/*.tag (custom tags) → React utilities
└── Complex EL expressions → TypeScript/REST APIs

Frontend Migration: JSP → Next.js + TypeScript
```

### 🟢 **LOW RISK** - Modern Elements Already Present
- ✅ Jakarta EE namespaces (servlet, persistence, validation)
- ✅ Java 17 compatibility
- ✅ Spring Framework 6.x (Spring Boot 3 compatible)
- ✅ Hibernate 6.x ORM layer
- ✅ Modern testing stack (JUnit 5, Mockito 5)

### 🔍 **CVE SCAN REQUIRED**
```
Dependency Security Assessment Needed:
- Webjars versions (Bootstrap 5.3.8, FontAwesome 4.7.0)
- Tomcat JDBC pool 10.1.35
- Database drivers (MySQL 8.1.0, PostgreSQL 42.7.8)
- Jackson 2.19.2
```

## 3. Dependency Diagram (Mermaid)

```mermaid
classDiagram
    %% Web Layer
    class OwnerController {
        +initCreationForm()
        +processCreationForm()
        +initFindForm()
        +processFindForm()
        +showOwner()
    }
    
    class PetController {
        +initCreationForm()
        +processCreationForm()
        +initUpdateForm()
        +processUpdateForm()
    }
    
    class VetController {
        +showVetList()
        +showResourcesVetList()
    }
    
    class VisitController {
        +initNewVisitForm()
        +processNewVisitForm()
    }

    %% Service Layer
    class ClinicServiceImpl {
        +findOwnerById()
        +findOwnerByLastName()
        +saveOwner()
        +findPetById()
        +savePet()
        +findVets()
        +saveVisit()
        +findVisitsByPetId()
    }
    
    class ClinicService {
        <<interface>>
    }

    %% Repository Layer
    class OwnerRepository {
        <<interface>>
        +findByLastName()
        +findById()
        +save()
    }
    
    class PetRepository {
        <<interface>>
        +findPetTypes()
        +findById()
        +save()
    }
    
    class VetRepository {
        <<interface>>
        +findAll()
    }
    
    class VisitRepository {
        <<interface>>
        +save()
        +findByPetId()
    }

    %% Model Layer
    class Owner {
        +firstName: String
        +lastName: String
        +address: String
        +city: String
        +telephone: String
        +pets: Set~Pet~
    }
    
    class Pet {
        +name: String
        +birthDate: LocalDate
        +type: PetType
        +owner: Owner
        +visits: Set~Visit~
    }
    
    class Visit {
        +date: LocalDate
        +description: String
        +pet: Pet
    }
    
    class Vet {
        +specialties: Set~Specialty~
    }

    %% Dependencies
    OwnerController --> ClinicServiceImpl
    PetController --> ClinicServiceImpl
    VetController --> ClinicServiceImpl
    VisitController --> ClinicServiceImpl
    
    ClinicServiceImpl --|> ClinicService
    ClinicServiceImpl --> OwnerRepository
    ClinicServiceImpl --> PetRepository
    ClinicServiceImpl --> VetRepository
    ClinicServiceImpl --> VisitRepository
    
    OwnerRepository --> Owner
    PetRepository --> Pet
    VetRepository --> Vet
    VisitRepository --> Visit
    
    Owner ||--o{ Pet
    Pet ||--o{ Visit
    Vet }o--o{ Specialty
```

## 4. Behaviour Summary

### Core Business Flows

#### **Owner Management Flow**
```
Controllers: OwnerController
Services: ClinicServiceImpl
Repositories: OwnerRepository
Views: owners/findOwners.jsp, owners/ownersList.jsp, owners/ownerDetails.jsp, owners/createOrUpdateOwnerForm.jsp

Flow: Search → List → Details → Edit/Add Pet
- GET /owners/find → Search form
- GET /owners?lastName=Smith → Search results  
- GET /owners/{id} → Owner details + pets + visits
- GET /owners/{id}/edit → Update owner form
```

#### **Pet Registration & Management Flow**
```
Controllers: PetController  
Services: ClinicServiceImpl
Repositories: PetRepository, OwnerRepository
Views: pets/createOrUpdatePetForm.jsp

Flow: Owner Details → Add Pet → Pet Form → Save
- GET /owners/{ownerId}/pets/new → New pet form
- POST /owners/{ownerId}/pets/new → Create pet
- GET /owners/{ownerId}/pets/{petId}/edit → Edit pet
```

#### **Veterinary Visit Flow**  
```
Controllers: VisitController
Services: ClinicServiceImpl  
Repositories: VisitRepository, PetRepository
Views: pets/createOrUpdateVisitForm.jsp

Flow: Pet Details → Add Visit → Visit Form → Save
- GET /owners/{ownerId}/pets/{petId}/visits/new → New visit form
- POST /owners/{ownerId}/pets/{petId}/visits/new → Create visit
```

#### **Veterinarian Listing Flow**
```
Controllers: VetController
Services: ClinicServiceImpl (with @Cacheable)
Repositories: VetRepository  
Views: vets/vetList.jsp + XML marshalling

Flow: Navigation → Vet List (HTML/XML)
- GET /vets → HTML veterinarian list
- GET /vets.xml → XML veterinarian data (JAXB marshalling)
```

### View Dependencies & JSP Coupling
- **Layout System**: `/WEB-INF/tags/` custom tag library (footer.tag, htmlHeader.tag, etc.)
- **Form Processing**: Heavy Spring Form binding + validation integration
- **Content Negotiation**: JSP vs XML marshalling based on Accept headers
- **Static Resources**: WebJars integration for Bootstrap/FontAwesome
- **Internationalization**: Multiple message bundles (EN/DE/ES)

## 5. Recommended Sequencing

| Phase | Goal | Inputs | Outputs | Risk Level |
|-------|------|--------|---------|------------|
| **Phase 1A** | Dependency Cleanup | Current pom.xml, javax usage scan | Updated dependencies, CVE-clean pom.xml | 🟢 **LOW** |
| **Phase 1B** | Spring Boot Migration | XML configs, PetclinicInitializer | @SpringBootApplication, application.yml | 🟡 **MEDIUM** |
| **Phase 2A** | Backend API Layer | Controllers, Service layer | REST APIs (/api/v1/owners, etc.) | 🟡 **MEDIUM** |
| **Phase 2B** | WAR → JAR Migration | Current WAR packaging | Executable JAR with embedded Tomcat | 🟢 **LOW** |
| **Phase 3A** | Next.js Setup | JSP view analysis, UI requirements | Next.js app structure, TypeScript models | 🟠 **HIGH** |
| **Phase 3B** | API Integration | REST endpoints, JSP form logic | React components, API client layer | 🟠 **HIGH** |
| **Phase 4** | JSP Retirement | Remaining JSP files, tag libraries | Full Next.js frontend, API-only backend | 🔴 **VERY HIGH** |
| **Phase 5** | Production Hardening | Test suites, deployment scripts | CI/CD pipelines, monitoring, security | 🟡 **MEDIUM** |

### Migration Considerations
- **XML → JavaConfig**: Replace 5 XML files with @Configuration classes
- **JSP → React**: 18 JSP files + custom tags → TypeScript components  
- **WAR → JAR**: Servlet container embedding vs external deployment
- **Form Binding**: Spring MVC @ModelAttribute → REST JSON payloads
- **Session Management**: Stateful JSP sessions → Stateless JWT/session tokens
- **Static Resources**: WebJars → npm/webpack asset pipeline
- **Database Migrations**: Flyway/Liquibase introduction for production deployments

**Test-First Strategy**: Comprehensive integration tests before each phase to ensure behavioral compatibility during modernization.

---

## Analysis Summary

This comprehensive analysis reveals a **well-structured legacy Spring application** that is surprisingly modern in many aspects. The application already uses **Java 17**, **Spring Framework 6.x**, and **Jakarta EE namespaces** - major components needed for Spring Boot 3 migration.

### Key Strengths
- ✅ **Modern Java/Spring baseline** already established
- ✅ **Clean 3-layer architecture** (web → service → repository)
- ✅ **Multiple persistence strategies** (JPA/JDBC/Spring Data)
- ✅ **Comprehensive test coverage** with modern testing stack
- ✅ **Container-ready deployment** (Docker + Jib)

### Primary Migration Challenges
- 🔄 **XML → JavaConfig transformation** (5 configuration files)
- 🔄 **JSP → Next.js frontend** (18 view files + custom tags)
- 🔄 **WAR → JAR packaging** for Spring Boot embedded container
- 🔄 **Session state → REST API** architectural shift

### Risk Mitigation Strategy

The **phased approach** minimizes disruption while maintaining system stability:

1. **Low-Risk First**: Dependency cleanup and Spring Boot migration leverage existing modern components
2. **Incremental API Introduction**: Backend APIs developed alongside existing JSP views 
3. **Parallel Frontend Development**: Next.js development while JSP views remain functional
4. **Controlled Cutover**: View-by-view migration with rollback capabilities
5. **Production Hardening**: Comprehensive testing and monitoring throughout

### Success Metrics
- **Zero downtime** during backend migration phases
- **Feature parity** maintained throughout frontend transition
- **Performance improvements** through modern stack optimizations
- **Developer productivity gains** via improved tooling and architecture
- **Security posture enhancement** through modern framework capabilities

This analysis provides a **clear roadmap from legacy Spring MVC + JSP to modern Spring Boot 3 + Next.js architecture** while preserving all existing business functionality and ensuring migration safety through comprehensive risk assessment and phased execution strategy.