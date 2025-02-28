package org.frizzlenpop.frizzlenMod.api.models;

/**
 * Represents a staff user who can access the web panel
 */
public class User {
    private String username;
    private String passwordHash;
    private String role; // "ADMIN", "MODERATOR", etc.
    private String minecraftUUID; // Optional link to Minecraft account
    private long lastLogin;
    private boolean active;
    
    public User() {
    }
    
    public User(String username, String passwordHash, String role) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.active = true;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPasswordHash() {
        return passwordHash;
    }
    
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public String getMinecraftUUID() {
        return minecraftUUID;
    }
    
    public void setMinecraftUUID(String minecraftUUID) {
        this.minecraftUUID = minecraftUUID;
    }
    
    public long getLastLogin() {
        return lastLogin;
    }
    
    public void setLastLogin(long lastLogin) {
        this.lastLogin = lastLogin;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    /**
     * Checks if this user has admin privileges
     * @return true if the user is an admin
     */
    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }
} 