package org.frizzlenpop.frizzlenMod.api.services;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.frizzlenpop.frizzlenMod.FrizzlenMod;
import org.frizzlenpop.frizzlenMod.api.models.Appeal;
import org.frizzlenpop.frizzlenMod.api.models.AppealComment;
import org.frizzlenpop.frizzlenMod.api.models.AppealStatus;
import org.frizzlenpop.frizzlenMod.api.models.PaginatedResponse;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Service for managing ban appeals
 */
public class AppealsService {
    private final FrizzlenMod plugin;
    private final File appealsFile;
    private FileConfiguration appealsConfig;
    private final Map<String, Appeal> appeals;
    private int nextAppealId;
    private int nextCommentId;
    
    /**
     * Creates a new AppealsService
     * @param plugin The FrizzlenMod plugin instance
     */
    public AppealsService(FrizzlenMod plugin) {
        this.plugin = plugin;
        this.appeals = new HashMap<>();
        this.appealsFile = new File(plugin.getDataFolder(), "appeals.yml");
        loadAppeals();
    }
    
    /**
     * Loads appeals from the appeals.yml file
     */
    private void loadAppeals() {
        if (!appealsFile.exists()) {
            try {
                appealsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create appeals file", e);
                return;
            }
        }
        
        appealsConfig = YamlConfiguration.loadConfiguration(appealsFile);
        nextAppealId = appealsConfig.getInt("nextAppealId", 1);
        nextCommentId = appealsConfig.getInt("nextCommentId", 1);
        
        ConfigurationSection appealsSection = appealsConfig.getConfigurationSection("appeals");
        if (appealsSection == null) {
            return;
        }
        
        for (String appealIdStr : appealsSection.getKeys(false)) {
            ConfigurationSection appealSection = appealsSection.getConfigurationSection(appealIdStr);
            if (appealSection == null) {
                continue;
            }
            
            Appeal appeal = new Appeal();
            appeal.setId(String.valueOf(appealSection.getInt("id")));
            appeal.setPlayerUUID(appealSection.getString("playerUUID"));
            appeal.setPlayerName(appealSection.getString("playerName")); 
            appeal.setAppealText(appealSection.getString("appealText"));
            appeal.setSubmissionTime(appealSection.getLong("submissionTime"));
            appeal.setStatus(AppealStatus.valueOf(appealSection.getString("status", "PENDING")));
            
            // Load comments
            ConfigurationSection commentsSection = appealSection.getConfigurationSection("comments");
            if (commentsSection != null) {
                List<AppealComment> comments = new ArrayList<>();
                
                for (String commentIdStr : commentsSection.getKeys(false)) {
                    ConfigurationSection commentSection = commentsSection.getConfigurationSection(commentIdStr);
                    if (commentSection == null) {
                        continue;
                    }
                    
                    AppealComment comment = new AppealComment();
                    comment.setId(String.valueOf(commentSection.getInt("id")));
                    comment.setStaffName(commentSection.getString("staffName"));
                    comment.setComment(commentSection.getString("comment"));
                    comment.setTimestamp(commentSection.getLong("timestamp"));
                    comments.add(comment);
                }
                
                appeal.setComments(comments);
            }
            
            appeals.put(String.valueOf(appeal.getId()), appeal);
        }
    }
    
    /**
     * Saves appeals to the appeals.yml file
     */
    public void saveAppeals() {
        if (appealsConfig == null) {
            appealsConfig = new YamlConfiguration();
        }
        
        appealsConfig.set("nextAppealId", nextAppealId);
        appealsConfig.set("nextCommentId", nextCommentId);
        
        ConfigurationSection appealsSection = appealsConfig.createSection("appeals");
        
        for (Appeal appeal : appeals.values()) {
            ConfigurationSection appealSection = appealsSection.createSection(String.valueOf(appeal.getId()));
            appealSection.set("id", appeal.getId());
            appealSection.set("playerUUID", appeal.getPlayerUUID());
            appealSection.set("playerName", appeal.getPlayerName());
            appealSection.set("appealText", appeal.getAppealText());
            appealSection.set("submissionTime", appeal.getSubmissionTime());
            appealSection.set("status", appeal.getStatus().name());
            
            // Save comments
            if (appeal.getComments() != null && !appeal.getComments().isEmpty()) {
                ConfigurationSection commentsSection = appealSection.createSection("comments");
                
                for (AppealComment comment : appeal.getComments()) {
                    ConfigurationSection commentSection = commentsSection.createSection(String.valueOf(comment.getId()));
                    commentSection.set("id", comment.getId());
                    commentSection.set("staffName", comment.getStaffName());
                    commentSection.set("comment", comment.getComment());
                    commentSection.set("timestamp", comment.getTimestamp());
                }
            }
        }
        
        try {
            appealsConfig.save(appealsFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save appeals file", e);
        }
    }
    
    /**
     * Creates a new appeal
     * @param playerUUID The UUID of the player submitting the appeal
     * @param playerName The name of the player submitting the appeal
     * @param appealText The text of the appeal
     * @return The created appeal
     */
    public Appeal createAppeal(String playerUUID, String playerName, String appealText) {
        // Check if player already has a pending appeal
        for (Appeal appeal : appeals.values()) {
            if (appeal.getPlayerUUID().equals(playerUUID) && 
                (appeal.getStatus() == AppealStatus.PENDING || appeal.getStatus() == AppealStatus.REOPENED)) {
                return null;
            }
        }
        Appeal appeal = new Appeal();
        appeal.setId(String.valueOf(nextAppealId++));
        appeal.setPlayerUUID(playerUUID);
        appeal.setPlayerName(playerName);
        appeal.setAppealText(appealText);
        appeal.setSubmissionTime(System.currentTimeMillis());
        appeal.setStatus(AppealStatus.PENDING);
        appeal.setComments(new ArrayList<>());
        
        appeals.put(String.valueOf(appeal.getId()), appeal);
        saveAppeals();
        
        return appeal;
    }
    
    /**
     * Gets an appeal by ID
     * @param appealId The ID of the appeal
     * @return The appeal, or null if not found
     */
    public Appeal getAppeal(int appealId) {
        return appeals.get(String.valueOf(appealId));
    }
    
    /**
     * Gets all appeals for a player
     * @param playerUUID The UUID of the player
     * @return A list of appeals for the player
     */
    public List<Appeal> getAppealsForPlayer(String playerUUID) {
        return appeals.values().stream()
                .filter(appeal -> appeal.getPlayerUUID().equals(playerUUID))
                .collect(Collectors.toList());
    }
    
    /**
     * Gets all appeals with pagination
     * @param page The page number (1-based)
     * @param pageSize The page size
     * @return A paginated response with appeals
     */
    public PaginatedResponse<Appeal> getAllAppeals(int page, int pageSize) {
        List<Appeal> allAppeals = new ArrayList<>(appeals.values());
        allAppeals.sort(Comparator.comparing(Appeal::getSubmissionTime).reversed());
        
        int totalItems = allAppeals.size();
        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalItems);
        
        if (startIndex >= totalItems) {
            return new PaginatedResponse<>(Collections.emptyList(), page, pageSize, totalItems);
        }
        
        List<Appeal> pageItems = allAppeals.subList(startIndex, endIndex);
        return new PaginatedResponse<>(pageItems, page, pageSize, totalItems);
    }
    
    /**
     * Gets all appeals with a specific status
     * @param status The status to filter by
     * @param page The page number (1-based)
     * @param pageSize The page size
     * @return A paginated response with appeals
     */
    public PaginatedResponse<Appeal> getAppealsByStatus(AppealStatus status, int page, int pageSize) {
        List<Appeal> filteredAppeals = appeals.values().stream()
                .filter(appeal -> appeal.getStatus() == status)
                .sorted(Comparator.comparing(Appeal::getSubmissionTime).reversed())
                .collect(Collectors.toList());
        
        int totalItems = filteredAppeals.size();
        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalItems);
        
        if (startIndex >= totalItems) {
            return new PaginatedResponse<>(Collections.emptyList(), page, pageSize, totalItems);
        }
        
        List<Appeal> pageItems = filteredAppeals.subList(startIndex, endIndex);
        return new PaginatedResponse<>(pageItems, page, pageSize, totalItems);
    }
    
    /**
     * Updates the status of an appeal
     * @param appealId The ID of the appeal
     * @param newStatus The new status
     * @param staffName The name of the staff member making the change
     * @param comment A comment explaining the status change
     * @return The updated appeal, or null if not found
     */
    public Appeal updateAppealStatus(int appealId, AppealStatus newStatus, String staffName, String comment) {
        Appeal appeal = getAppeal(appealId);
        if (appeal == null) {
            return null;
        }
        
        appeal.setStatus(newStatus);
        
        // Add a comment about the status change
        if (comment != null && !comment.isEmpty()) {
            AppealComment appealComment = new AppealComment();
            appealComment.setId(String.valueOf(nextCommentId++));
            appealComment.setStaffName(staffName);
            appealComment.setComment(comment);
            appealComment.setTimestamp(System.currentTimeMillis());
            if (appeal.getComments() == null) {
                appeal.setComments(new ArrayList<>());
            }
            
            appeal.getComments().add(appealComment);
        }
        
        saveAppeals();
        return appeal;
    }
    
    /**
     * Adds a comment to an appeal
     * @param appealId The ID of the appeal
     * @param staffName The name of the staff member adding the comment
     * @param comment The comment text
     * @return The updated appeal, or null if not found
     */
    public Appeal addComment(int appealId, String staffName, String comment) {
        Appeal appeal = getAppeal(appealId);
        if (appeal == null) {
            return null;
        }
        
        AppealComment appealComment = new AppealComment();
        appealComment.setId(String.valueOf(nextCommentId++));
        appealComment.setStaffName(staffName);
        appealComment.setComment(comment);
        appealComment.setTimestamp(System.currentTimeMillis());
        if (appeal.getComments() == null) {
            appeal.setComments(new ArrayList<>());
        }
        
        appeal.getComments().add(appealComment);
        saveAppeals();
        
        return appeal;
    }
} 