package org.frizzlenpop.frizzlenMod.api.controllers;

import com.google.gson.Gson;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.frizzlenpop.frizzlenMod.FrizzlenMod;
import org.frizzlenpop.frizzlenMod.api.models.Appeal;
import org.frizzlenpop.frizzlenMod.api.models.AppealComment;
import org.frizzlenpop.frizzlenMod.api.models.AppealStatus;
import spark.Request;
import spark.Response;

import java.util.*;
import java.util.logging.Level;

/**
 * Handles all appeal-related API endpoints
 */
public class AppealsController {
    private final FrizzlenMod plugin;
    private final Gson gson;
    private final FileConfiguration appealsConfig;
    
    public AppealsController(FrizzlenMod plugin, Gson gson) {
        this.plugin = plugin;
        this.gson = gson;
        this.appealsConfig = plugin.getStorageManager().createOrGetConfig("appeals");
    }
    
    /**
     * Submit a new ban appeal
     */
    public Object submitAppeal(Request request, Response response) {
        try {
            // Parse request body
            Appeal appeal = gson.fromJson(request.body(), Appeal.class);
            
            // Validate required fields
            if (appeal.getPlayerUUID() == null || appeal.getPlayerName() == null || appeal.getAppealText() == null) {
                response.status(400);
                return gson.toJson(Map.of("error", "Missing required fields"));
            }
            
            // Check if the player exists
            OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(appeal.getPlayerUUID()));
            if (player == null || !player.hasPlayedBefore()) {
                response.status(400);
                return gson.toJson(Map.of("error", "Player not found"));
            }
            
            // Check if the player is actually banned
            if (!Bukkit.getBanList(org.bukkit.BanList.Type.NAME).isBanned(player.getName())) {
                response.status(400);
                return gson.toJson(Map.of("error", "Player is not banned"));
            }
            
            // Check for existing appeal cooldown
            String cooldownKey = "cooldowns." + appeal.getPlayerUUID();
            if (appealsConfig.contains(cooldownKey)) {
                long cooldownTime = appealsConfig.getLong(cooldownKey);
                long currentTime = System.currentTimeMillis();
                
                // Check if cooldown is still active (default 24 hours)
                int cooldownHours = plugin.getConfig().getInt("web-api.appeal-cooldown-hours", 24);
                if (currentTime - cooldownTime < cooldownHours * 60 * 60 * 1000) {
                    response.status(429);
                    return gson.toJson(Map.of("error", "You can only submit one appeal every " + cooldownHours + " hours"));
                }
            }
            
            // Generate appeal ID
            String appealId = UUID.randomUUID().toString();
            
            // Create the appeal record
            appeal.setId(appealId);
            appeal.setSubmissionTime(System.currentTimeMillis());
            appeal.setStatus(AppealStatus.PENDING);
            appeal.setComments(new ArrayList<>());
            
            // Save appeal to config
            String appealKey = "appeals." + appealId;
            appealsConfig.set(appealKey + ".playerUUID", appeal.getPlayerUUID());
            appealsConfig.set(appealKey + ".playerName", appeal.getPlayerName());
            appealsConfig.set(appealKey + ".appealText", appeal.getAppealText());
            appealsConfig.set(appealKey + ".submissionTime", appeal.getSubmissionTime());
            appealsConfig.set(appealKey + ".status", appeal.getStatus().toString());
            appealsConfig.set(appealKey + ".banReason", Bukkit.getBanList(org.bukkit.BanList.Type.NAME).getBanEntry(player.getName()).getReason());
            
            // Save contact info if provided
            if (appeal.getContactEmail() != null) {
                appealsConfig.set(appealKey + ".contactEmail", appeal.getContactEmail());
            }
            if (appeal.getDiscordTag() != null) {
                appealsConfig.set(appealKey + ".discordTag", appeal.getDiscordTag());
            }
            
            // Set cooldown
            appealsConfig.set(cooldownKey, System.currentTimeMillis());
            
            // Save config
            plugin.getStorageManager().saveConfig(appealsConfig, "appeals");
            
            // Log the appeal
            plugin.getLogger().info("New ban appeal submitted by " + appeal.getPlayerName() + " (ID: " + appealId + ")");
            
            // Return success response
            response.status(201);
            return gson.toJson(Map.of(
                    "success", true,
                    "message", "Appeal submitted successfully",
                    "appealId", appealId
            ));
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error processing appeal submission: " + e.getMessage(), e);
            response.status(500);
            return gson.toJson(Map.of("error", "Internal server error"));
        }
    }
    
    /**
     * Get the status of an appeal by player UUID
     */
    public Object getAppealStatus(Request request, Response response) {
        try {
            String playerUuid = request.params(":uuid");
            
            // Find the latest appeal for this player
            Appeal latestAppeal = null;
            long latestTime = 0;
            
            ConfigurationSection appealsSection = appealsConfig.getConfigurationSection("appeals");
            if (appealsSection != null) {
                for (String appealId : appealsSection.getKeys(false)) {
                    String appealUuid = appealsSection.getString(appealId + ".playerUUID");
                    
                    if (playerUuid.equalsIgnoreCase(appealUuid)) {
                        long submissionTime = appealsSection.getLong(appealId + ".submissionTime");
                        
                        if (submissionTime > latestTime) {
                            latestTime = submissionTime;
                            
                            Appeal appeal = new Appeal();
                            appeal.setId(appealId);
                            appeal.setPlayerUUID(appealUuid);
                            appeal.setPlayerName(appealsSection.getString(appealId + ".playerName"));
                            appeal.setSubmissionTime(submissionTime);
                            appeal.setStatus(AppealStatus.valueOf(appealsSection.getString(appealId + ".status")));
                            
                            latestAppeal = appeal;
                        }
                    }
                }
            }
            
            if (latestAppeal == null) {
                response.status(404);
                return gson.toJson(Map.of("error", "No appeals found for this player"));
            }
            
            return gson.toJson(latestAppeal);
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error getting appeal status: " + e.getMessage(), e);
            response.status(500);
            return gson.toJson(Map.of("error", "Internal server error"));
        }
    }
    
    /**
     * Get all appeals (admin)
     */
    public Object getAllAppeals(Request request, Response response) {
        try {
            List<Appeal> appeals = new ArrayList<>();
            
            ConfigurationSection appealsSection = appealsConfig.getConfigurationSection("appeals");
            if (appealsSection != null) {
                for (String appealId : appealsSection.getKeys(false)) {
                    Appeal appeal = new Appeal();
                    appeal.setId(appealId);
                    appeal.setPlayerUUID(appealsSection.getString(appealId + ".playerUUID"));
                    appeal.setPlayerName(appealsSection.getString(appealId + ".playerName"));
                    appeal.setAppealText(appealsSection.getString(appealId + ".appealText"));
                    appeal.setSubmissionTime(appealsSection.getLong(appealId + ".submissionTime"));
                    appeal.setStatus(AppealStatus.valueOf(appealsSection.getString(appealId + ".status")));
                    
                    // Add optional fields if they exist
                    if (appealsSection.contains(appealId + ".contactEmail")) {
                        appeal.setContactEmail(appealsSection.getString(appealId + ".contactEmail"));
                    }
                    if (appealsSection.contains(appealId + ".discordTag")) {
                        appeal.setDiscordTag(appealsSection.getString(appealId + ".discordTag"));
                    }
                    if (appealsSection.contains(appealId + ".banReason")) {
                        appeal.setBanReason(appealsSection.getString(appealId + ".banReason"));
                    }
                    if (appealsSection.contains(appealId + ".adminResponse")) {
                        appeal.setAdminResponse(appealsSection.getString(appealId + ".adminResponse"));
                    }
                    
                    // Load comments if they exist
                    ConfigurationSection commentsSection = appealsSection.getConfigurationSection(appealId + ".comments");
                    if (commentsSection != null) {
                        List<AppealComment> comments = new ArrayList<>();
                        
                        for (String commentId : commentsSection.getKeys(false)) {
                            AppealComment comment = new AppealComment();
                            comment.setId(commentId);
                            comment.setStaffName(commentsSection.getString(commentId + ".staffName"));
                            comment.setComment(commentsSection.getString(commentId + ".comment"));
                            comment.setTimestamp(commentsSection.getLong(commentId + ".timestamp"));
                            
                            comments.add(comment);
                        }
                        
                        appeal.setComments(comments);
                    } else {
                        appeal.setComments(new ArrayList<>());
                    }
                    
                    appeals.add(appeal);
                }
            }
            
            // Sort by submission time, newest first
            appeals.sort((a1, a2) -> Long.compare(a2.getSubmissionTime(), a1.getSubmissionTime()));
            
            return gson.toJson(appeals);
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error getting all appeals: " + e.getMessage(), e);
            response.status(500);
            return gson.toJson(Map.of("error", "Internal server error"));
        }
    }
    
    /**
     * Get a specific appeal by ID (admin)
     */
    public Object getAppeal(Request request, Response response) {
        try {
            String appealId = request.params(":id");
            
            if (!appealsConfig.contains("appeals." + appealId)) {
                response.status(404);
                return gson.toJson(Map.of("error", "Appeal not found"));
            }
            
            ConfigurationSection appealSection = appealsConfig.getConfigurationSection("appeals." + appealId);
            
            Appeal appeal = new Appeal();
            appeal.setId(appealId);
            appeal.setPlayerUUID(appealSection.getString("playerUUID"));
            appeal.setPlayerName(appealSection.getString("playerName"));
            appeal.setAppealText(appealSection.getString("appealText"));
            appeal.setSubmissionTime(appealSection.getLong("submissionTime"));
            appeal.setStatus(AppealStatus.valueOf(appealSection.getString("status")));
            
            // Add optional fields if they exist
            if (appealSection.contains("contactEmail")) {
                appeal.setContactEmail(appealSection.getString("contactEmail"));
            }
            if (appealSection.contains("discordTag")) {
                appeal.setDiscordTag(appealSection.getString("discordTag"));
            }
            if (appealSection.contains("banReason")) {
                appeal.setBanReason(appealSection.getString("banReason"));
            }
            if (appealSection.contains("adminResponse")) {
                appeal.setAdminResponse(appealSection.getString("adminResponse"));
            }
            
            // Load comments if they exist
            ConfigurationSection commentsSection = appealSection.getConfigurationSection("comments");
            if (commentsSection != null) {
                List<AppealComment> comments = new ArrayList<>();
                
                for (String commentId : commentsSection.getKeys(false)) {
                    AppealComment comment = new AppealComment();
                    comment.setId(commentId);
                    comment.setStaffName(commentsSection.getString(commentId + ".staffName"));
                    comment.setComment(commentsSection.getString(commentId + ".comment"));
                    comment.setTimestamp(commentsSection.getLong(commentId + ".timestamp"));
                    
                    comments.add(comment);
                }
                
                comments.sort((c1, c2) -> Long.compare(c1.getTimestamp(), c2.getTimestamp()));
                appeal.setComments(comments);
            } else {
                appeal.setComments(new ArrayList<>());
            }
            
            return gson.toJson(appeal);
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error getting appeal: " + e.getMessage(), e);
            response.status(500);
            return gson.toJson(Map.of("error", "Internal server error"));
        }
    }
    
    /**
     * Approve an appeal (admin)
     */
    public Object approveAppeal(Request request, Response response) {
        try {
            String appealId = request.params(":id");
            
            if (!appealsConfig.contains("appeals." + appealId)) {
                response.status(404);
                return gson.toJson(Map.of("error", "Appeal not found"));
            }
            
            // Get appeal data
            String playerName = appealsConfig.getString("appeals." + appealId + ".playerName");
            String playerUUID = appealsConfig.getString("appeals." + appealId + ".playerUUID");
            String adminResponse = gson.fromJson(request.body(), Map.class).get("adminResponse").toString();
            
            // Update appeal status
            appealsConfig.set("appeals." + appealId + ".status", AppealStatus.APPROVED.toString());
            appealsConfig.set("appeals." + appealId + ".adminResponse", adminResponse);
            appealsConfig.set("appeals." + appealId + ".resolvedTime", System.currentTimeMillis());
            appealsConfig.set("appeals." + appealId + ".resolvedBy", request.attribute("username"));
            
            // Save config
            plugin.getStorageManager().saveConfig(appealsConfig, "appeals");
            
            // Unban the player
            Bukkit.getBanList(org.bukkit.BanList.Type.NAME).pardon(playerName);
            
            // Log action
            plugin.getStorageManager().logModAction(
                    request.attribute("username"),
                    "ApproveAppeal",
                    playerName,
                    "Appeal approved: " + adminResponse
            );
            
            // Send notification if email or Discord is configured
            sendNotification(appealId, "Your ban appeal has been approved");
            
            return gson.toJson(Map.of(
                    "success", true,
                    "message", "Appeal approved successfully"
            ));
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error approving appeal: " + e.getMessage(), e);
            response.status(500);
            return gson.toJson(Map.of("error", "Internal server error"));
        }
    }
    
    /**
     * Deny an appeal (admin)
     */
    public Object denyAppeal(Request request, Response response) {
        try {
            String appealId = request.params(":id");
            
            if (!appealsConfig.contains("appeals." + appealId)) {
                response.status(404);
                return gson.toJson(Map.of("error", "Appeal not found"));
            }
            
            // Get appeal data
            String playerName = appealsConfig.getString("appeals." + appealId + ".playerName");
            String playerUUID = appealsConfig.getString("appeals." + appealId + ".playerUUID");
            String adminResponse = gson.fromJson(request.body(), Map.class).get("adminResponse").toString();
            
            // Update appeal status
            appealsConfig.set("appeals." + appealId + ".status", AppealStatus.DENIED.toString());
            appealsConfig.set("appeals." + appealId + ".adminResponse", adminResponse);
            appealsConfig.set("appeals." + appealId + ".resolvedTime", System.currentTimeMillis());
            appealsConfig.set("appeals." + appealId + ".resolvedBy", request.attribute("username"));
            
            // Save config
            plugin.getStorageManager().saveConfig(appealsConfig, "appeals");
            
            // Log action
            plugin.getStorageManager().logModAction(
                    request.attribute("username"),
                    "DenyAppeal",
                    playerName,
                    "Appeal denied: " + adminResponse
            );
            
            // Send notification if email or Discord is configured
            sendNotification(appealId, "Your ban appeal has been denied");
            
            return gson.toJson(Map.of(
                    "success", true,
                    "message", "Appeal denied successfully"
            ));
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error denying appeal: " + e.getMessage(), e);
            response.status(500);
            return gson.toJson(Map.of("error", "Internal server error"));
        }
    }
    
    /**
     * Add a comment to an appeal (admin)
     */
    public Object addComment(Request request, Response response) {
        try {
            String appealId = request.params(":id");
            
            if (!appealsConfig.contains("appeals." + appealId)) {
                response.status(404);
                return gson.toJson(Map.of("error", "Appeal not found"));
            }
            
            // Get comment data
            Map<String, Object> requestData = gson.fromJson(request.body(), Map.class);
            String comment = requestData.get("comment").toString();
            String staffName = request.attribute("username");
            
            // Generate comment ID
            String commentId = UUID.randomUUID().toString();
            
            // Save comment
            String commentPath = "appeals." + appealId + ".comments." + commentId;
            appealsConfig.set(commentPath + ".staffName", staffName);
            appealsConfig.set(commentPath + ".comment", comment);
            appealsConfig.set(commentPath + ".timestamp", System.currentTimeMillis());
            
            // Save config
            plugin.getStorageManager().saveConfig(appealsConfig, "appeals");
            
            return gson.toJson(Map.of(
                    "success", true,
                    "message", "Comment added successfully",
                    "commentId", commentId
            ));
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error adding comment: " + e.getMessage(), e);
            response.status(500);
            return gson.toJson(Map.of("error", "Internal server error"));
        }
    }
    
    /**
     * Send a notification about an appeal status change
     */
    private void sendNotification(String appealId, String message) {
        // This would handle email or Discord notifications
        // For now just log it
        plugin.getLogger().info("Would send notification for appeal " + appealId + ": " + message);
        
        // In the future, this will be implemented based on the configured notification methods
    }
} 