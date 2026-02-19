package com.eurostep.camunda.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Creates work order in maintenance system.
 * 
 * This delegate:
 * 1. Generates unique work order number
 * 2. Creates work order record
 * 3. Sets priority based on urgency
 * 4. Links to process instance
 * 
 * Input Variables:
 * - doorType: Type of door
 * - location: Installation location
 * - budget: Approved budget
 * - urgency: Urgency level (LOW, MEDIUM, HIGH, CRITICAL)
 * 
 * Output Variables:
 * - workOrderNumber: Generated work order ID
 * - workOrderCreated: Timestamp
 * - assignedPriority: Calculated priority
 */
@Component("createWorkOrderDelegate")
public class CreateWorkOrderDelegate implements JavaDelegate {
    
    private static final Logger LOG = LoggerFactory.getLogger(CreateWorkOrderDelegate.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
    
    @Override
    public void execute(DelegateExecution execution) throws Exception {
        LOG.info("Creating work order for execution: {}", execution.getId());
        
        try {
            // Get input variables
            String doorType = (String) execution.getVariable("doorType");
            String location = (String) execution.getVariable("location");
            Double budget = (Double) execution.getVariable("budget");
            String urgency = (String) execution.getVariable("urgency");
            
            // Generate work order number
            String workOrderNumber = generateWorkOrderNumber();
            LOG.info("Generated work order number: {}", workOrderNumber);
            
            // Determine priority
            String priority = calculatePriority(urgency, budget);
            LOG.info("Assigned priority: {}", priority);
            
            // Create work order (placeholder - would integrate with maintenance system)
            createWorkOrderInSystem(workOrderNumber, execution);
            
            // Set output variables
            execution.setVariable("workOrderNumber", workOrderNumber);
            execution.setVariable("workOrderCreated", LocalDateTime.now().format(formatter));
            execution.setVariable("assignedPriority", priority);
            
            LOG.info("Work order {} created successfully", workOrderNumber);
            
        } catch (Exception e) {
            LOG.error("Error creating work order", e);
            throw e;
        }
    }
    
    /**
     * Generates unique work order number
     * Format: WO-YYYYMMDD-HHMMSS-XXXX
     */
    private String generateWorkOrderNumber() {
        String timestamp = LocalDateTime.now().format(formatter);
        String uniqueId = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        return String.format("WO-%s-%s", timestamp, uniqueId);
    }
    
    /**
     * Calculates priority based on urgency and budget
     */
    private String calculatePriority(String urgency, Double budget) {
        // Default urgency
        if (urgency == null) {
            urgency = "MEDIUM";
        }
        
        // Budget-based escalation
        boolean highBudget = budget != null && budget > 25000;
        
        switch (urgency.toUpperCase()) {
            case "CRITICAL":
                return "P1_CRITICAL";
            case "HIGH":
                return highBudget ? "P1_CRITICAL" : "P2_HIGH";
            case "MEDIUM":
                return highBudget ? "P2_HIGH" : "P3_MEDIUM";
            case "LOW":
                return highBudget ? "P3_MEDIUM" : "P4_LOW";
            default:
                return "P3_MEDIUM";
        }
    }
    
    /**
     * Creates work order in external maintenance system
     * TODO: Integrate with actual maintenance system API
     */
    private void createWorkOrderInSystem(String workOrderNumber, DelegateExecution execution) {
        // Placeholder implementation
        // In production, this would:
        // 1. Call maintenance system REST API
        // 2. Create work order record
        // 3. Store correlation ID
        
        LOG.info("Creating work order in maintenance system: {}", workOrderNumber);
        
        // Simulate API call delay
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Log work order details
        LOG.info("Work Order Details:");
        LOG.info("  - Number: {}", workOrderNumber);
        LOG.info("  - Process Instance: {}", execution.getProcessInstanceId());
        LOG.info("  - Door Type: {}", execution.getVariable("doorType"));
        LOG.info("  - Location: {}", execution.getVariable("location"));
    }
}
