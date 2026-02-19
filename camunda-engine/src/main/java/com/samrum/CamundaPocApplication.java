package com.samrum;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Samrum Camunda 7 POC Application
 * 
 * Main entry point for the Door Installation Process demonstration.
 * This application migrates a subset of Samrum's Bizagi processes to Camunda 7.
 * 
 * @author Code Agent (GLM-5)
 * @version 1.0.0
 */
@SpringBootApplication
public class CamundaPocApplication {

    private static final Logger LOG = LoggerFactory.getLogger(CamundaPocApplication.class);

    public static void main(String[] args) {
        LOG.info("🚀 Starting Samrum Camunda 7 POC - Door Installation Process");
        SpringApplication.run(CamundaPocApplication.class, args);
    }

    /**
     * Demo runner - automatically starts a sample process on startup
     * Remove @Bean comment to enable auto-start demo
     */
    @Bean
    public CommandLineRunner demo(ProcessEngine processEngine) {
        return args -> {
            LOG.info("✅ Camunda Engine started successfully!");
            LOG.info("📊 Access Camunda Cockpit: http://localhost:8080/camunda");
            LOG.info("📋 Access Tasklist: http://localhost:8080/camunda/app/tasklist");
            LOG.info("🔧 Access REST API: http://localhost:8080/engine-rest");
            
            RepositoryService repositoryService = processEngine.getRepositoryService();
            RuntimeService runtimeService = processEngine.getRuntimeService();
            TaskService taskService = processEngine.getTaskService();

            // Check if process definition is deployed
            long processCount = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey("doorInstallationProcess")
                .count();

            if (processCount > 0) {
                LOG.info("✅ Door Installation Process deployed successfully!");
                
                // Optional: Start a demo process instance
                // Uncomment to auto-start demo on every startup
                /*
                LOG.info("🎯 Starting demo process instance...");
                
                Map<String, Object> variables = new HashMap<>();
                variables.put("projectId", "DEMO-001");
                variables.put("doorType", "Fire Door Type A");
                variables.put("reviewerId", "engineer1");
                variables.put("designerId", "designer1");
                
                ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(
                    "doorInstallationProcess", 
                    variables
                );
                
                LOG.info("📊 Process Instance ID: " + processInstance.getId());
                LOG.info("📊 Business Key: " + processInstance.getBusinessKey());
                
                // Show active tasks
                List<Task> tasks = taskService.createTaskQuery()
                    .processInstanceId(processInstance.getId())
                    .list();
                
                LOG.info("📋 Active Tasks: " + tasks.size());
                for (Task task : tasks) {
                    LOG.info("  - Task: " + task.getName() + " (Assignee: " + task.getAssignee() + ")");
                }
                */
                
                LOG.info("💡 To start a process, use the REST API:");
                LOG.info("   POST http://localhost:8080/engine-rest/process-instance/key/doorInstallationProcess/start");
            } else {
                LOG.error("❌ Door Installation Process NOT found!");
            }
        };
    }
}
