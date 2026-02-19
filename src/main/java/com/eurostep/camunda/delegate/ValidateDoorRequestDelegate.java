package com.eurostep.camunda.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Validates door request automatically.
 * 
 * Business Rules:
 * 1. All required fields must be present
 * 2. Budget must be available
 * 3. Technical specifications must be valid
 * 4. Location must exist in building registry
 * 
 * Input Variables:
 * - doorType: Type of door (single, double, fire-rated, etc.)
 * - location: Installation location
 * - budget: Estimated cost
 * - requestor: Person requesting the door
 * 
 * Output Variables:
 * - valid: Boolean indicating validation result
 * - rejectionReason: Reason for rejection (if invalid)
 */
@Component("validateDoorRequestDelegate")
public class ValidateDoorRequestDelegate implements JavaDelegate {
    
    private static final Logger LOG = LoggerFactory.getLogger(ValidateDoorRequestDelegate.class);
    
    @Override
    public void execute(DelegateExecution execution) throws Exception {
        LOG.info("Validating door request for execution: {}", execution.getId());
        
        try {
            // Get input variables
            String doorType = (String) execution.getVariable("doorType");
            String location = (String) execution.getVariable("location");
            Double budget = (Double) execution.getVariable("budget");
            String requestor = (String) execution.getVariable("requestor");
            
            // Validation checks
            boolean isValid = true;
            String rejectionReason = "";
            
            // Check 1: Required fields
            if (doorType == null || doorType.trim().isEmpty()) {
                isValid = false;
                rejectionReason = "Door type is required";
            } else if (location == null || location.trim().isEmpty()) {
                isValid = false;
                rejectionReason = "Installation location is required";
            } else if (requestor == null || requestor.trim().isEmpty()) {
                isValid = false;
                rejectionReason = "Requestor information is required";
            }
            
            // Check 2: Budget validation
            if (isValid && (budget == null || budget <= 0)) {
                isValid = false;
                rejectionReason = "Valid budget estimate is required";
            } else if (isValid && budget > 50000) {
                // High budget requires additional approval
                LOG.info("High budget request detected: {}", budget);
                execution.setVariable("requiresAdditionalApproval", true);
            }
            
            // Check 3: Door type validation
            if (isValid) {
                if (!isValidDoorType(doorType)) {
                    isValid = false;
                    rejectionReason = "Invalid door type: " + doorType;
                }
            }
            
            // Check 4: Location validation (would check against building registry)
            if (isValid) {
                if (!locationExists(location)) {
                    isValid = false;
                    rejectionReason = "Location not found in building registry: " + location;
                }
            }
            
            // Set output variables
            execution.setVariable("valid", isValid);
            if (!isValid) {
                execution.setVariable("rejectionReason", rejectionReason);
                LOG.warn("Door request validation failed: {}", rejectionReason);
            } else {
                LOG.info("Door request validation successful");
            }
            
        } catch (Exception e) {
            LOG.error("Error during door request validation", e);
            execution.setVariable("valid", false);
            execution.setVariable("rejectionReason", "System error during validation: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Validates if the door type is supported
     */
    private boolean isValidDoorType(String doorType) {
        String[] validTypes = {
            "SINGLE_STANDARD",
            "DOUBLE_STANDARD", 
            "FIRE_RATED_SINGLE",
            "FIRE_RATED_DOUBLE",
            "SECURITY_DOOR",
            "ACCESS_CONTROL_DOOR",
            "EMERGENCY_EXIT"
        };
        
        for (String validType : validTypes) {
            if (validType.equalsIgnoreCase(doorType)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Checks if location exists in building registry
     * TODO: Integrate with actual building registry system
     */
    private boolean locationExists(String location) {
        // Placeholder implementation
        // In production, this would call the asset management system
        return location != null && !location.trim().isEmpty();
    }
}
