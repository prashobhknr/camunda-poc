# Samrum/Camunda 7 Process Orchestration Architecture

## Executive Summary

This document describes the architecture for modernizing the legacy Samrum system using Camunda 7 for process orchestration. The existing system, built on Bizagi BPMN models and SQL Server databases, manages construction and facility management processes for building systems (HVAC, electrical, fire safety, doors, etc.).

## Current State Analysis

### Existing System Components

#### 1. **Business Process Layer (Bizagi)**
- **20+ BPMN processes** covering building systems:
  - Fire alarm systems (Brandlarm)
  - Door management (Dörrprocessen)
  - HVAC systems (Ventilation)
  - Electrical systems (Elkraft, Belysning)
  - Security systems (Säkerhetssystem, Passagekontroll)
  - Elevator systems (Hiss)
  - Sprinkler systems
  
- **Process Structure**:
  - Main processes with sub-processes
  - Sections/Components (e.g., "Sektion 1", "Sektion 2")
  - Signal flows to control systems
  - Integration points with external systems (SOS Alarm, fire department)

#### 2. **Data Layer (SQL Server)**
- **Database**: SAMRUM_Master
- **Key Tables**:
  - `DV_DatabaseVersion` - Version tracking
  - `PDB_ProjectDatabase` - Project databases
  - `UR_UserRole` - User roles
  - `US_User` - Users
  - `RFU_RoleForUser` - Role assignments
  - `UPDB_UserInDatabase` - User database access

#### 3. **Business Domain**
- **PLCS (Product Life Cycle Support)** framework
- **AEC (Architecture, Engineering, Construction)** industry models
- **Object-oriented structure**:
  - Objects (Doors, Fire alarms, etc.)
  - Object types
  - Data types
  - Relationships and connections

### Pain Points in Current System
1. **Limited workflow automation** - Bizagi models are documentation-only
2. **Manual process execution** - No runtime workflow engine
3. **Siloed processes** - Limited integration between systems
4. **No real-time monitoring** - Cannot track process instances
5. **Hard-coded business logic** - Changes require code deployment

## Target Architecture with Camunda 7

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────┐
│                   Presentation Layer                     │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐     │
│  │  Web App    │  │  Admin UI   │  │  Mobile App │     │
│  │  (React)    │  │  (Camunda)  │  │  (Future)   │     │
│  └─────────────┘  └─────────────┘  └─────────────┘     │
└─────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────┐
│                   API Gateway Layer                      │
│  ┌─────────────────────────────────────────────────┐    │
│  │           REST API (Spring Boot)                │    │
│  │    - Process Endpoints                          │    │
│  │    - Task Management                            │    │
│  │    - User Management                            │    │
│  └─────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────┐
│                Process Orchestration Layer               │
│  ┌─────────────────────────────────────────────────┐    │
│  │          Camunda 7 Workflow Engine              │    │
│  │    - BPMN 2.0 Process Execution                 │    │
│  │    - DMN Decision Tables                        │    │
│  │    - Form Engine                                │    │
│  │    - External Task Pattern                      │    │
│  └─────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────┐
│                   Service Layer                          │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐  │
│  │ Process  │ │  Task    │ │  User    │ │External  │  │
│  │ Service  │ │ Service  │ │ Service  │ │ Systems  │  │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘  │
└─────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────┐
│                   Data Layer                             │
│  ┌──────────────┐          ┌──────────────┐            │
│  │  Camunda DB  │          │  Business DB │            │
│  │ (PostgreSQL/ │          │  (SQL Server │            │
│  │   H2)        │          │   Migration) │            │
│  └──────────────┘          └──────────────┘            │
└─────────────────────────────────────────────────────────┘
```

### Component Details

#### 1. **Camunda 7 Engine**
- **Version**: 7.20+ (Latest stable)
- **Deployment**: Embedded in Spring Boot application
- **Database**: PostgreSQL (production), H2 (development/testing)
- **Key Features**:
  - BPMN 2.0 process execution
  - DMN 1.3 decision engine
  - Form engine for task forms
  - External task pattern for workers
  - Cockpit for monitoring
  - Optimize for process analytics

#### 2. **Spring Boot Application**
- **Version**: 3.2+
- **Modules**:
  - `camunda-bpm-spring-boot-starter`
  - `spring-boot-starter-web`
  - `spring-boot-starter-data-jpa`
  - `spring-boot-starter-security`

#### 3. **Process Migration Strategy**

**Phase 1: Door Management Process (POC)**
- Selected from: "Dörrprocessen helhet Bizagi"
- Reason: Clear boundaries, well-defined steps, high business value
- Scope: Door request → Approval → Installation → Verification

**Phase 2: Fire Alarm Process**
- Selected from: "Brandlarm.bpm"
- Components: Detection → Alarm → Notification → Response

**Phase 3: Integration Processes**
- Signal routing to building management systems
- External system integrations (SOS Alarm, etc.)

### Process Example: Door Management POC

```
┌─────────────────────────────────────────────────────────┐
│           Door Management Process (Simplified)           │
└─────────────────────────────────────────────────────────┘

[Start] → [Submit Door Request] → [Auto-Validate]
                                      │
                    ┌─────────────────┴─────────────────┐
                    │                                   │
              [Valid?]──No──→ [Reject & Notify]        │
                    │                                   │
                   Yes                                  │
                    │                                   │
                    ▼                                   │
         [Route for Approval] ←─────────────────────────┘
                    │
        ┌───────────┴───────────┐
        │                       │
  [Facility Manager]      [Security Manager]
        │                       │
        └───────────┬───────────┘
                    │
              [All Approved?]──No──→ [Request Changes]
                    │                       │
                   Yes                      │
                    │                       └──────────┐
                    ▼                                  │
         [Create Work Order]                           │
                    │                                  │
                    ▼                                  │
         [Assign to Technician]                        │
                    │                                  │
                    ▼                                  │
         [Install Door] ←──────────────────────────────┘
                    │
                    ▼
         [Quality Check]
                    │
        ┌───────────┴───────────┐
        │                       │
     [Pass?]──No──→ [Rework]   │
        │                       │
       Yes                      │
        │                       │
        ▼                       │
[Update Asset Register]         │
        │                       │
        ▼                       │
[Notify Stakeholders]           │
        │                       │
        ▼                       │
      [End] ←───────────────────┘
```

### Data Migration Approach

#### Option 1: Parallel Run (Recommended)
- Keep existing SQL Server database
- Camunda uses separate PostgreSQL database
- Gradual migration of business data
- Bi-directional sync during transition

#### Option 2: Big Bang Migration
- Migrate all data to new schema
- Switch to Camunda-driven processes
- Higher risk, faster cutover

#### Data Mapping
```
Legacy Table          →  New Entity
─────────────────────────────────────────
US_User              →  User (Camunda Identity)
UR_UserRole          →  Group/Authorization
PDB_ProjectDatabase  →  Project/ProcessInstance
DV_DatabaseVersion   →  ProcessDefinition Version
```

### Integration Patterns

#### 1. **External Task Pattern** (Recommended)
```java
@Component
public class DoorInstallationWorker {
    
    @ExternalTaskSubscription("install-door")
    public void installDoor(ExternalTask task, ExternalTaskService service) {
        // Business logic here
        String doorId = task.getVariable("doorId");
        // ... installation logic
        
        service.complete(task, variables);
    }
}
```

#### 2. **Java Delegate Pattern**
```java
public class ValidateDoorRequest implements JavaDelegate {
    @Override
    public void execute(DelegateExecution execution) {
        // Validation logic
        Boolean isValid = validate(execution.getVariable("doorSpec"));
        execution.setVariable("valid", isValid);
    }
}
```

#### 3. **REST API Integration**
```java
@RestController
@RequestMapping("/api/processes")
public class ProcessController {
    
    @PostMapping("/door-request")
    public ResponseEntity<ProcessInstanceDto> startDoorRequest(
            @RequestBody DoorRequestDto request) {
        // Start process instance
    }
}
```

### Security Architecture

#### Authentication
- Spring Security with OAuth2/OIDC
- Integration with existing user directory
- Role-based access control (RBAC)

#### Authorization
- Camunda authorization concepts
- Process-level permissions
- Task-level permissions
- Data-level security

```
┌────────────────────────────────────────┐
│         Security Layers                │
├────────────────────────────────────────┤
│ 1. Network Security (TLS/SSL)          │
│ 2. Application Security (Spring Sec)   │
│ 3. Process Security (Camunda AuthZ)    │
│ 4. Data Security (Encryption at Rest)  │
└────────────────────────────────────────┘
```

### Deployment Architecture

#### Development
```
Developer Machine
├── Docker Compose
│   ├── Camunda (with H2)
│   ├── PostgreSQL
│   └── Keycloak (optional)
└── Spring Boot App (IDE)
```

#### Production
```
Kubernetes Cluster
├── Camunda Engine (StatefulSet)
├── PostgreSQL (StatefulSet + Backup)
├── Spring Boot App (Deployment)
├── Load Balancer
└── Monitoring Stack (Prometheus/Grafana)
```

### Monitoring & Observability

#### Camunda Cockpit
- Process instance monitoring
- Incident management
- Task list overview
- Historical analysis

#### Custom Dashboards
- Process KPIs (cycle time, success rate)
- Task completion metrics
- User productivity metrics
- System health metrics

#### Logging
- Structured logging (JSON format)
- Correlation IDs for process tracing
- Integration with ELK stack or similar

## Implementation Roadmap

### Phase 1: Foundation (Weeks 1-4)
- [x] Set up development environment
- [ ] Create Spring Boot application with Camunda
- [ ] Implement basic CI/CD pipeline
- [ ] Set up monitoring infrastructure

### Phase 2: POC Process (Weeks 5-8)
- [ ] Model Door Management BPMN
- [ ] Implement Java delegates
- [ ] Create REST APIs
- [ ] Build simple UI (Camunda Tasklist)
- [ ] Test with sample data

### Phase 3: Enhancement (Weeks 9-12)
- [ ] Add DMN decision tables
- [ ] Implement form engine
- [ ] Add authentication/authorization
- [ ] Performance testing
- [ ] Documentation

### Phase 4: Production Ready (Weeks 13-16)
- [ ] Security hardening
- [ ] Production deployment
- [ ] User training
- [ ] Migration planning for next process

## Technical Decisions

### Why Camunda 7 (not 8)?
- Mature, stable version
- Large community and ecosystem
- Compatible with existing Java skills
- Sufficient for current requirements
- Can upgrade to Camunda 8 later if needed

### Why Spring Boot?
- Industry standard for Java microservices
- Excellent Camunda integration
- Large ecosystem of starters
- Easy deployment and scaling

### Why PostgreSQL?
- Open source
- Excellent performance
- Strong consistency guarantees
- Good Camunda support

## Risk Mitigation

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| Data migration complexity | High | Medium | Parallel run, gradual migration |
| Performance issues | Medium | Low | Load testing, proper indexing |
| User adoption | High | Medium | Training, intuitive UI, change management |
| Integration failures | Medium | Medium | Robust error handling, monitoring |
| Scope creep | Medium | High | Strict POC boundaries, phased approach |

## Success Criteria

### Technical
- Process instances execute without errors
- Response time < 2 seconds for API calls
- 99.9% uptime for production system
- Successful migration of door management process

### Business
- Reduced process cycle time by 30%
- Improved visibility into process status
- Reduced manual handoffs
- Better compliance tracking

## Next Steps

1. **Review this architecture** with stakeholders
2. **Set up development environment** (Docker, IDE, etc.)
3. **Create POC repository** with initial structure
4. **Model the door management process** in BPMN
5. **Implement first working prototype**

## Appendix A: File Structure Reference

```
/Users/prashobh/Downloads/Visakh/
├── Bizagi-processes, schemas/    # BPMN files (Bizagi format)
├── Database Excel/                # Data dictionaries, lookup tables
├── Database schemas/              # SQL Server schema scripts
├── PLCS/                          # Industry standard docs
├── Start/                         # Additional process docs
└── Documents to Eurostep/         # Integration specs
```

## Appendix B: Key Stakeholders

- **Process Owners**: Facility managers, security managers
- **IT Operations**: System administrators, database admins
- **End Users**: Technicians, approvers, requestors
- **Compliance**: Safety officers, auditors

---

*Document Version: 1.0*  
*Created: 2026-02-18*  
*Author: Code Agent (GLM-5)*  
*Review Date: TBD*
