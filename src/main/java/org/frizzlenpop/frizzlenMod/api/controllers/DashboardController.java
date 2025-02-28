package org.frizzlenpop.frizzlenMod.api.controllers;

import com.google.gson.Gson;
import org.bukkit.Bukkit;
import org.frizzlenpop.frizzlenMod.FrizzlenMod;
import spark.Request;
import spark.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for dashboard-related API endpoints
 */
public class DashboardController {
    private final FrizzlenMod plugin;
    private final Gson gson;

    public DashboardController(FrizzlenMod plugin, Gson gson) {
        this.plugin = plugin;
        this.gson = gson;
    }

    /**
     * Get dashboard statistics
     * GET /api/admin/dashboard/stats
     */
    public Object getStats(Request request, Response response) {
        response.type("application/json");
        
        try {
            // Create mock data for dashboard stats
            Map<String, Object> stats = new HashMap<>();
            stats.put("activeBans", 5); // Mock number
            stats.put("activeMutes", 3); // Mock number
            stats.put("pendingAppeals", 2); // Mock number
            stats.put("totalUsers", Bukkit.getServer().getOfflinePlayers().length);
            
            Map<String, Object> apiResponse = new HashMap<>();
            apiResponse.put("success", true);
            apiResponse.put("data", stats);
            
            return gson.toJson(apiResponse);
        } catch (Exception e) {
            plugin.getLogger().severe("Error getting dashboard stats: " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get dashboard stats");
            return gson.toJson(errorResponse);
        }
    }

    /**
     * Get recent moderation logs
     * GET /api/admin/dashboard/recent-logs
     */
    public Object getRecentLogs(Request request, Response response) {
        response.type("application/json");
        
        try {
            // Create mock logs data
            List<Map<String, Object>> logs = new ArrayList<>();
            
            // Add sample log entries
            Map<String, Object> log1 = new HashMap<>();
            log1.put("timestamp", System.currentTimeMillis());
            log1.put("moderator", "Admin");
            log1.put("action", "BAN");
            log1.put("target", "Player1");
            log1.put("reason", "Hacking");
            logs.add(log1);
            
            Map<String, Object> log2 = new HashMap<>();
            log2.put("timestamp", System.currentTimeMillis() - 86400000); // 1 day ago
            log2.put("moderator", "Mod1");
            log2.put("action", "MUTE");
            log2.put("target", "Player2");
            log2.put("reason", "Spam");
            logs.add(log2);
            
            Map<String, Object> apiResponse = new HashMap<>();
            apiResponse.put("success", true);
            apiResponse.put("data", logs);
            
            return gson.toJson(apiResponse);
        } catch (Exception e) {
            plugin.getLogger().severe("Error getting recent logs: " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get recent logs");
            return gson.toJson(errorResponse);
        }
    }

    /**
     * Get recent ban appeals
     * GET /api/admin/dashboard/recent-appeals
     */
    public Object getRecentAppeals(Request request, Response response) {
        response.type("application/json");
        
        try {
            // Create mock appeals data
            List<Map<String, Object>> appeals = new ArrayList<>();
            
            // Add sample appeal entries
            Map<String, Object> appeal1 = new HashMap<>();
            appeal1.put("id", "appeal-123");
            appeal1.put("playerName", "Player1");
            appeal1.put("submissionTime", System.currentTimeMillis());
            appeal1.put("status", "PENDING");
            appeals.add(appeal1);
            
            Map<String, Object> appeal2 = new HashMap<>();
            appeal2.put("id", "appeal-124");
            appeal2.put("playerName", "Player2");
            appeal2.put("submissionTime", System.currentTimeMillis() - 172800000); // 2 days ago
            appeal2.put("status", "APPROVED");
            appeals.add(appeal2);
            
            Map<String, Object> apiResponse = new HashMap<>();
            apiResponse.put("success", true);
            apiResponse.put("data", appeals);
            
            return gson.toJson(apiResponse);
        } catch (Exception e) {
            plugin.getLogger().severe("Error getting recent appeals: " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get recent appeals");
            return gson.toJson(errorResponse);
        }
    }
} 