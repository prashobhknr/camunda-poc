# Samrum Camunda 7 POC - Door Installation Process

**Proof of Concept** for migrating Samrum process orchestration from Bizagi to Camunda 7.

---

## 🚀 Quick Start (No Docker Required!)

### Prerequisites
- ✅ Java 11 or 17
- ✅ Maven 3.8+

### Run as Spring Boot Application

```bash
# Navigate to the project
cd /Users/prashobh/.openclaw/workspace/camunda-poc/camunda-engine

# Run directly with Maven
mvn spring-boot:run
```

**Wait for this message:**
```
✅ Camunda Engine started successfully!
📊 Access Camunda Cockpit: http://localhost:8080/camunda
📋 Access Tasklist: http://localhost:8080/camunda/app/tasklist
```

**Login**: `admin` / `admin`

### Alternative: Build and Run JAR

```bash
# Build
mvn clean package -DskipTests

# Run JAR
java -jar target/camunda-poc-engine-1.0.0-SNAPSHOT.jar
```

---

## 📚 Documentation

| Document | Purpose |
|----------|---------|
| **[RUN-SPRINGBOOT.md](RUN-SPRINGBOOT.md)** | 🏃 **Start here!** Complete guide to running the Spring Boot app |
| [README.md](README.md.old) | Original overview (being updated) |
| [ARCHITECTURE.md](ARCHITECTURE.md) | 🏗️ Technical architecture and design |
| [QUICKSTART.md](QUICKSTART.md) | 🚀 Step-by-step testing guide |
| [PROJECT-SUMMARY.md](PROJECT-SUMMARY.md) | 📊 Complete project summary |

---

## 🎯 What This POC Demonstrates

### Business Process: Door Installation Approval

```
Submit Design → Engineer Review → Decision → [Approve | Reject | Revise]
```

This is a **subset** of Samrum's 25+ Bizagi processes, proving the migration approach works.

### Key Features
- ✅ **Camunda 7.19** Community Edition
- ✅ **Spring Boot 2.7** Integration
- ✅ **BPMN 2.0** Process Definition
- ✅ **REST API** for process control
- ✅ **H2 Database** (development) / **PostgreSQL** (production)
- ✅ **Camunda Cockpit** for monitoring
- ✅ **Tasklist** for user tasks

---

## 🧪 Test the POC

### 1. Start a Process

```bash
curl -X POST http://localhost:8080/api/door-process/start \
  -H "Content-Type: application/json" \
  -d '{
    "projectId": "PROJ-001",
    "doorType": "Fire Door Type A",
    "reviewerId": "engineer1",
    "designerId": "designer1"
  }'
```

### 2. View Tasks

```bash
curl http://localhost:8080/api/door-process/tasks/engineer1
```

### 3. Complete Task

```bash
curl -X POST http://localhost:8080/api/door-process/tasks/{taskId}/complete \
  -H "Content-Type: application/json" \
  -d '{
    "approvalDecision": "APPROVED",
    "comments": "Design looks good!"
  }'
```

---

## 📁 Project Structure

```
camunda-poc/
│
├── 📘 RUN-SPRINGBOOT.md          # Complete running guide
├── 🏗️ ARCHITECTURE.md             # Technical architecture
├── 🚀 QUICKSTART.md               # Testing guide
├── 📊 PROJECT-SUMMARY.md          # Project summary
│
└── camunda-engine/                # Spring Boot Application
    ├── pom.xml                    # Maven configuration
    ├── Dockerfile                 # (Optional) Docker build
    │
    └── src/main/
        ├── java/com/samrum/
        │   ├── CamundaPocApplication.java    # Main entry point
        │   ├── delegate/
        │   │   ├── ApprovalNotificationDelegate.java
        │   │   └── RejectionNotificationDelegate.java
        │   ├── rest/
        │   │   └── DoorProcessController.java    # REST API
        │   └── dto/
        │       ├── DoorProcessRequest.java
        │       └── TaskCompletionRequest.java
        │
        └── resources/
            ├── application.yml                   # Configuration
            └── processes/
                └── door-installation.bpmn        # Process definition
```

---

## 🌐 Access Points

Once running, access these URLs:

| Service | URL | Credentials |
|---------|-----|-------------|
| **Camunda Cockpit** | http://localhost:8080/camunda | admin / admin |
| **Tasklist** | http://localhost:8080/camunda/app/tasklist | admin / admin |
| **REST API** | http://localhost:8080/engine-rest | - |
| **Custom API** | http://localhost:8080/api/door-process | - |
| **H2 Console** | http://localhost:8080/h2-console | sa / (blank) |

---

## 🎓 What You'll Learn

1. ✅ How to run Camunda 7 as a Spring Boot application
2. ✅ How to deploy BPMN processes
3. ✅ How to start and complete processes via API
4. ✅ How to monitor processes in Cockpit
5. ✅ Migration approach from Bizagi to Camunda

---

## 🔄 Next Steps

### Immediate
1. **Run the application**: `mvn spring-boot:run`
2. **Explore Cockpit**: http://localhost:8080/camunda
3. **Test the API**: Use curl commands above

### Short Term
- Add more processes (Fire Safety, Document Review)
- Implement email notifications
- Create React frontend
- Connect to Samrum database

### Long Term
- Migrate all 25+ Bizagi processes
- Deploy to production
- Train users

---

## 🛠️ Troubleshooting

### Port 8080 Already in Use
```bash
# Kill the process
lsof -ti:8080 | xargs kill -9

# Or change port
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"
```

### Maven Build Fails
```bash
# Clean and rebuild
mvn clean install -DskipTests -U

# Check Java version
java -version  # Must be 11+
```

### Can't Access Cockpit
```bash
# Check if app is running
curl http://localhost:8080/engine-rest

# Check logs in terminal
```

---

## 📞 Support

- **Running Guide**: [RUN-SPRINGBOOT.md](RUN-SPRINGBOOT.md)
- **Camunda Docs**: https://docs.camunda.org/manual/7.19/
- **BPMN 2.0**: https://www.omg.org/spec/BPMN/2.0/

---

**🎉 Ready to run!**

```bash
cd /Users/prashobh/.openclaw/workspace/camunda-poc/camunda-engine
mvn spring-boot:run
```

**Then open**: http://localhost:8080/camunda

---

**Created**: 2026-02-18  
**Purpose**: Demonstrate Camunda 7 migration from Bizagi  
**Status**: ✅ Complete and Runnable  
**Version**: 1.0.0
