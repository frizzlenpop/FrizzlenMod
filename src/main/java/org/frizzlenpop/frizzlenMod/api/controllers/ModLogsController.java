package org.frizzlenpop.frizzlenMod.api.controllers;

import com.google.gson.Gson;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.frizzlenpop.frizzlenMod.FrizzlenMod;
import org.frizzlenpop.frizzlenMod.api.models.ModLog;
import spark.Request;
import spark.Response;

import java.util.*;
import java.util.logging.Level;

/**
 * Handles moderation logs API endpoints
 */
public class ModLogsController {
    private final FrizzlenMod plugin;
    private final Gson gson;
    private final FileConfiguration modLogsConfig;
    
    public ModLogsController(FrizzlenMod plugin, Gson gson) {
        this.plugin = plugin;
        this.gson = gson;
        this.modLogsConfig = plugin.getStorageManager().getModLogsConfig();
    }
    
    /**
     * Get all moderation logs (admin)
     */
    public Object getAllLogs(Request request, Response response) {
        try {
            List<ModLog> logs = new ArrayList<>();
            
            // Parse pagination parameters
            int page = Integer.parseInt(request.queryParams("page") != null ? request.queryParams("page") : "1");
            int limit = Integer.parseInt(request.queryParams("limit") != null ? request.queryParams("limit") : "50");
            
            // Get all log keys and sort them by timestamp (descending)
            List<String> logKeys = new ArrayList<>(modLogsConfig.getKeys(false));
            logKeys.sort((k1, k2) -> {
                long t1 = modLogsConfig.getLong(k1 + ".timestamp", 0);
                long t2 = modLogsConfig.getLong(k2 + ".timestamp", 0);
                return Long.compare(t2, t1); // Descending order
            });
            
            // Apply pagination
            int totalLogs = logKeys.size();
            int totalPages = (int) Math.ceil((double) totalLogs / limit);
            
            int start = (page - 1) * limit;
            int end = Math.min(start + limit, totalLogs);
            
            // Ensure valid page range
            if (start >= totalLogs) {
                response.status(400);
                return gson.toJson(Map.of("error", "Page number out of range"));
            }
            
            // Extract paginated logs
            for (int i = start; i < end; i++) {
                String key = logKeys.get(i);
                ConfigurationSection section = modLogsConfig.getConfigurationSection(key);
                
                if (section != null) {
                    ModLog log = new ModLog();
                    log.setId(key);
                    log.setModerator(section.getString("moderator", "Unknown"));
                    log.setAction(section.getString("action", "Unknown"));
                    log.setTarget(section.getString("target", "Unknown"));
                    log.setReason(section.getString("reason", ""));
                    log.setDuration(section.getString("duration", ""));
                    log.setTimestamp(section.getLong("timestamp", 0));
                    
                    logs.add(log);
                }
            }
            
            // Create pagination metadata
            Map<String, Object> pagination = new HashMap<>();
            pagination.put("page", page);
            pagination.put("limit", limit);
            pagination.put("totalLogs", totalLogs);
            pagination.put("totalPages", totalPages);
            
            // Create response object
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("logs", logs);
            responseData.put("pagination", pagination);
            
            return gson.toJson(responseData);
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error getting moderation logs: " + e.getMessage(), e);
            response.status(500);
            return gson.toJson(Map.of("error", "Internal server error"));
        }
    }
    
    /**
     * Get moderation logs for a specific player (admin)
     */
    public Object getPlayerLogs(Request request, Response response) {
        try {
            String playerName = request.params(":player");
            List<ModLog> logs = new ArrayList<>();
            
            // Get all log keys
            Set<String> logKeys = modLogsConfig.getKeys(false);
            
            for (String key : logKeys) {
                ConfigurationSection section = modLogsConfig.getConfigurationSection(key);
                
                if (section != null) {
                    String target = section.getString("target", "");
                    
                    if (target.equalsIgnoreCase(playerName)) {
                        ModLog log = new ModLog();
                        log.setId(key);
                        log.setModerator(section.getString("moderator", "Unknown"));
                        log.setAction(section.getString("action", "Unknown"));
                        log.setTarget(target);
                        log.setReason(section.getString("reason", ""));
                        log.setDuration(section.getString("duration", ""));
                        log.setTimestamp(section.getLong("timestamp", 0));
                        
                        logs.add(log);
                    }
                }
            }
            
            // Sort by timestamp (newest first)
            logs.sort((l1, l2) -> Long.compare(l2.getTimestamp(), l1.getTimestamp()));
            
            return gson.toJson(logs);
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error getting player logs: " + e.getMessage(), e);
            response.status(500);
            return gson.toJson(Map.of("error", "Internal server error"));
        }
    }
} 