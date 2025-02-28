package org.frizzlenpop.frizzlenMod.api.services;

import org.frizzlenpop.frizzlenMod.FrizzlenMod;
import org.frizzlenpop.frizzlenMod.api.models.ModLog;
import org.frizzlenpop.frizzlenMod.api.models.PaginatedResponse;
import org.frizzlenpop.frizzlenMod.storage.ModAction;
import org.frizzlenpop.frizzlenMod.storage.StorageManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing moderation logs
 */
public class ModLogsService {
    private final FrizzlenMod plugin;
    private final StorageManager storageManager;
    
    /**
     * Creates a new ModLogsService
     * @param plugin The FrizzlenMod plugin instance
     */
    public ModLogsService(FrizzlenMod plugin) {
        this.plugin = plugin;
        this.storageManager = plugin.getStorageManager();
    }
    
    /**
     * Gets all moderation logs with pagination
     * @param page The page number (1-based)
     * @param pageSize The page size
     * @return A paginated response with moderation logs
     */
    public PaginatedResponse<ModLog> getAllLogs(int page, int pageSize) {
        List<ModLog> allLogs = getAllModLogs();
        allLogs.sort(Comparator.comparing(ModLog::getTimestamp).reversed());
        
        return paginateResults(allLogs, page, pageSize);
    }
    
    /**
     * Gets all moderation logs for a specific player with pagination
     * @param playerName The name of the player
     * @param page The page number (1-based)
     * @param pageSize The page size
     * @return A paginated response with moderation logs
     */
    public PaginatedResponse<ModLog> getLogsForPlayer(String playerName, int page, int pageSize) {
        List<ModAction> playerActions = storageManager.getModActionLogs(playerName);
        List<ModLog> playerLogs = convertModActionsToModLogs(playerActions);
        playerLogs.sort(Comparator.comparing(ModLog::getTimestamp).reversed());
        
        return paginateResults(playerLogs, page, pageSize);
    }
    
    /**
     * Gets all moderation logs for a specific action type with pagination
     * @param action The action type
     * @param page The page number (1-based)
     * @param pageSize The page size
     * @return A paginated response with moderation logs
     */
    public PaginatedResponse<ModLog> getLogsByAction(String action, int page, int pageSize) {
        List<ModLog> allLogs = getAllModLogs();
        List<ModLog> filteredLogs = allLogs.stream()
                .filter(log -> log.getAction().equalsIgnoreCase(action))
                .sorted(Comparator.comparing(ModLog::getTimestamp).reversed())
                .collect(Collectors.toList());
        
        return paginateResults(filteredLogs, page, pageSize);
    }
    
    /**
     * Gets all moderation logs for a specific moderator with pagination
     * @param moderator The name of the moderator
     * @param page The page number (1-based)
     * @param pageSize The page size
     * @return A paginated response with moderation logs
     */
    public PaginatedResponse<ModLog> getLogsByModerator(String moderator, int page, int pageSize) {
        List<ModLog> allLogs = getAllModLogs();
        List<ModLog> filteredLogs = allLogs.stream()
                .filter(log -> log.getModerator().equalsIgnoreCase(moderator))
                .sorted(Comparator.comparing(ModLog::getTimestamp).reversed())
                .collect(Collectors.toList());
        
        return paginateResults(filteredLogs, page, pageSize);
    }
    
    /**
     * Gets all moderation logs within a time range with pagination
     * @param startTime The start time (milliseconds since epoch)
     * @param endTime The end time (milliseconds since epoch)
     * @param page The page number (1-based)
     * @param pageSize The page size
     * @return A paginated response with moderation logs
     */
    public PaginatedResponse<ModLog> getLogsByTimeRange(long startTime, long endTime, int page, int pageSize) {
        List<ModLog> allLogs = getAllModLogs();
        List<ModLog> filteredLogs = allLogs.stream()
                .filter(log -> log.getTimestamp() >= startTime && log.getTimestamp() <= endTime)
                .sorted(Comparator.comparing(ModLog::getTimestamp).reversed())
                .collect(Collectors.toList());
        
        return paginateResults(filteredLogs, page, pageSize);
    }
    
    /**
     * Paginates a list of results
     * @param results The list of results
     * @param page The page number (1-based)
     * @param pageSize The page size
     * @return A paginated response
     */
    private <T> PaginatedResponse<T> paginateResults(List<T> results, int page, int pageSize) {
        int totalItems = results.size();
        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalItems);
        
        if (startIndex >= totalItems) {
            return new PaginatedResponse<>(Collections.emptyList(), page, pageSize, totalItems);
        }
        
        List<T> pageItems = results.subList(startIndex, endIndex);
        return new PaginatedResponse<>(pageItems, page, pageSize, totalItems);
    }
    
    /**
     * Adds a new moderation log
     * @param moderator The name of the moderator
     * @param action The action type
     * @param target The name of the target player
     * @param reason The reason for the action
     * @param duration The duration of the action (if applicable)
     * @return The created moderation log
     */
    public ModLog addLog(String moderator, String action, String target, String reason, String duration) {
        storageManager.logModAction(moderator, action, target, duration, reason);
        
        ModLog log = new ModLog();
        log.setId(String.valueOf(System.currentTimeMillis()));
        log.setModerator(moderator);
        log.setAction(action);
        log.setTarget(target);
        log.setReason(reason);
        log.setDuration(duration);
        log.setTimestamp(System.currentTimeMillis());
        
        return log;
    }
    
    /**
     * Gets all moderation logs
     * @return A list of all moderation logs
     */
    private List<ModLog> getAllModLogs() {
        List<ModLog> allLogs = new ArrayList<>();
        
        // Get all player names from the config
        for (String key : storageManager.getModLogsConfig().getKeys(false)) {
            if (storageManager.getModLogsConfig().isConfigurationSection(key)) {
                String moderator = storageManager.getModLogsConfig().getString(key + ".moderator");
                String action = storageManager.getModLogsConfig().getString(key + ".action");
                String target = storageManager.getModLogsConfig().getString(key + ".target");
                String reason = storageManager.getModLogsConfig().getString(key + ".reason", "");
                String duration = storageManager.getModLogsConfig().getString(key + ".duration", "");
                long timestamp = storageManager.getModLogsConfig().getLong(key + ".timestamp");
                
                ModLog log = new ModLog();
                log.setId(key);
                log.setModerator(moderator);
                log.setAction(action);
                log.setTarget(target);
                log.setReason(reason);
                log.setDuration(duration);
                log.setTimestamp(timestamp);
                
                allLogs.add(log);
            }
        }
        
        return allLogs;
    }
    
    /**
     * Converts a list of ModAction objects to ModLog objects
     * @param actions The list of ModAction objects
     * @return A list of ModLog objects
     */
    private List<ModLog> convertModActionsToModLogs(List<ModAction> actions) {
        return actions.stream().map(action -> {
            ModLog log = new ModLog();
            log.setId(action.getId().toString());
            log.setModerator(action.getModerator());
            log.setAction(action.getAction());
            log.setTarget(action.getTarget());
            log.setReason(action.getReason());
            log.setDuration(action.getDuration());
            log.setTimestamp(action.getTimestamp().getTime());
            return log;
        }).collect(Collectors.toList());
    }
} 