package com.samrum.dto;

import javax.validation.constraints.NotBlank;

/**
 * DTO for completing a task with approval decision
 */
public class TaskCompletionRequest {

    @NotBlank(message = "Approval decision is required")
    private String approvalDecision;  // Values: APPROVED, REJECTED, CHANGES_NEEDED

    private String comments;

    // Default constructor
    public TaskCompletionRequest() {}

    // All-args constructor
    public TaskCompletionRequest(String approvalDecision, String comments) {
        this.approvalDecision = approvalDecision;
        this.comments = comments;
    }

    // Getters and Setters
    public String getApprovalDecision() { return approvalDecision; }
    public void setApprovalDecision(String approvalDecision) { this.approvalDecision = approvalDecision; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
}
