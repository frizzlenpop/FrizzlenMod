package org.frizzlenpop.frizzlenMod.storage;

import java.util.Date;
import java.util.UUID;

/**
 * Represents a moderation action
 */
public class ModAction {
    private UUID id;
    private String moderator;
    private String target;
    private String action;
    private String reason;
    private String duration;
    private Date timestamp;
    
    /**
     * Creates a new ModAction with default values
     */
    public ModAction() {
        this.id = UUID.randomUUID();
        this.timestamp = new Date();
    }
    
    /**
     * Creates a new ModAction
     * @param id The unique ID of the action
     * @param moderator The name of the moderator who performed the action
     * @param target The name of the target player
     * @param action The type of action
     * @param reason The reason for the action
     * @param duration The duration of the action (if applicable)
     * @param timestamp The timestamp when the action was performed
     */
    public ModAction(UUID id, String moderator, String target, String action, String reason, String duration, Date timestamp) {
        this.id = id;
        this.moderator = moderator;
        this.target = target;
        this.action = action;
        this.reason = reason;
        this.duration = duration;
        this.timestamp = timestamp;
    }
    
    /**
     * Gets the unique ID of the action
     * @return The ID
     */
    public UUID getId() {
        return id;
    }
    
    /**
     * Sets the unique ID of the action
     * @param id The ID
     */
    public void setId(String id) {
        try {
            this.id = UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            // If the string is not a valid UUID, generate a deterministic UUID from the string
            this.id = UUID.nameUUIDFromBytes(id.getBytes());
        }
    }
    
    /**
     * Gets the name of the moderator who performed the action
     * @return The moderator's name
     */
    public String getModerator() {
        return moderator;
    }
    
    /**
     * Sets the name of the moderator who performed the action
     * @param moderator The moderator's name
     */
    public void setModerator(String moderator) {
        this.moderator = moderator;
    }
    
    /**
     * Gets the name of the target player
     * @return The target's name
     */
    public String getTarget() {
        return target;
    }
    
    /**
     * Sets the name of the target player
     * @param target The target's name
     */
    public void setTarget(String target) {
        this.target = target;
    }
    
    /**
     * Gets the type of action
     * @return The action type
     */
    public String getAction() {
        return action;
    }
    
    /**
     * Sets the type of action
     * @param action The action type
     */
    public void setAction(String action) {
        this.action = action;
    }
    
    /**
     * Gets the reason for the action
     * @return The reason
     */
    public String getReason() {
        return reason;
    }
    
    /**
     * Sets the reason for the action
     * @param reason The reason
     */
    public void setReason(String reason) {
        this.reason = reason;
    }
    
    /**
     * Gets the duration of the action (if applicable)
     * @return The duration
     */
    public String getDuration() {
        return duration;
    }
    
    /**
     * Sets the duration of the action
     * @param duration The duration
     */
    public void setDuration(String duration) {
        this.duration = duration;
    }
    
    /**
     * Gets the timestamp when the action was performed
     * @return The timestamp
     */
    public Date getTimestamp() {
        return timestamp;
    }
    
    /**
     * Sets the timestamp when the action was performed
     * @param timestamp The timestamp
     */
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
    
    /**
     * Gets the details of the action (alias for reason)
     * @return The details
     */
    public String getDetails() {
        return reason;
    }
    
    /**
     * Sets the details of the action (alias for reason)
     * @param details The details
     */
    public void setDetails(String details) {
        this.reason = details;
    }

    /**
     * Gets the name of the moderator who performed the action (alias for getModerator)
     * @return The moderator's name
     */
    public String getStaffName() {
        return moderator;
    }
    
    /**
     * Gets the type of action (alias for getAction)
     * @return The action type
     */
    public String getActionType() {
        return action;
    }
    
    /**
     * Gets the name of the target player (alias for getTarget)
     * @return The target's name
     */
    public String getTargetName() {
        return target;
    }

    @Override
    public String toString() {
        return "ModAction{" +
                "id=" + id +
                ", moderator='" + moderator + '\'' +
                ", target='" + target + '\'' +
                ", action='" + action + '\'' +
                ", reason='" + reason + '\'' +
                ", duration='" + duration + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
} 