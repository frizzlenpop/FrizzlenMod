package org.frizzlenpop.frizzlenMod.api.models;

import java.util.List;

/**
 * Represents a ban appeal
 */
public class Appeal {
    private String id;
    private String playerUUID;
    private String playerName;
    private String appealText;
    private long submissionTime;
    private AppealStatus status;
    private String contactEmail;
    private String discordTag;
    private String banReason;
    private String adminResponse;
    private List<AppealComment> comments;
    
    public Appeal() {
        this.submissionTime = System.currentTimeMillis();
        this.status = AppealStatus.PENDING;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getPlayerUUID() {
        return playerUUID;
    }
    
    public void setPlayerUUID(String playerUUID) {
        this.playerUUID = playerUUID;
    }
    
    public String getPlayerName() {
        return playerName;
    }
    
    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
    
    public String getAppealText() {
        return appealText;
    }
    
    public void setAppealText(String appealText) {
        this.appealText = appealText;
    }
    
    public long getSubmissionTime() {
        return submissionTime;
    }
    
    public void setSubmissionTime(long submissionTime) {
        this.submissionTime = submissionTime;
    }
    
    public AppealStatus getStatus() {
        return status;
    }
    
    public void setStatus(AppealStatus status) {
        this.status = status;
    }
    
    public String getContactEmail() {
        return contactEmail;
    }
    
    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }
    
    public String getDiscordTag() {
        return discordTag;
    }
    
    public void setDiscordTag(String discordTag) {
        this.discordTag = discordTag;
    }
    
    public String getBanReason() {
        return banReason;
    }
    
    public void setBanReason(String banReason) {
        this.banReason = banReason;
    }
    
    public String getAdminResponse() {
        return adminResponse;
    }
    
    public void setAdminResponse(String adminResponse) {
        this.adminResponse = adminResponse;
    }
    
    public List<AppealComment> getComments() {
        return comments;
    }
    
    public void setComments(List<AppealComment> comments) {
        this.comments = comments;
    }
} 