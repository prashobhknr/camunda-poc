package com.samrum.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Rejection Notification Delegate
 * 
 * Service task executed when a door design is rejected.
 * Sends notifications and logs the rejection.
 * 
 * In a real implementation, this would:
 * - Send email notifications to designer and stakeholders
 * - Update the door status in the database
 * - Log the rejection with reasons in audit system
 * - Possibly trigger a redesign workflow
 */
@Component("rejectionNotificationDelegate")
public class RejectionNotificationDelegate implements JavaDelegate {

    private static final Logger LOG = LoggerFactory.getLogger(RejectionNotificationDelegate.class);

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String processInstanceId = execution.getProcessInstanceId();
        String projectId = (String) execution.getVariable("projectId");
        String doorType = (String) execution.getVariable("doorType");
        String reviewerId = (String) execution.getVariable("reviewerId");
        String designerId = (String) execution.getVariable("designerId");
        String comments = (String) execution.getVariable("comments");

        LOG.info("❌ Door Design REJECTED!");
        LOG.info("   Process Instance: {}", processInstanceId);
        LOG.info("   Project ID: {}", projectId);
        LOG.info("   Door Type: {}", doorType);
        LOG.info("   Reviewed by: {}", reviewerId);
        LOG.info("   Designer: {}", designerId);
        LOG.info("   Comments: {}", comments != null ? comments : "No comments provided");

        // TODO: Implement actual notification logic
        // Example implementations:
        
        // 1. Send email notification to designer
        // emailService.sendRejectionEmail(projectId, designerId, reviewerId, comments);
        
        // 2. Update database status
        // doorRepository.updateStatus(projectId, DoorStatus.REJECTED);
        
        // 3. Log to audit system with rejection reason
        // auditService.logRejection(processInstanceId, reviewerId, comments, LocalDateTime.now());
        
        // 4. Create task in project management system
        // projectService.createRedesignTask(projectId, comments);

        LOG.info("✅ Rejection notifications sent successfully");
        
        // Set a flag that notification was sent
        execution.setVariable("rejectionNotificationSent", true);
        execution.setVariable("rejectionTimestamp", new java.util.Date());
    }
}
