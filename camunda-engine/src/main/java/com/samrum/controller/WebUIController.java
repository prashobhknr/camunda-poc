package com.samrum.controller;

import com.samrum.dto.DoorProcessRequest;
import com.samrum.dto.TaskCompletionRequest;
import org.camunda.bpm.engine.*;
import org.camunda.bpm.engine.history.*;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

/**
 * Web UI Controller for Door Installation Process
 * Provides Thymeleaf-based forms for manual tasks
 */
@Controller
@RequestMapping("/ui")
public class WebUIController {

    private static final Logger LOG = LoggerFactory.getLogger(WebUIController.class);

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private HistoryService historyService;

    /**
     * Dashboard - Main landing page
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Get statistics
        long activeProcesses = runtimeService.createProcessInstanceQuery()
                .processDefinitionKey("doorInstallationProcess")
                .count();

        List<Task> allTasks = taskService.createTaskQuery()
                .processDefinitionKey("doorInstallationProcess")
                .list();

        long totalTasks = allTasks.size();
        long pendingTasks = allTasks.stream().filter(t -> t.getAssignee() == null).count();
        long assignedTasks = totalTasks - pendingTasks;

        model.addAttribute("activeProcesses", activeProcesses);
        model.addAttribute("totalTasks", totalTasks);
        model.addAttribute("pendingTasks", pendingTasks);
        model.addAttribute("assignedTasks", assignedTasks);

        // Get recent process instances
        List<ProcessInstance> instances = runtimeService.createProcessInstanceQuery()
                .processDefinitionKey("doorInstallationProcess")
                .orderByProcessInstanceId()
                .asc()
                .listPage(0, 10);

        List<Map<String, Object>> recentProcesses = new ArrayList<>();
        for (ProcessInstance pi : instances) {
            Map<String, Object> procInfo = new HashMap<>();
            procInfo.put("id", pi.getId());
            procInfo.put("businessKey", pi.getBusinessKey());
            procInfo.put("projectId", runtimeService.getVariable(pi.getId(), "projectId"));
            procInfo.put("doorType", runtimeService.getVariable(pi.getId(), "doorType"));
            recentProcesses.add(procInfo);
        }
        model.addAttribute("recentProcesses", recentProcesses);

        return "dashboard";
    }

    /**
     * Start new process form
     */
    @GetMapping("/start-process")
    public String startProcessForm(Model model) {
        model.addAttribute("request", new DoorProcessRequest());
        return "start-process";
    }

    /**
     * Start new process submission
     */
    @PostMapping("/start-process")
    public String startProcessSubmit(@ModelAttribute DoorProcessRequest request,
                                     RedirectAttributes redirectAttributes) {
        LOG.info("🚀 Starting door installation process for project: {}", request.getProjectId());

        Map<String, Object> variables = new HashMap<>();
        variables.put("projectId", request.getProjectId());
        variables.put("doorType", request.getDoorType());
        variables.put("reviewerId", request.getReviewerId());
        variables.put("designerId", request.getDesignerId());
        variables.put("submissionDate", new Date());

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(
                "doorInstallationProcess",
                request.getProjectId(),
                variables
        );

        LOG.info("✅ Process started: {}", processInstance.getId());

        redirectAttributes.addFlashAttribute("success", true);
        redirectAttributes.addFlashAttribute("processInstanceId", processInstance.getId());
        redirectAttributes.addFlashAttribute("projectId", request.getProjectId());

        return "redirect:/ui/process-detail/" + processInstance.getId();
    }

    /**
     * Task list for a user
     */
    @GetMapping("/tasks")
    public String taskList(@RequestParam(required = false) String userId, Model model) {
        List<Task> tasks;

        if (userId != null && !userId.isEmpty()) {
            tasks = taskService.createTaskQuery()
                    .taskAssignee(userId)
                    .orderByTaskCreateTime()
                    .desc()
                    .list();
            model.addAttribute("userId", userId);
        } else {
            tasks = taskService.createTaskQuery()
                    .processDefinitionKey("doorInstallationProcess")
                    .orderByTaskCreateTime()
                    .desc()
                    .list();
        }

        List<Map<String, Object>> taskList = new ArrayList<>();
        for (Task task : tasks) {
            Map<String, Object> taskInfo = new HashMap<>();
            taskInfo.put("taskId", task.getId());
            taskInfo.put("taskName", task.getName());
            taskInfo.put("assignee", task.getAssignee());
            taskInfo.put("createTime", task.getCreateTime());
            taskInfo.put("description", task.getDescription());
            taskInfo.put("processInstanceId", task.getProcessInstanceId());

            // Get process variables
            String projectId = (String) runtimeService.getVariable(task.getProcessInstanceId(), "projectId");
            String doorType = (String) runtimeService.getVariable(task.getProcessInstanceId(), "doorType");
            taskInfo.put("projectId", projectId);
            taskInfo.put("doorType", doorType);

            taskList.add(taskInfo);
        }

        model.addAttribute("tasks", taskList);
        return "task-list";
    }

    /**
     * Complete task form
     */
    @GetMapping("/task/{taskId}/complete")
    public String completeTaskForm(@PathVariable String taskId, Model model) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            return "redirect:/ui/tasks?error=notfound";
        }

        String processInstanceId = task.getProcessInstanceId();
        String projectId = (String) runtimeService.getVariable(processInstanceId, "projectId");
        String doorType = (String) runtimeService.getVariable(processInstanceId, "doorType");

        model.addAttribute("task", task);
        model.addAttribute("projectId", projectId);
        model.addAttribute("doorType", doorType);
        model.addAttribute("request", new TaskCompletionRequest());

        return "complete-task";
    }

    /**
     * Complete task submission
     */
    @PostMapping("/task/{taskId}/complete")
    public String completeTaskSubmit(@PathVariable String taskId,
                                     @ModelAttribute TaskCompletionRequest request,
                                     RedirectAttributes redirectAttributes) {
        LOG.info("✅ Completing task: {} with decision: {}", taskId, request.getApprovalDecision());

        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            redirectAttributes.addFlashAttribute("error", "Task not found");
            return "redirect:/ui/tasks";
        }

        String processInstanceId = task.getProcessInstanceId();

        Map<String, Object> variables = new HashMap<>();
        variables.put("approvalDecision", request.getApprovalDecision());
        variables.put("comments", request.getComments());
        variables.put("completedAt", new Date());

        taskService.complete(taskId, variables);

        LOG.info("Task completed successfully");

        redirectAttributes.addFlashAttribute("success", true);
        redirectAttributes.addFlashAttribute("completedTaskId", taskId);

        return "redirect:/ui/process-detail/" + processInstanceId;
    }

    /**
     * Process instance detail view
     */
    @GetMapping("/process-detail/{processInstanceId}")
public String processDetail(@PathVariable String processInstanceId, Model model) {
// First try to get from runtime (active processes)
ProcessInstance pi = runtimeService.createProcessInstanceQuery()
.processInstanceId(processInstanceId)
.singleResult();

boolean isCompleted = false;
if (pi == null) {
// If not in runtime, check history (completed processes)
org.camunda.bpm.engine.history.HistoricProcessInstance historicPi = 
historyService.createHistoricProcessInstanceQuery()
.processInstanceId(processInstanceId)
.singleResult();

if (historicPi == null) {
return "redirect:/ui/dashboard?error=notfound";
}

// Use historic process instance data
model.addAttribute("processInstance", null);
model.addAttribute("historicProcessInstance", historicPi);
model.addAttribute("processInstanceId", processInstanceId);
model.addAttribute("businessKey", historicPi.getBusinessKey());
model.addAttribute("isCompleted", true);

// Get historic variables
Map<String, Object> variables = new HashMap<>();
org.camunda.bpm.engine.history.HistoricVariableInstanceQuery varQuery = 
historyService.createHistoricVariableInstanceQuery()
.processInstanceId(processInstanceId);
for (org.camunda.bpm.engine.history.HistoricVariableInstance var : varQuery.list()) {
variables.put(var.getVariableName(), var.getValue());
}
model.addAttribute("variables", variables);

// Get historic tasks
List<org.camunda.bpm.engine.history.HistoricTaskInstance> historicTasks = 
historyService.createHistoricTaskInstanceQuery()
.processInstanceId(processInstanceId)
.orderByHistoricTaskInstanceEndTime()
.desc()
.list();
model.addAttribute("currentTasks", new ArrayList<>());
model.addAttribute("historicTasks", historicTasks);
model.addAttribute("processDefinitionId", historicPi.getProcessDefinitionId());

return "process-detail";
}

// Active process - original logic
model.addAttribute("processInstance", pi);
model.addAttribute("processInstanceId", processInstanceId);
model.addAttribute("businessKey", pi.getBusinessKey());
model.addAttribute("isCompleted", false);

// Get variables
Map<String, Object> variables = runtimeService.getVariables(processInstanceId);
model.addAttribute("variables", variables);

// Get current tasks
List<Task> tasks = taskService.createTaskQuery()
.processInstanceId(processInstanceId)
.list();
model.addAttribute("currentTasks", tasks);
model.addAttribute("historicTasks", new ArrayList<>());
model.addAttribute("processDefinitionId", pi.getProcessDefinitionId());
return "process-detail";
}

    /**
     * All process instances (active + completed)
     */
    @GetMapping("/processes")
    public String allProcesses(Model model) {
        List<Map<String, Object>> processList = new ArrayList<>();

        // Get active processes
        List<ProcessInstance> activeInstances = runtimeService.createProcessInstanceQuery()
                .processDefinitionKey("doorInstallationProcess")
                .orderByProcessInstanceId()
                .asc()
                .list();

        for (ProcessInstance pi : activeInstances) {
            Map<String, Object> procInfo = new HashMap<>();
            procInfo.put("id", pi.getId());
            procInfo.put("businessKey", pi.getBusinessKey());
            procInfo.put("projectId", runtimeService.getVariable(pi.getId(), "projectId"));
            procInfo.put("doorType", runtimeService.getVariable(pi.getId(), "doorType"));
            procInfo.put("status", "ACTIVE");
            procInfo.put("startTime", null);
            procInfo.put("endTime", null);
            procInfo.put("duration", null);

            // Get current tasks count
            long taskCount = taskService.createTaskQuery()
                    .processInstanceId(pi.getId())
                    .count();
            procInfo.put("taskCount", taskCount);

            processList.add(procInfo);
        }

        // Get completed processes from history
        List<HistoricProcessInstance> historicInstances = historyService.createHistoricProcessInstanceQuery()
                .processDefinitionKey("doorInstallationProcess")
                .finished()
                .orderByProcessInstanceEndTime()
                .desc()
                .list();

        for (HistoricProcessInstance hpi : historicInstances) {
            Map<String, Object> procInfo = new HashMap<>();
            procInfo.put("id", hpi.getId());
            procInfo.put("businessKey", hpi.getBusinessKey());
            
            // Get variables from history
            HistoricVariableInstanceQuery varQuery = historyService.createHistoricVariableInstanceQuery()
                    .processInstanceId(hpi.getId());
            for (HistoricVariableInstance var : varQuery.list()) {
                if ("projectId".equals(var.getVariableName())) {
                    procInfo.put("projectId", var.getValue());
                }
                if ("doorType".equals(var.getVariableName())) {
                    procInfo.put("doorType", var.getValue());
                }
            }
            
            procInfo.put("status", "COMPLETED");
            procInfo.put("startTime", hpi.getStartTime());
            procInfo.put("endTime", hpi.getEndTime());
            procInfo.put("duration", hpi.getDurationInMillis());
            procInfo.put("taskCount", 0);

            processList.add(procInfo);
        }

        model.addAttribute("processes", processList);
        return "process-list";
    }

    /**
     * Home redirect
     */
    @GetMapping("/")
    public String home() {
        return "redirect:/ui/dashboard";
    }
}
