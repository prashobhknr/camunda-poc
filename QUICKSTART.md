# 🚀 Quick Start Guide - Camunda 7 POC

## Get Running in 5 Minutes!

### Option 1: Run with Java (Fastest)

```bash
# Navigate to the engine directory
cd camunda-poc/camunda-engine

# Run with Maven
mvn spring-boot:run

# Wait for this message:
# ✅ Camunda Engine started successfully!
# 📊 Access Camunda Cockpit: http://localhost:8080/camunda
```

**Access Points:**
- 📊 **Camunda Cockpit**: http://localhost:8080/camunda
- 📋 **Tasklist**: http://localhost:8080/camunda/app/tasklist
- 🔧 **REST API**: http://localhost:8080/engine-rest
- 💾 **H2 Console**: http://localhost:8080/h2-console

---

### Option 2: Run with Docker

```bash
# From the root directory
cd camunda-poc
docker-compose up

# Or with PostgreSQL (production-like)
docker-compose --profile prod up
```

**Additional Access Points with Docker:**
- 🐘 **pgAdmin**: http://localhost:5050 (email: admin@samrum.com, pass: admin)

---

## 🎯 Try the POC - Step by Step

### Step 1: Start a Process

Open a terminal and run:

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

**Expected Response:**
```json
{
  "processInstanceId": "abc123...",
  "businessKey": "PROJ-001",
  "currentTask": "Design Review",
  "currentTaskAssignee": "engineer1"
}
```

### Step 2: View Tasks for the Engineer

```bash
curl http://localhost:8080/api/door-process/tasks/engineer1
```

**Expected Response:**
```json
[
  {
    "taskId": "xyz789...",
    "taskName": "Design Review",
    "projectId": "PROJ-001",
    "doorType": "Fire Door Type A"
  }
]
```

### Step 3: Complete the Task (Approve)

```bash
curl -X POST http://localhost:8080/api/door-process/tasks/xyz789.../complete \
  -H "Content-Type: application/json" \
  -d '{
    "approvalDecision": "APPROVED",
    "comments": "Design looks good! Meets all requirements."
  }'
```

**Expected Response:**
```json
{
  "taskId": "xyz789...",
  "completed": true,
  "processStatus": "COMPLETED"
}
```

### Step 4: Check Process Status

```bash
curl http://localhost:8080/api/door-process/instances
```

---

## 🎨 Visual Workflow

Here's what happens when you run the POC:

```
┌─────────────┐
│  START      │  POST /api/door-process/start
│  (API)      │  → Creates process instance
└──────┬──────┘
       │
       ▼
┌─────────────────┐
│ Design Review   │  Task assigned to engineer1
│ (User Task)     │  → Engineer reviews CAD drawings
└──────┬──────────┘
       │
       ▼
┌─────────────────┐
│ Submit Decision │  POST complete with decision
│ (User Task)     │  → APPROVED / REJECTED / CHANGES
└──────┬──────────┘
       │
       ▼
    ┌──────┐
    │ XOR  │  Decision Gateway
    │Gate  │
    └──┬───┘
       │
   ┌───┴────┬──────────────┐
   │        │              │
   ▼        ▼              ▼
APPROVED  REJECTED    CHANGES_NEEDED
   │        │              │
   │        │              ▼
   │        │        ┌──────────┐
   │        │        │  Revise  │
   │        │        │  Design  │
   │        │        └────┬─────┘
   │        │             │
   └────────┴─────────────┘
            │
            ▼
         [END]
```

---

## 🧪 Test Scenarios

### Scenario 1: Happy Path (Approval)
```bash
# Start process
curl -X POST http://localhost:8080/api/door-process/start \
  -H "Content-Type: application/json" \
  -d '{"projectId":"TEST-001","doorType":"Door A","reviewerId":"eng1","designerId":"des1"}'

# Get task
TASK_ID=$(curl -s http://localhost:8080/api/door-process/tasks/eng1 | jq -r '.[0].taskId')

# Approve
curl -X POST http://localhost:8080/api/door-process/tasks/$TASK_ID/complete \
  -H "Content-Type: application/json" \
  -d '{"approvalDecision":"APPROVED","comments":"Looks great!"}'
```

### Scenario 2: Rejection Path
```bash
# Start process
curl -X POST http://localhost:8080/api/door-process/start \
  -H "Content-Type: application/json" \
  -d '{"projectId":"TEST-002","doorType":"Door B","reviewerId":"eng1","designerId":"des1"}'

# Get task and reject
TASK_ID=$(curl -s http://localhost:8080/api/door-process/tasks/eng1 | jq -r '.[0].taskId')
curl -X POST http://localhost:8080/api/door-process/tasks/$TASK_ID/complete \
  -H "Content-Type: application/json" \
  -d '{"approvalDecision":"REJECTED","comments":"Does not meet fire safety standards"}'
```

### Scenario 3: Changes Requested (Loop)
```bash
# Start process
curl -X POST http://localhost:8080/api/door-process/start \
  -H "Content-Type: application/json" \
  -d '{"projectId":"TEST-003","doorType":"Door C","reviewerId":"eng1","designerId":"des1"}'

# Request changes
TASK_ID=$(curl -s http://localhost:8080/api/door-process/tasks/eng1 | jq -r '.[0].taskId')
curl -X POST http://localhost:8080/api/door-process/tasks/$TASK_ID/complete \
  -H "Content-Type: application/json" \
  -d '{"approvalDecision":"CHANGES_NEEDED","comments":"Update dimensions per spec"}'

# Now designer gets a task
curl http://localhost:8080/api/door-process/tasks/des1

# Designer revises and resubmits
TASK_ID2=$(curl -s http://localhost:8080/api/door-process/tasks/des1 | jq -r '.[0].taskId')
# Note: In full implementation, this would loop back to review
```

---

## 🛠️ Troubleshooting

### Port 8080 already in use
```bash
# Find and kill the process
lsof -ti:8080 | xargs kill -9

# Or change port in application.yml
server:
  port: 8081
```

### Maven build fails
```bash
# Clean and rebuild
mvn clean install -DskipTests

# Check Java version
java -version  # Should be 11+
```

### Can't access Camunda Cockpit
```bash
# Check if app is running
curl http://localhost:8080/engine-rest

# Check logs for errors
tail -f camunda-engine/target/*.log
```

---

## 📚 What's Next?

After exploring this POC:

1. **Extend the Process**
   - Add more user tasks
   - Implement email notifications
   - Add document upload

2. **Connect to Real Systems**
   - Integrate with Samrum database
   - Connect to BIM/IFC systems
   - Add authentication (Keycloak)

3. **Deploy to Production**
   - Set up PostgreSQL
   - Configure monitoring
   - Create CI/CD pipeline

---

## 📞 Need Help?

- **Camunda Docs**: https://docs.camunda.org/manual/7.19/
- **Sample Code**: Check the `src/main/java/com/samrum` directory
- **Process Diagram**: Open `door-installation.bpmn` in Camunda Modeler

**Happy Process Orchestrating! 🎉**
