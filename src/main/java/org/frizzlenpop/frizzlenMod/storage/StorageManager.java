package org.frizzlenpop.frizzlenMod.storage;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.frizzlenpop.frizzlenMod.FrizzlenMod;
import org.frizzlenpop.frizzlenMod.utils.TimeUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class StorageManager {
    private final FrizzlenMod plugin;
    private final ConfigManager configManager;
    
    // Configuration files
    private FileConfiguration punishmentsConfig;
    private FileConfiguration jailsConfig;
    private FileConfiguration reportsConfig;
    private FileConfiguration modLogsConfig;
    
    // File names
    private static final String PUNISHMENTS_FILE = "punishments";
    private static final String JAILS_FILE = "jails";
    private static final String REPORTS_FILE = "reports";
    private static final String MODLOGS_FILE = "modlogs";
    
    // Save task ID
    private int saveTaskId = -1;

    public StorageManager(FrizzlenMod plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        
        // Initialize all data files
        loadDataFiles();
        
        // Start automatic saving
        startSaveTask();
    }
    
    /**
     * Loads all data files from disk
     */
    private void loadDataFiles() {
        punishmentsConfig = configManager.createConfigFile(PUNISHMENTS_FILE);
        jailsConfig = configManager.createConfigFile(JAILS_FILE);
        reportsConfig = configManager.createConfigFile(REPORTS_FILE);
        modLogsConfig = configManager.createConfigFile(MODLOGS_FILE);
    }
    
    /**
     * Starts a task to periodically save all data
     */
    private void startSaveTask() {
        // Cancel any existing task
        if (saveTaskId != -1) {
            Bukkit.getScheduler().cancelTask(saveTaskId);
        }
        
        // Get save interval from config (in minutes)
        int saveInterval = plugin.getConfig().getInt("general.save-interval", 5);
        
        // Start new save task
        saveTaskId = Bukkit.getScheduler().runTaskTimer(plugin, this::saveAll, 
                20L * 60 * saveInterval, 20L * 60 * saveInterval).getTaskId();
    }
    
    /**
     * Saves all data files to disk
     */
    public void saveAll() {
        savePunishmentsConfig();
        saveJailsConfig();
        saveReportsConfig();
        saveModLogsConfig();
    }
    
    /**
     * Reloads all data files from disk
     */
    public void reloadAll() {
        loadDataFiles();
        startSaveTask(); // Restart the save task with potentially new interval
    }
    
    /**
     * Gets the punishments configuration
     * 
     * @return The punishments FileConfiguration
     */
    public FileConfiguration getPunishmentsConfig() {
        return punishmentsConfig;
    }
    
    /**
     * Saves the punishments configuration to disk
     */
    public void savePunishmentsConfig() {
        configManager.saveConfigFile(punishmentsConfig, PUNISHMENTS_FILE);
    }
    
    /**
     * Gets the jails configuration
     * 
     * @return The jails FileConfiguration
     */
    public FileConfiguration getJailsConfig() {
        return jailsConfig;
    }
    
    /**
     * Saves the jails configuration to disk
     */
    public void saveJailsConfig() {
        configManager.saveConfigFile(jailsConfig, JAILS_FILE);
    }
    
    /**
     * Gets the reports configuration
     * 
     * @return The reports FileConfiguration
     */
    public FileConfiguration getReportsConfig() {
        return reportsConfig;
    }
    
    /**
     * Saves the reports configuration to disk
     */
    public void saveReportsConfig() {
        configManager.saveConfigFile(reportsConfig, REPORTS_FILE);
    }
    
    /**
     * Gets the moderation logs configuration
     * 
     * @return The moderation logs FileConfiguration
     */
    public FileConfiguration getModLogsConfig() {
        return modLogsConfig;
    }
    
    /**
     * Saves the moderation logs configuration to disk
     */
    public void saveModLogsConfig() {
        configManager.saveConfigFile(modLogsConfig, MODLOGS_FILE);
    }
    
    /**
     * Logs a moderation action to the moderation logs
     * 
     * @param moderator The name of the moderator performing the action
     * @param action The action being performed
     * @param target The target of the action
     * @param reason The reason for the action
     */
    public void logModAction(String moderator, String action, String target, String reason) {
        long timestamp = System.currentTimeMillis();
        String logKey = timestamp + "." + action.toLowerCase().replace(" ", "_");
        
        modLogsConfig.set(logKey + ".moderator", moderator);
        modLogsConfig.set(logKey + ".action", action);
        modLogsConfig.set(logKey + ".target", target);
        modLogsConfig.set(logKey + ".reason", reason);
        modLogsConfig.set(logKey + ".timestamp", timestamp);
        
        // Auto-save the modlogs file
        saveModLogsConfig();
    }
    
    /**
     * Logs a moderation action to the moderation logs with duration
     * 
     * @param moderator The name of the moderator performing the action
     * @param action The action being performed
     * @param target The target of the action
     * @param duration The duration of the action (for temporary actions)
     * @param reason The reason for the action
     */
    public void logModAction(String moderator, String action, String target, String duration, String reason) {
        long timestamp = System.currentTimeMillis();
        String logKey = timestamp + "." + action.toLowerCase().replace(" ", "_");
        
        modLogsConfig.set(logKey + ".moderator", moderator);
        modLogsConfig.set(logKey + ".action", action);
        modLogsConfig.set(logKey + ".target", target);
        modLogsConfig.set(logKey + ".duration", duration);
        modLogsConfig.set(logKey + ".reason", reason);
        modLogsConfig.set(logKey + ".timestamp", timestamp);
        
        // Auto-save the modlogs file
        saveModLogsConfig();
    }
    
    /**
     * Retrieves the moderation action logs for a specific player
     * 
     * @param playerName The name of the player to get logs for
     * @return A list of ModAction objects
     */
    public List<ModAction> getModActionLogs(String playerName) {
        List<ModAction> logs = new ArrayList<>();
        
        // Check if there are any logs
        if (modLogsConfig.getKeys(false).isEmpty()) {
            return logs;
        }
        
        // Loop through all log entries
        for (String key : modLogsConfig.getKeys(false)) {
            ConfigurationSection section = modLogsConfig.getConfigurationSection(key);
            if (section == null) continue;
            
            String target = section.getString("target");
            if (target != null && target.equalsIgnoreCase(playerName)) {
                String staffName = section.getString("moderator", "Unknown");
                String actionType = section.getString("action", "Unknown");
                String reason = section.getString("reason", "");
                String duration = section.getString("duration", "");
                long timestamp = section.getLong("timestamp", 0);
                
                UUID id = UUID.randomUUID();
                Date date = new Date(timestamp);
                
                logs.add(new ModAction(id, staffName, target, actionType, reason, duration, date));
            }
        }
        
        return logs;
    }
    
    /**
     * Adds a player report to the reports file
     * 
     * @param reporter The name of the player making the report
     * @param reported The name of the reported player
     * @param reason The reason for the report
     * @return The report ID
     */
    public String addReport(String reporter, String reported, String reason) {
        long timestamp = System.currentTimeMillis();
        String reportId = timestamp + "-" + reported.toLowerCase();
        
        reportsConfig.set("reports." + reportId + ".reporter", reporter);
        reportsConfig.set("reports." + reportId + ".reported", reported);
        reportsConfig.set("reports." + reportId + ".reason", reason);
        reportsConfig.set("reports." + reportId + ".timestamp", timestamp);
        reportsConfig.set("reports." + reportId + ".resolved", false);
        
        // Auto-save the reports file
        saveReportsConfig();
        
        return reportId;
    }
    
    /**
     * Marks a report as resolved
     * 
     * @param reportId The ID of the report
     * @param resolvedBy The name of the staff member who resolved it
     * @param resolution How the report was resolved
     * @return true if the report was found and marked as resolved
     */
    public boolean resolveReport(String reportId, String resolvedBy, String resolution) {
        if (reportsConfig.contains("reports." + reportId)) {
            reportsConfig.set("reports." + reportId + ".resolved", true);
            reportsConfig.set("reports." + reportId + ".resolved_by", resolvedBy);
            reportsConfig.set("reports." + reportId + ".resolution", resolution);
            reportsConfig.set("reports." + reportId + ".resolved_time", System.currentTimeMillis());
            
            // Auto-save the reports file
            saveReportsConfig();
            return true;
        }
        return false;
    }
    
    /**
     * Gets a map of all unresolved reports
     * 
     * @return A map of report IDs to report details
     */
    public Map<String, Map<String, Object>> getUnresolvedReports() {
        Map<String, Map<String, Object>> reports = new HashMap<>();
        
        if (reportsConfig.contains("reports")) {
            for (String reportId : reportsConfig.getConfigurationSection("reports").getKeys(false)) {
                boolean resolved = reportsConfig.getBoolean("reports." + reportId + ".resolved", false);
                
                if (!resolved) {
                    Map<String, Object> reportDetails = new HashMap<>();
                    reportDetails.put("reporter", reportsConfig.getString("reports." + reportId + ".reporter"));
                    reportDetails.put("reported", reportsConfig.getString("reports." + reportId + ".reported"));
                    reportDetails.put("reason", reportsConfig.getString("reports." + reportId + ".reason"));
                    reportDetails.put("timestamp", reportsConfig.getLong("reports." + reportId + ".timestamp"));
                    
                    reports.put(reportId, reportDetails);
                }
            }
        }
        
        return reports;
    }
    
    /**
     * Creates or gets a configuration file
     * 
     * @param fileName The name of the configuration file (without extension)
     * @return The FileConfiguration
     */
    public FileConfiguration createOrGetConfig(String fileName) {
        return configManager.createConfigFile(fileName);
    }
    
    /**
     * Saves a configuration file
     * 
     * @param config The configuration to save
     * @param fileName The name of the file to save to (without extension)
     */
    public void saveConfig(FileConfiguration config, String fileName) {
        configManager.saveConfigFile(config, fileName);
    }
    
    /**
     * Gets all moderation logs
     * 
     * @return A list of all moderation logs
     */
    public List<ModAction> getAllModLogs() {
        List<ModAction> logs = new ArrayList<>();
        
        // Check if there are any logs
        if (modLogsConfig.getKeys(false).isEmpty()) {
            return logs;
        }
        
        // Loop through all log entries
        for (String key : modLogsConfig.getKeys(false)) {
            ConfigurationSection section = modLogsConfig.getConfigurationSection(key);
            if (section == null) continue;
            
            String staffName = section.getString("moderator", "Unknown");
            String target = section.getString("target", "Unknown");
            String actionType = section.getString("action", "Unknown");
            String reason = section.getString("reason", "");
            String duration = section.getString("duration", "");
            long timestamp = section.getLong("timestamp", 0);
            
            UUID id = UUID.randomUUID();
            Date date = new Date(timestamp);
            
            logs.add(new ModAction(id, staffName, target, actionType, reason, duration, date));
        }
        
        return logs;
    }
    
    /**
     * Parses a time string (e.g., "1d", "2h", "30m") into milliseconds
     * 
     * @param timeString The time string to parse
     * @return The time in milliseconds
     */
    public long parseTimeString(String timeString) {
        if (timeString == null || timeString.isEmpty()) {
            return 0;
        }
        
        return TimeUtils.parseTimeString(timeString);
    }
    
    /**
     * Retrieves a list of moderation actions for a specific player
     * 
     * @param playerName The name of the player to get actions for
     * @return A list of ModAction objects
     */
    public List<ModAction> getPlayerModActions(String playerName) {
        List<ModAction> actions = new ArrayList<>();
        
        // Check if there are any logs
        if (modLogsConfig.getKeys(false).isEmpty()) {
            return actions;
        }
        
        // Loop through all log entries
        for (String key : modLogsConfig.getKeys(false)) {
            ConfigurationSection section = modLogsConfig.getConfigurationSection(key);
            if (section == null) continue;
            
            String target = section.getString("target");
            if (target != null && target.equalsIgnoreCase(playerName)) {
                String moderatorName = section.getString("moderator", "Unknown");
                String actionType = section.getString("action", "Unknown");
                String reason = section.getString("reason", "");
                String duration = section.getString("duration", "");
                long timestamp = section.getLong("timestamp", 0);
                
                UUID id = UUID.randomUUID();
                Date date = new Date(timestamp);
                
                actions.add(new ModAction(id, moderatorName, target, actionType, reason, duration, date));
            }
        }
        
        return actions;
    }
} 