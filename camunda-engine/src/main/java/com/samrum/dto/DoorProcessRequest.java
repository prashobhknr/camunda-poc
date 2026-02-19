package com.samrum.dto;

import javax.validation.constraints.NotBlank;

/**
 * DTO for starting a door installation process
 */
public class DoorProcessRequest {

    @NotBlank(message = "Project ID is required")
    private String projectId;

    @NotBlank(message = "Door type is required")
    private String doorType;

    @NotBlank(message = "Reviewer ID is required")
    private String reviewerId;

    @NotBlank(message = "Designer ID is required")
    private String designerId;

    // Default constructor
    public DoorProcessRequest() {}

    // All-args constructor
    public DoorProcessRequest(String projectId, String doorType, String reviewerId, String designerId) {
        this.projectId = projectId;
        this.doorType = doorType;
        this.reviewerId = reviewerId;
        this.designerId = designerId;
    }

    // Getters and Setters
    public String getProjectId() { return projectId; }
    public void setProjectId(String projectId) { this.projectId = projectId; }

    public String getDoorType() { return doorType; }
    public void setDoorType(String doorType) { this.doorType = doorType; }

    public String getReviewerId() { return reviewerId; }
    public void setReviewerId(String reviewerId) { this.reviewerId = reviewerId; }

    public String getDesignerId() { return designerId; }
    public void setDesignerId(String designerId) { this.designerId = designerId; }
}
