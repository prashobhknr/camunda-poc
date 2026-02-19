package com.samrum.rest;

import com.samrum.dto.DoorProcessRequest;
import com.samrum.dto.TaskCompletionRequest;
import org.camunda.bpm.engine.*;
import org.camunda.bpm.engine.rest.dto.VariableValueDto;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceDto;
import org.camunda.bpm.engine.rest.dto.task.TaskDto;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * REST Controller for Door Installation Process
 * 
 * Provides APIs to:
 * - Start new door installation processes
 * - Get tasks for users
 * - Complete tasks with decisions
 * - Query process instances
 */
@RestController
@RequestMapping("/api/door-process")
@CrossOrigin(origins = "*")
public class DoorProcessController {

    private static final Logger LOG = LoggerFactory.getLogger(DoorProcessController.class);

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private HistoryService historyService;

    /**
     * Start a new door installation process
     * 
     * POST /api/door-process/start
     * Body: {
     *   "projectId": "PROJ-001",
     *   "doorType": "Fire Door A",
     *   "reviewerId": "engineer1",
     *   "designerId": "designer1"
     * }
     */
    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> startProcess(@RequestBody DoorProcessRequest request) {
        LOG.info("🚀 Starting door installation process for project: {}", request.getProjectId());

        Map<String, Object> variables = new HashMap<>();
        variables.put("projectId", request.getProjectId());
        variables.put("doorType", request.getDoorType());
        variables.put("reviewerId", request.getReviewerId());
        variables.put("designerId", request.getDesignerId());
        variables.put("submissionDate", new Date());

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(
            "doorInstallationProcess",
            request.getProjectId(),  // business key
            variables
        );

        LOG.info("✅ Process started: {}", processInstance.getId());

        Map<String, Object> response = new HashMap<>();
        response.put("processInstanceId", processInstance.getId());
        response.put("businessKey", processInstance.getBusinessKey());
        response.put("processDefinitionId", processInstance.getProcessDefinitionId());
        response.put("variables", variables);

        // Get the first task
        Task currentTask = taskService.createTaskQuery()
            .processInstanceId(processInstance.getId())
            .singleResult();

        if (currentTask != null) {
            response.put("currentTask", currentTask.getName());
            response.put("currentTaskAssignee", currentTask.getAssignee());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Get tasks for a specific user
     * 
     * GET /api/door-process/tasks/{userId}
     */
    @GetMapping("/tasks/{userId}")
    public ResponseEntity<List<Map<String, Object>>> getTasksForUser(@PathVariable String userId) {
        LOG.info("📋 Fetching tasks for user: {}", userId);

        List<Task> tasks = taskService.createTaskQuery()
            .taskAssignee(userId)
            .orderByTaskCreateTime()
            .desc()
            .list();

        List<Map<String, Object>> response = new ArrayList<>();
        for (Task task : tasks) {
            Map<String, Object> taskInfo = new HashMap<>();
            taskInfo.put("taskId", task.getId());
            taskInfo.put("taskName", task.getName());
            taskInfo.put("processInstanceId", task.getProcessInstanceId());
            taskInfo.put("processDefinitionKey", task.getProcessDefinitionId());
            taskInfo.put("createTime", task.getCreateTime());
            taskInfo.put("description", task.getDescription());

            // Get process variables
            ProcessInstance pi = runtimeService.createProcessInstanceQuery()
                .processInstanceId(task.getProcessInstanceId())
                .singleResult();

            if (pi != null) {
                taskInfo.put("projectId", runtimeService.getVariable(task.getProcessInstanceId(), "projectId"));
                taskInfo.put("doorType", runtimeService.getVariable(task.getProcessInstanceId(), "doorType"));
            }

            response.add(taskInfo);
        }

        LOG.info("Found {} tasks for user {}", tasks.size(), userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Complete a task with decision
     * 
     * POST /api/door-process/tasks/{taskId}/complete
     * Body: {
     *   "approvalDecision": "APPROVED",  // or "REJECTED" or "CHANGES_NEEDED"
     *   "comments": "Looks good!"
     * }
     */
    @PostMapping("/tasks/{taskId}/complete")
    public ResponseEntity<Map<String, Object>> completeTask(
            @PathVariable String taskId,
            @RequestBody TaskCompletionRequest request) {

        LOG.info("✅ Completing task: {} with decision: {}", taskId, request.getApprovalDecision());

        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Task not found"));
        }

        String processInstanceId = task.getProcessInstanceId();
        Map<String, Object> variables = new HashMap<>();
        variables.put("approvalDecision", request.getApprovalDecision());
        variables.put("comments", request.getComments());
        variables.put("completedAt", new Date());

        taskService.complete(taskId, variables);

        LOG.info("Task completed successfully");

        // Check process status
        ProcessInstance pi = runtimeService.createProcessInstanceQuery()
            .processInstanceId(processInstanceId)
            .singleResult();

        Map<String, Object> response = new HashMap<>();
        response.put("taskId", taskId);
        response.put("completed", true);
        response.put("approvalDecision", request.getApprovalDecision());

        if (pi == null) {
            response.put("processStatus", "COMPLETED");
            LOG.info("Process instance completed: {}", processInstanceId);
        } else {
            response.put("processStatus", "ACTIVE");
            // Get next task
            Task nextTask = taskService.createTaskQuery()
                .processInstanceId(processInstanceId)
                .singleResult();
            if (nextTask != null) {
                response.put("nextTask", nextTask.getName());
                response.put("nextTaskAssignee", nextTask.getAssignee());
            }
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Get process instance details
     * 
     * GET /api/door-process/instance/{processInstanceId}
     */
    @GetMapping("/instance/{processInstanceId}")
    public ResponseEntity<Map<String, Object>> getProcessInstance(
            @PathVariable String processInstanceId) {

        ProcessInstance pi = runtimeService.createProcessInstanceQuery()
            .processInstanceId(processInstanceId)
            .singleResult();

        if (pi == null) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("processInstanceId", pi.getId());
        response.put("businessKey", pi.getBusinessKey());
        response.put("processDefinitionId", pi.getProcessDefinitionId());
        response.put("isActive", true);

        // Get variables
        Map<String, Object> variables = runtimeService.getVariables(processInstanceId);
        response.put("variables", variables);

        // Get current tasks
        List<Task> tasks = taskService.createTaskQuery()
            .processInstanceId(processInstanceId)
            .list();

        List<Map<String, String>> currentTasks = new ArrayList<>();
        for (Task task : tasks) {
            Map<String, String> taskInfo = new HashMap<>();
            taskInfo.put("taskId", task.getId());
            taskInfo.put("taskName", task.getName());
            taskInfo.put("assignee", task.getAssignee());
            currentTasks.add(taskInfo);
        }
        response.put("currentTasks", currentTasks);

        return ResponseEntity.ok(response);
    }

    /**
     * Get all active process instances
     * 
     * GET /api/door-process/instances
     */
    @GetMapping("/instances")
    public ResponseEntity<List<Map<String, Object>>> getAllInstances() {
        List<ProcessInstance> instances = runtimeService.createProcessInstanceQuery()
            .processDefinitionKey("doorInstallationProcess")
            .list();

        List<Map<String, Object>> response = new ArrayList<>();
        for (ProcessInstance pi : instances) {
            Map<String, Object> instanceInfo = new HashMap<>();
            instanceInfo.put("processInstanceId", pi.getId());
            instanceInfo.put("businessKey", pi.getBusinessKey());
            instanceInfo.put("projectId", runtimeService.getVariable(pi.getId(), "projectId"));
            instanceInfo.put("doorType", runtimeService.getVariable(pi.getId(), "doorType"));
            instanceInfo.put("status", "ACTIVE");
            response.add(instanceInfo);
        }

        return ResponseEntity.ok(response);
    }
}
