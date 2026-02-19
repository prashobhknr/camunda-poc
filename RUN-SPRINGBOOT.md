# 🚀 Running Camunda 7 POC - Spring Boot Direct Execution

This guide shows how to run the Camunda 7 POC as a **native Spring Boot application** without Docker.

---

## ✅ Prerequisites

### 1. Java 11 or Higher
```bash
java -version
# Should show: java version "11" or higher
```

If you don't have Java 11+:
- **macOS**: `brew install openjdk@11`
- **Windows**: Download from [Adoptium](https://adoptium.net/)
- **Linux**: `sudo apt install openjdk-11-jdk`

### 2. Maven 3.8+
```bash
mvn -version
# Should show: Apache Maven 3.8.x or higher
```

If you don't have Maven:
- **macOS**: `brew install maven`
- **Windows**: Download from [Maven Apache](https://maven.apache.org/download.cgi)
- **Linux**: `sudo apt install maven`

---

## 🏃 Quick Start (3 Steps)

### Step 1: Navigate to Project
```bash
cd /Users/prashobh/.openclaw/workspace/camunda-poc/camunda-engine
```

### Step 2: Build the Application
```bash
mvn clean package -DskipTests
```

**Expected Output:**
```
[INFO] BUILD SUCCESS
[INFO] Total time:  45.123 s
[INFO] Finished at: 2026-02-18T22:45:00Z
```

### Step 3: Run the Application
```bash
mvn spring-boot:run
```

**Wait for this message:**
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::               (v2.7.18)

🚀 Starting Samrum Camunda 7 POC - Door Installation Process
✅ Camunda Engine started successfully!
📊 Access Camunda Cockpit: http://localhost:8080/camunda
📋 Access Tasklist: http://localhost:8080/camunda/app/tasklist
🔧 Access REST API: http://localhost:8080/engine-rest
```

---

## 🌐 Access the Application

Once running, open your browser:

### 1. Camunda Cockpit (Process Monitoring)
**URL**: http://localhost:8080/camunda  
**Login**: 
- Username: `admin`
- Password: `admin`

**What you'll see:**
- Deployed process definitions
- Running process instances
- Process diagrams with active tokens
- Historical data and metrics

### 2. Camunda Tasklist (User Interface)
**URL**: http://localhost:8080/camunda/app/tasklist  
**Login**: `admin` / `admin`

**What you'll see:**
- Tasks assigned to you
- Task forms
- Complete/approve/reject actions

### 3. REST API
**Base URL**: http://localhost:8080/engine-rest  
**Our Custom API**: http://localhost:8080/api/door-process

**Test it:**
```bash
curl http://localhost:8080/engine-rest
```

### 4. H2 Database Console (Debugging)
**URL**: http://localhost:8080/h2-console  
**Settings:**
- JDBC URL: `jdbc:h2:mem:camunda-db`
- Username: `sa`
- Password: (leave blank)

---

## 🧪 Test the POC

### Test 1: Start a Door Installation Process

```bash
curl -X POST http://localhost:8080/api/door-process/start \
  -H "Content-Type: application/json" \
  -d '{
    "projectId": "PROJ-001",
    "doorType": "Fire Door Type A",
    "reviewerId": "engineer1",
    "designerId": "designer1"
  }' | jq .
```

**Expected Response:**
```json
{
  "processInstanceId": "abc123-def456-ghi789",
  "businessKey": "PROJ-001",
  "processDefinitionId": "doorInstallationProcess:1:123",
  "variables": {
    "projectId": "PROJ-001",
    "doorType": "Fire Door Type A",
    "reviewerId": "engineer1",
    "designerId": "designer1",
    "submissionDate": "2026-02-18T22:45:00.000+00:00"
  },
  "currentTask": "Design Review",
  "currentTaskAssignee": "engineer1"
}
```

### Test 2: View Tasks for Engineer

```bash
curl http://localhost:8080/api/door-process/tasks/engineer1 | jq .
```

**Expected Response:**
```json
[
  {
    "taskId": "task123",
    "taskName": "Design Review",
    "processInstanceId": "abc123-def456-ghi789",
    "processDefinitionKey": "doorInstallationProcess",
    "createTime": "2026-02-18T22:45:00.000+00:00",
    "projectId": "PROJ-001",
    "doorType": "Fire Door Type A"
  }
]
```

### Test 3: Complete the Task (Approve)

```bash
# Replace TASK_ID with actual task ID from previous response
curl -X POST http://localhost:8080/api/door-process/tasks/TASK_ID/complete \
  -H "Content-Type: application/json" \
  -d '{
    "approvalDecision": "APPROVED",
    "comments": "Design meets all requirements!"
  }' | jq .
```

**Expected Response:**
```json
{
  "taskId": "task123",
  "completed": true,
  "approvalDecision": "APPROVED",
  "processStatus": "COMPLETED"
}
```

### Test 4: Check Process Status

```bash
curl http://localhost:8080/api/door-process/instances | jq .
```

---

## 🔄 Alternative: Run as JAR File

Instead of `mvn spring-boot:run`, you can build and run as a standalone JAR:

### Build JAR
```bash
mvn clean package -DskipTests
```

### Run JAR
```bash
java -jar target/camunda-poc-engine-1.0.0-SNAPSHOT.jar
```

### Run with Custom Profile
```bash
# Development (H2 database)
java -jar target/camunda-poc-engine-1.0.0-SNAPSHOT.jar --spring.profiles.active=dev

# Production (PostgreSQL database)
java -jar target/camunda-poc-engine-1.0.0-SNAPSHOT.jar --spring.profiles.active=prod
```

---

## ⚙️ Configuration Options

### Change Server Port

Edit `src/main/resources/application.yml`:
```yaml
server:
  port: 8081  # Change from 8080 to 8081
```

Or run with command-line argument:
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"
```

### Enable PostgreSQL (Production)

1. Install PostgreSQL:
   - **macOS**: `brew install postgresql`
   - **Windows**: Download from [PostgreSQL.org](https://www.postgresql.org/download/)
   - **Linux**: `sudo apt install postgresql`

2. Create database:
```bash
createdb samrum_camunda
```

3. Run with production profile:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

Or set environment variables:
```bash
export DB_USERNAME=camunda
export DB_PASSWORD=your_password
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

### Enable Debug Logging

Run with:
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--logging.level.com.samrum=DEBUG"
```

---

## 🛠️ Troubleshooting

### Issue: Port 8080 Already in Use

**Solution 1**: Find and kill the process
```bash
# macOS/Linux
lsof -ti:8080 | xargs kill -9

# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

**Solution 2**: Change port
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"
```

### Issue: Maven Build Fails

**Check Java Version:**
```bash
java -version
# Must be Java 11 or higher
```

**Clean and Rebuild:**
```bash
mvn clean install -DskipTests -U
```

**Clear Maven Cache:**
```bash
rm -rf ~/.m2/repository/org/camunda
mvn clean install
```

### Issue: Can't Access Camunda Cockpit

**Check if App is Running:**
```bash
curl http://localhost:8080/engine-rest
# Should return JSON with engine info
```

**Check Logs:**
```bash
# Look for errors in terminal output
# Common issues:
# - Database connection failed
# - Process deployment failed
# - Port binding error
```

**Verify Process Deployment:**
```bash
curl http://localhost:8080/engine-rest/process-definition?key=doorInstallationProcess
```

### Issue: H2 Console Not Accessible

**Enable in application.yml:**
```yaml
spring:
  h2:
    console:
      enabled: true
      path: /h2-console
```

**Access with correct URL:**
```
http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:camunda-db
Username: sa
Password: (blank)
```

---

## 📊 Monitoring the Application

### View Application Logs

The logs appear in your terminal. Key messages to watch for:

```
✅ Camunda Engine started successfully!
📊 Access Camunda Cockpit: http://localhost:8080/camunda
✅ Door Installation Process deployed successfully!
🚀 Starting door installation process for project: PROJ-001
✅ Process started: abc123-def456-ghi789
🎉 Door Design APPROVED!
✅ Approval notifications sent successfully
```

### Enable File Logging

Add to `application.yml`:
```yaml
logging:
  file:
    name: camunda-poc.log
  level:
    com.samrum: DEBUG
```

Then view logs:
```bash
tail -f camunda-poc.log
```

### Monitor Process Instances via API

```bash
# Get all active instances
curl http://localhost:8080/engine-rest/process-instance?active=true

# Get history (completed instances)
curl http://localhost:8080/engine-rest/history/process-instance?finished=true
```

---

## 🎯 Development Workflow

### 1. Make Code Changes

Edit any Java file in `src/main/java/com/samrum/`

### 2. Rebuild (if needed)

For Java changes:
```bash
mvn clean package -DskipTests
```

For resource changes (BPMN, YAML):
```bash
# Usually auto-reloaded on restart
mvn spring-boot:run
```

### 3. Test Changes

```bash
# Start a new process
curl -X POST http://localhost:8080/api/door-process/start ...

# Check in Cockpit
open http://localhost:8080/camunda
```

### 4. Debug

**Option A: IDE Debugging**
- Import project into IntelliJ IDEA or Eclipse
- Set breakpoints
- Run `CamundaPocApplication` in debug mode

**Option B: Remote Debugging**
```bash
mvn spring-boot:run \
  -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"
```

Then attach debugger from IDE to port 5005.

---

## 📝 Stopping the Application

**In Terminal:** Press `Ctrl+C`

**Verify it stopped:**
```bash
curl http://localhost:8080/engine-rest
# Should fail with "Connection refused"
```

**Note:** H2 in-memory database is cleared on shutdown. For persistent data, use PostgreSQL profile.

---

## 🎓 Next Steps After Running

1. ✅ **Explore Cockpit**: Visualize the process flow
2. ✅ **Complete Tasks**: Use Tasklist UI
3. ✅ **Test API**: Try different scenarios (approve, reject, changes)
4. ✅ **Modify Process**: Edit BPMN in Camunda Modeler
5. ✅ **Add Features**: Implement email notifications, etc.

---

## 📞 Need Help?

- **Logs**: Check terminal output for errors
- **Docs**: See `README.md`, `QUICKSTART.md`, `ARCHITECTURE.md`
- **Camunda Docs**: https://docs.camunda.org/manual/7.19/
- **Code**: Check `src/main/java/com/samrum/` for implementation details

---

**🎉 You're ready to run Camunda 7 POC as a Spring Boot application!**

```bash
cd /Users/prashobh/.openclaw/workspace/camunda-poc/camunda-engine
mvn spring-boot:run
```

**Then open**: http://localhost:8080/camunda
