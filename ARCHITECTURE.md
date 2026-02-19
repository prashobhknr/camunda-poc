# Samrum Process Orchestration - Architecture Overview

## 📋 Executive Summary

This document provides a comprehensive architecture overview for migrating the **Samrum** system from Bizagi to **Camunda 7** for process orchestration. The system manages construction/facility management processes with focus on door/lock systems, fire safety, HVAC, electrical systems, and project data management.

---

## 🏗️ Current System Analysis

### Legacy Platform: Bizagi
- **Process Files**: 25+ BPMN processes (`.bpm` files)
- **Database**: SQL Server (`SAMRUM_Master`)
- **Key Processes Identified**:
  - Brandlarm (Fire Alarm) - `Brandlarm.bpm`
  - Brandspjäll (Fire Damper) - `Brandspjällsprocessen.bpm`
  - Dörrprocessen (Door Process) - `Dörrprocessen helhet...bpm`
  - Låssmed (Locksmith) - `Låssmedsprocessen.bpm`
  - Ventilation (HVAC) - `Ventilation.bpm`
  - El belysning (Electrical Lighting) - `El belysning.bpm`
  - IFC-process (Building Information Modeling) - `IFC-process.bpm`
  - Passagekontroll (Access Control) - `Passagekontroll.bpm`

### Database Schema (Key Tables)
```sql
-- Core Tables Identified:
- DV_DatabaseVersion          // Database versioning
- PDB_ProjectDatabase         // Project databases
- US_User                     // User management
- UR_UserRole                 // Role definitions
- RFU_RoleForUser             // User-role assignments
- UPDB_UserInDatabase         // User access to projects
- VAV_ValidApplicationVersion // Application versioning
```

### Business Domain
The system manages:
1. **Construction Projects** - Multi-project architecture
2. **Object Types** (OT_ObjectType) - Building components
3. **Data Types** (DT_DataType) - Standardized data structures
4. **Version Control** - Document and design versioning
5. **Role-Based Access** - Granular permissions per project

---

## 🎯 Target Architecture: Camunda 7

### High-Level Architecture Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                        │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐          │
│  │  Web App    │  │  Mobile     │  │  Admin      │          │
│  │  (React)    │  │  App        │  │  Portal     │          │
│  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘          │
│         │                │                │                   │
│         └────────────────┴────────────────┘                   │
│                          │                                    │
│                  REST API Gateway                             │
└──────────────────────────┼────────────────────────────────────┘
                           │
┌──────────────────────────┼────────────────────────────────────┐
│                 PROCESS ORCHESTRATION LAYER                   │
│                          │                                    │
│  ┌───────────────────────▼───────────────────────┐            │
│  │          CAMUNDA 7 ENGINE                     │            │
│  │  ┌─────────────────────────────────────────┐  │            │
│  │  │  Process Definitions (BPMN 2.0)        │  │            │
│  │  │  - Door Installation Process           │  │            │
│  │  │  - Fire Safety Approval Process        │  │            │
│  │  │  - Document Review Process             │  │            │
│  │  └─────────────────────────────────────────┘  │            │
│  │  ┌─────────────────────────────────────────┐  │            │
│  │  │  Java Delegates / External Tasks       │  │            │
│  │  │  - Integration Services                │  │            │
│  │  │  - Business Logic                      │  │            │
│  │  └─────────────────────────────────────────┘  │            │
│  │  ┌─────────────────────────────────────────┐  │            │
│  │  │  Tasklist API / Cockpit                │  │            │
│  │  └─────────────────────────────────────────┘  │            │
│  └───────────────────────┬───────────────────────┘            │
│                          │                                    │
└──────────────────────────┼────────────────────────────────────┘
                           │
┌──────────────────────────┼────────────────────────────────────┐
│                    SERVICE LAYER                              │
│  ┌───────────────────────▼───────────────────────┐            │
│  │         SPRING BOOT MICROSERVICES            │            │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐   │            │
│  │  │  User    │  │ Project  │  │ Document │   │            │
│  │  │  Service │  │ Service  │  │ Service  │   │            │
│  │  └──────────┘  └──────────┘  └──────────┘   │            │
│  └───────────────────────┬───────────────────────┘            │
│                          │                                    │
└──────────────────────────┼────────────────────────────────────┘
                           │
┌──────────────────────────┼────────────────────────────────────┐
│                     DATA LAYER                                │
│  ┌───────────────────────▼───────────────────────┐            │
│  │              DATABASES                        │            │
│  │  ┌─────────────────┐  ┌─────────────────┐    │            │
│  │  │  Camunda DB     │  │  Business DB    │    │            │
│  │  │  (H2/PostgreSQL)│  │  (PostgreSQL)   │    │            │
│  │  │  - ACT_* tables │  │  - Users        │    │            │
│  │  │  - Process Inst │  │  - Projects     │    │            │
│  │  │  - Tasks        │  │  - Documents    │    │            │
│  │  └─────────────────┘  └─────────────────┘    │            │
│  └───────────────────────────────────────────────┘            │
│                                                               │
│  ┌─────────────────┐  ┌─────────────────┐                    │
│  │  File Storage   │  │  External APIs  │                    │
│  │  (MinIO/S3)     │  │  (IFC, BIM)     │                    │
│  └─────────────────┘  └─────────────────┘                    │
└───────────────────────────────────────────────────────────────┘
```

---

## 🚀 POC Scope: Door Installation Process

### Why This Process?
1. **Representative**: Covers typical workflow patterns
2. **Manageable**: Medium complexity for POC
3. **Business Value**: Core to Samrum operations
4. **Integration Points**: Demonstrates external system connectivity

### POC Process Flow

```
┌─────────┐     ┌──────────────┐     ┌─────────────┐
│  Start  │────▶│ Design Review │────▶│  Approval   │
│  (API)  │     │   (Task)      │     │  (Gateway)  │
└─────────┘     └──────────────┘     └──────┬──────┘
                                            │
                    ┌───────────────────────┼───────────────────────┐
                    │                       │                       │
              ┌─────▼─────┐           ┌─────▼─────┐           ┌─────▼─────┐
              │  Approved │           │  Rejected │           │  Changes  │
              │    (End)  │           │    (End)  │           │  Requested│
              └───────────┘           └───────────┘           └─────┬─────┘
                                                                    │
                                                            ┌───────▼───────┐
                                                            │   Revise      │
                                                            │   (Task)      │
                                                            └───────┬───────┘
                                                                    │
                                                                    └──────┐
                                                                           │
                                                                   (loop back)
```

### BPMN Elements Used
- **Start Event**: API-triggered process start
- **User Tasks**: Design Review, Approval, Revise
- **Exclusive Gateway**: Approval decision
- **End Events**: Approved, Rejected
- **Sequence Flows**: Process flow with conditions

---

## 📁 Project Structure

```
camunda-poc/
├── README.md
├── ARCHITECTURE.md                    # This file
├── pom.xml                            # Maven build
├── docker-compose.yml                 # Local development
│
├── camunda-engine/                    # Camunda 7 Spring Boot
│   ├── src/main/java/
│   │   └── com/samrum/
│   │       ├── CamundaApplication.java
│   │       ├── config/
│   │       │   ├── CamundaConfig.java
│   │       │   └── SecurityConfig.java
│   │       ├── delegate/
│   │       │   ├── DesignReviewDelegate.java
│   │       │   └── ApprovalNotificationDelegate.java
│   │       ├── rest/
│   │       │   └── ProcessController.java
│   │       └── service/
│   │           └── DoorProcessService.java
│   │
│   └── src/main/resources/
│       ├── application.yml
│       ├── processes/
│       │   └── door-installation.bpmn
│       └── migrations/
│           └── V1__initial_schema.sql
│
├── business-services/                 # Business microservices
│   ├── user-service/
│   ├── project-service/
│   └── document-service/
│
├── web-client/                        # React frontend
│   └── src/
│       ├── components/
│       ├── pages/
│       └── services/
│
└── docs/
    ├── process-diagrams/
    ├── api-specs/
    └── migration-guide.md
```

---

## 🔧 Technology Stack

### Core Platform
- **Process Engine**: Camunda 7.19+ (Spring Boot integration)
- **Backend Framework**: Spring Boot 2.7+ / 3.x
- **Database**: 
  - Camunda: H2 (dev) / PostgreSQL (prod)
  - Business Data: PostgreSQL 14+
- **Build Tool**: Maven 3.8+

### Frontend
- **Framework**: React 18+ with TypeScript
- **UI Library**: Material-UI or Ant Design
- **State Management**: React Query + Zustand
- **Camunda Integration**: camunda-bpmn-js, camunda-external-task-client

### Infrastructure
- **Containerization**: Docker & Docker Compose
- **File Storage**: MinIO (S3-compatible)
- **API Gateway**: Spring Cloud Gateway
- **Monitoring**: Camunda Cockpit + Prometheus + Grafana

### Integration Patterns
- **External Tasks**: For long-running service tasks
- **Java Delegates**: For synchronous business logic
- **REST APIs**: For microservice communication
- **Message Correlation**: For async process signaling

---

## 📊 Database Migration Strategy

### Phase 1: Schema Analysis
```sql
-- Legacy SQL Server → PostgreSQL mapping
-- Example: User table migration

-- OLD (SQL Server)
CREATE TABLE [dbo].[US_User](
  [US_ID] [int] IDENTITY(1,1) NOT NULL,
  [US_UserNme] [varchar](255) NOT NULL,
  [CREATED_BY] [varchar](50) NOT NULL,
  [CREATED_DATE] [datetime] NOT NULL,
  ...
)

-- NEW (PostgreSQL)
CREATE TABLE us_user (
  us_id SERIAL PRIMARY KEY,
  us_user_name VARCHAR(255) NOT NULL UNIQUE,
  created_by VARCHAR(50) NOT NULL DEFAULT current_user,
  created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  ...
);
```

### Phase 2: Data Migration
1. Export from SQL Server (BCP/SSIS)
2. Transform (data type mapping, encoding)
3. Load into PostgreSQL (COPY command)
4. Validate row counts and referential integrity

### Phase 3: Camunda Tables
- Camunda creates `ACT_*` tables automatically
- No migration needed - fresh installation
- Historical data can be imported via CSV if needed

---

## 🔄 Process Migration Approach

### From Bizagi to Camunda BPMN 2.0

#### Bizagi Constructs → Camunda Equivalents
| Bizagi | Camunda 7 | Notes |
|--------|-----------|-------|
| Task | User Task / Service Task | Direct mapping |
| Gateway (XOR) | Exclusive Gateway | Same semantics |
| Gateway (AND) | Parallel Gateway | Same semantics |
| Subprocess | Embedded Subprocess / Call Activity | Depends on reuse |
| Event (Start/End) | Start/End Event | Direct mapping |
| Pool/Lane | Participant/Lane | Same concepts |
| Business Rule | Business Rule Task | DMN integration |
| Script Task | Script Task | JavaScript/Groovy |

### Migration Steps
1. **Export** Bizagi processes as BPMN 2.0
2. **Analyze** proprietary extensions
3. **Transform** to Camunda-compatible BPMN
4. **Add** Camunda-specific extensions (execution listeners, task listeners)
5. **Test** in Camunda Modeler
6. **Deploy** to Camunda Engine

---

## 🔐 Security Architecture

### Authentication
- **Provider**: Keycloak or Spring Security OAuth2
- **Protocol**: OIDC / SAML 2.0
- **Integration**: Camunda identity provider SPI

### Authorization
- **Camunda Permissions**: 
  - Process Definition: READ, UPDATE, DELETE
  - Process Instance: READ, UPDATE
  - Task: READ, UPDATE
  - Authorization: CREATE, DELETE
- **Application-Level**: Role-based access control (RBAC)
- **Data-Level**: Project-based isolation

### Network Security
- **TLS**: All external communication
- **API Authentication**: JWT tokens
- **Database**: Encrypted connections
- **Secrets**: Vault or Kubernetes Secrets

---

## 📈 Scalability Considerations

### Horizontal Scaling
- **Stateless Services**: All microservices are stateless
- **Session Clustering**: Redis for distributed sessions
- **Database**: Read replicas for business DB
- **Camunda**: Shared database with proper indexing

### Performance Optimization
- **Async Executor**: Tuned for workload
- **Database Indexes**: On process variables, task assignments
- **Caching**: 
  - Process definitions (Camunda built-in)
  - User/role data (Redis)
  - API responses (Spring Cache)

### Monitoring
- **Camunda Cockpit**: Process instance tracking
- **Metrics**: 
  - Process duration
  - Task completion time
  - Error rates
  - Resource utilization
- **Alerting**: Slack/email on failures

---

## 🧪 Testing Strategy

### Unit Testing
```java
@Deployment
public class DoorProcessTest {
  
  @Test
  public void testHappyPath() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey(
      "doorInstallationProcess", 
      variables
    );
    
    // Complete design review task
    Task task = taskService.createTaskQuery()
      .processInstanceId(pi.getId())
      .taskDefinitionKey("designReview")
      .singleResult();
    
    taskService.complete(task.getId(), approvalVars);
    
    // Assert process completed
    assertThat(pi).isEnded();
  }
}
```

### Integration Testing
- **Testcontainers**: PostgreSQL, Camunda
- **API Tests**: REST Assured
- **Process Tests**: Camunda BPM Assert

### End-to-End Testing
- **UI Tests**: Playwright / Cypress
- **Performance**: Gatling / JMeter
- **Chaos**: Chaos Monkey for resilience

---

## 📅 Implementation Roadmap

### Phase 1: Foundation (Weeks 1-4)
- [x] Architecture design
- [ ] Set up Camunda 7 with Spring Boot
- [ ] Create POC process (Door Installation)
- [ ] Basic frontend with task list
- [ ] Docker Compose for local dev

### Phase 2: Core Features (Weeks 5-8)
- [ ] User management integration
- [ ] Project/service integration
- [ ] Document upload/review
- [ ] Email notifications
- [ ] Camunda Cockpit configuration

### Phase 3: Migration (Weeks 9-16)
- [ ] Migrate 3-5 priority processes
- [ ] Database migration scripts
- [ ] Data migration ETL
- [ ] Integration with external systems (IFC/BIM)
- [ ] Performance testing

### Phase 4: Production Ready (Weeks 17-24)
- [ ] Security hardening
- [ ] Monitoring & alerting
- [ ] CI/CD pipeline
- [ ] Documentation
- [ ] User training
- [ ] Gradual rollout

---

## 🎓 Key Learnings from Legacy System

### What to Keep
1. **Role-based access** - Granular permissions work well
2. **Project isolation** - Multi-tenant architecture
3. **Version control** - Critical for audit trails
4. **Process visualization** - BPMN diagrams are valuable

### What to Improve
1. **Performance** - Optimize database queries
2. **UX** - Modern, responsive interface
3. **Integration** - RESTful APIs over direct DB access
4. **Monitoring** - Real-time process analytics
5. **Deployment** - Containerized, cloud-native

---

## 📞 Support & Resources

### Documentation
- [Camunda 7 Docs](https://docs.camunda.org/manual/7.19/)
- [Spring Boot Integration](https://docs.camunda.org/manual/latest/user-guide/spring-boot-integration/)
- [BPMN 2.0 Reference](https://www.omg.org/spec/BPMN/2.0/)

### Community
- [Camunda Forum](https://forum.camunda.io/)
- [Stack Overflow](https://stackoverflow.com/questions/tagged/camunda)

### Internal Contacts
- **Architecture Team**: [TBD]
- **Process Owners**: [TBD]
- **Legacy System SME**: [TBD]

---

## 📝 Appendix

### A. Glossary
- **Samrum**: Swedish construction/facility management platform
- **POC**: Proof of Concept
- **BPMN**: Business Process Model and Notation
- **PLCS**: Product Life Cycle Support (industry standard)

### B. File Inventory
Located in `/Users/prashobh/Downloads/Visakh/`:
- 25+ Bizagi process files (`.bpm`)
- SQL Server database scripts
- Excel data dictionaries
- PDF documentation (Swedish)
- Visio diagrams

### C. Next Steps
1. Review this architecture with stakeholders
2. Set up development environment
3. Implement POC process
4. Schedule demo and feedback session

---

**Document Version**: 1.0  
**Created**: 2026-02-18  
**Author**: Code Agent (GLM-5)  
**Status**: Draft for Review
