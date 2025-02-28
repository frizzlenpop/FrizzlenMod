package org.frizzlenpop.frizzlenMod.api.controllers;

import com.google.gson.Gson;
import org.frizzlenpop.frizzlenMod.FrizzlenMod;
import spark.Request;
import spark.Response;

import java.util.*;
import java.util.logging.Level;

/**
 * Handles user management API endpoints
 */
public class UsersController {
    private final FrizzlenMod plugin;
    private final Gson gson;
    
    public UsersController(FrizzlenMod plugin, Gson gson) {
        this.plugin = plugin;
        this.gson = gson;
    }
    
    /**
     * Get all admin users
     */
    public Object getAllUsers(Request request, Response response) {
        try {
            // For now, return mock data
            List<Map<String, Object>> users = new ArrayList<>();
            
            // Add the admin user
            Map<String, Object> adminUser = new HashMap<>();
            adminUser.put("username", "admin");
            adminUser.put("role", "ADMIN");
            adminUser.put("createdAt", System.currentTimeMillis() - 30 * 24 * 60 * 60 * 1000L); // 30 days ago
            
            // Add a moderator user
            Map<String, Object> modUser = new HashMap<>();
            modUser.put("username", "moderator");
            modUser.put("role", "MODERATOR");
            modUser.put("createdAt", System.currentTimeMillis() - 15 * 24 * 60 * 60 * 1000L); // 15 days ago
            
            users.add(adminUser);
            users.add(modUser);
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("users", users);
            responseData.put("success", true);
            
            return gson.toJson(responseData);
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error getting users: " + e.getMessage(), e);
            response.status(500);
            return gson.toJson(Map.of("error", "Internal server error", "success", false));
        }
    }
    
    /**
     * Create a new admin user
     */
    public Object createUser(Request request, Response response) {
        try {
            // Parse request body
            Map<String, Object> requestBody = gson.fromJson(request.body(), Map.class);
            String username = (String) requestBody.get("username");
            String password = (String) requestBody.get("password");
            String role = (String) requestBody.get("role");
            
            // Validate input
            if (username == null || password == null || role == null) {
                response.status(400);
                return gson.toJson(Map.of("error", "Missing required fields", "success", false));
            }
            
            // In a real implementation, we would store the user
            // For now, just return success
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("message", "User created successfully");
            responseData.put("success", true);
            
            return gson.toJson(responseData);
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error creating user: " + e.getMessage(), e);
            response.status(500);
            return gson.toJson(Map.of("error", "Internal server error", "success", false));
        }
    }
    
    /**
     * Get a specific user by username
     */
    public Object getUser(Request request, Response response) {
        try {
            String username = request.params(":username");
            
            // For now, return mock data
            Map<String, Object> user = new HashMap<>();
            
            if (username.equals("admin")) {
                user.put("username", "admin");
                user.put("role", "ADMIN");
                user.put("createdAt", System.currentTimeMillis() - 30 * 24 * 60 * 60 * 1000L);
            } else if (username.equals("moderator")) {
                user.put("username", "moderator");
                user.put("role", "MODERATOR");
                user.put("createdAt", System.currentTimeMillis() - 15 * 24 * 60 * 60 * 1000L);
            } else {
                response.status(404);
                return gson.toJson(Map.of("error", "User not found", "success", false));
            }
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("user", user);
            responseData.put("success", true);
            
            return gson.toJson(responseData);
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error getting user: " + e.getMessage(), e);
            response.status(500);
            return gson.toJson(Map.of("error", "Internal server error", "success", false));
        }
    }
    
    /**
     * Update a user's password
     */
    public Object updatePassword(Request request, Response response) {
        try {
            String username = request.params(":username");
            
            // Parse request body
            Map<String, Object> requestBody = gson.fromJson(request.body(), Map.class);
            String newPassword = (String) requestBody.get("password");
            
            // Validate input
            if (newPassword == null) {
                response.status(400);
                return gson.toJson(Map.of("error", "Missing password field", "success", false));
            }
            
            // In a real implementation, we would update the user's password
            // For now, just return success
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("message", "Password updated successfully");
            responseData.put("success", true);
            
            return gson.toJson(responseData);
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error updating password: " + e.getMessage(), e);
            response.status(500);
            return gson.toJson(Map.of("error", "Internal server error", "success", false));
        }
    }
    
    /**
     * Update a user's role
     */
    public Object updateRole(Request request, Response response) {
        try {
            String username = request.params(":username");
            
            // Parse request body
            Map<String, Object> requestBody = gson.fromJson(request.body(), Map.class);
            String newRole = (String) requestBody.get("role");
            
            // Validate input
            if (newRole == null) {
                response.status(400);
                return gson.toJson(Map.of("error", "Missing role field", "success", false));
            }
            
            // In a real implementation, we would update the user's role
            // For now, just return success
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("message", "Role updated successfully");
            responseData.put("success", true);
            
            return gson.toJson(responseData);
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error updating role: " + e.getMessage(), e);
            response.status(500);
            return gson.toJson(Map.of("error", "Internal server error", "success", false));
        }
    }
    
    /**
     * Delete a user
     */
    public Object deleteUser(Request request, Response response) {
        try {
            String username = request.params(":username");
            
            // In a real implementation, we would delete the user
            // For now, just return success
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("message", "User deleted successfully");
            responseData.put("success", true);
            
            return gson.toJson(responseData);
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error deleting user: " + e.getMessage(), e);
            response.status(500);
            return gson.toJson(Map.of("error", "Internal server error", "success", false));
        }
    }
} 