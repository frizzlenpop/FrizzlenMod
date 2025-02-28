package org.frizzlenpop.frizzlenMod.api.models;

/**
 * Represents a moderation log entry
 */
public class ModLog {
    private String id;
    private String moderator;
    private String action;
    private String target;
    private String reason;
    private String duration;
    private long timestamp;
    
    public ModLog() {
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getModerator() {
        return moderator;
    }
    
    public void setModerator(String moderator) {
        this.moderator = moderator;
    }
    
    public String getAction() {
        return action;
    }
    
    public void setAction(String action) {
        this.action = action;
    }
    
    public String getTarget() {
        return target;
    }
    
    public void setTarget(String target) {
        this.target = target;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
    
    public String getDuration() {
        return duration;
    }
    
    public void setDuration(String duration) {
        this.duration = duration;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
} 