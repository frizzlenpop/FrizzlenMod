package org.frizzlenpop.frizzlenMod.api.models;

/**
 * Represents a player punishment
 */
public class Punishment {
    private String playerName;
    private String playerUUID;
    private String type; // "BAN", "MUTE", "WARNING", etc.
    private String reason;
    private String moderator;
    private String duration;
    private long timestamp;
    private long expiration;
    private int count; // For warnings
    
    public Punishment() {
        this.timestamp = System.currentTimeMillis();
    }
    
    public String getPlayerName() {
        return playerName;
    }
    
    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
    
    public String getPlayerUUID() {
        return playerUUID;
    }
    
    public void setPlayerUUID(String playerUUID) {
        this.playerUUID = playerUUID;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
    
    public String getModerator() {
        return moderator;
    }
    
    public void setModerator(String moderator) {
        this.moderator = moderator;
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
    
    public long getExpiration() {
        return expiration;
    }
    
    public void setExpiration(long expiration) {
        this.expiration = expiration;
    }
    
    public int getCount() {
        return count;
    }
    
    public void setCount(int count) {
        this.count = count;
    }
} 