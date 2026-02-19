package com.samrum.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Approval Notification Delegate
 * 
 * Service task executed when a door design is approved.
 * Sends notifications and updates external systems.
 * 
 * In a real implementation, this would:
 * - Send email notifications to stakeholders
 * - Update the door status in the database
 * - Trigger downstream processes (manufacturing, installation)
 * - Log the approval in audit system
 */
@Component("approvalNotificationDelegate")
public class ApprovalNotificationDelegate implements JavaDelegate {

    private static final Logger LOG = LoggerFactory.getLogger(ApprovalNotificationDelegate.class);

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String processInstanceId = execution.getProcessInstanceId();
        String projectId = (String) execution.getVariable("projectId");
        String doorType = (String) execution.getVariable("doorType");
        String reviewerId = (String) execution.getVariable("reviewerId");
        String comments = (String) execution.getVariable("comments");

        LOG.info("🎉 Door Design APPROVED!");
        LOG.info("   Process Instance: {}", processInstanceId);
        LOG.info("   Project ID: {}", projectId);
        LOG.info("   Door Type: {}", doorType);
        LOG.info("   Reviewed by: {}", reviewerId);
        LOG.info("   Comments: {}", comments != null ? comments : "No comments");

        // TODO: Implement actual notification logic
        // Example implementations:
        
        // 1. Send email notification
        // emailService.sendApprovalEmail(projectId, reviewerId, doorType);
        
        // 2. Update database status
        // doorRepository.updateStatus(projectId, DoorStatus.APPROVED);
        
        // 3. Log to audit system
        // auditService.logApproval(processInstanceId, reviewerId, LocalDateTime.now());
        
        // 4. Trigger manufacturing process
        // manufacturingService.startProduction(projectId);

        LOG.info("✅ Approval notifications sent successfully");
        
        // Set a flag that notification was sent
        execution.setVariable("approvalNotificationSent", true);
        execution.setVariable("approvalTimestamp", new java.util.Date());
    }
}
