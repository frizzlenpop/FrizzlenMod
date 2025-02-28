package org.frizzlenpop.frizzlenMod.storage;

import java.util.Date;
import java.util.UUID;

/**
 * Represents a moderation action performed by staff.
 */
public class ModAction {
    private final UUID id;
    private final String staffName;
    private final String targetName;
    private final String actionType;
    private final String reason;
    private final String duration;
    private final Date timestamp;

    /**
     * Creates a new ModAction.
     *
     * @param id Unique identifier for this action
     * @param staffName Name of the staff member who performed the action
     * @param targetName Name of the player who was the target of the action
     * @param actionType Type of action (e.g., "Ban", "Kick", "Mute")
     * @param reason Reason for the action
     * @param duration Duration of the action (for temporary actions)
     * @param timestamp Time when the action was performed
     */
    public ModAction(UUID id, String staffName, String targetName, String actionType, 
                     String reason, String duration, Date timestamp) {
        this.id = id;
        this.staffName = staffName;
        this.targetName = targetName;
        this.actionType = actionType;
        this.reason = reason;
        this.duration = duration;
        this.timestamp = timestamp;
    }

    /**
     * Gets the unique ID of this action.
     *
     * @return The UUID of this action
     */
    public UUID getId() {
        return id;
    }

    /**
     * Gets the name of the staff member who performed the action.
     *
     * @return The staff member's name
     */
    public String getStaffName() {
        return staffName;
    }

    /**
     * Gets the name of the player who was the target of the action.
     *
     * @return The target player's name
     */
    public String getTargetName() {
        return targetName;
    }

    /**
     * Gets the type of action that was performed.
     *
     * @return The action type
     */
    public String getActionType() {
        return actionType;
    }

    /**
     * Gets the reason for the action.
     *
     * @return The reason, or null if no reason was provided
     */
    public String getReason() {
        return reason;
    }

    /**
     * Gets the duration of the action (for temporary actions).
     *
     * @return The duration string, or null if the action is permanent
     */
    public String getDuration() {
        return duration;
    }

    /**
     * Gets the timestamp when the action was performed.
     *
     * @return The timestamp
     */
    public Date getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "ModAction{" +
                "id=" + id +
                ", staffName='" + staffName + '\'' +
                ", targetName='" + targetName + '\'' +
                ", actionType='" + actionType + '\'' +
                ", reason='" + reason + '\'' +
                ", duration='" + duration + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
} 