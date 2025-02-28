package org.frizzlenpop.frizzlenMod.api.controllers;

import com.google.gson.Gson;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.frizzlenpop.frizzlenMod.FrizzlenMod;
import spark.Request;
import spark.Response;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * Handles authentication for the admin panel
 */
public class AuthController {
    private final FrizzlenMod plugin;
    private final Gson gson;
    private final String jwtSecret;
    private final FileConfiguration usersConfig;
    
    public AuthController(FrizzlenMod plugin, Gson gson, String jwtSecret) {
        this.plugin = plugin;
        this.gson = gson;
        this.jwtSecret = jwtSecret;
        this.usersConfig = plugin.getStorageManager().createOrGetConfig("webusers");
        
        // Initialize default admin user if no users exist
        initializeDefaultAdmin();
    }
    
    /**
     * Handle login requests
     */
    public Object login(Request request, Response response) {
        try {
            // Parse login request
            Map<String, Object> requestData = gson.fromJson(request.body(), Map.class);
            String username = (String) requestData.get("username");
            String password = (String) requestData.get("password");
            
            if (username == null || password == null) {
                response.status(400);
                return gson.toJson(Map.of("error", "Username and password are required"));
            }
            
            // Verify credentials
            if (!validateCredentials(username, password)) {
                response.status(401);
                return gson.toJson(Map.of("error", "Invalid username or password"));
            }
            
            // Get user role
            String role = usersConfig.getString("users." + username + ".role", "moderator");
            
            // Generate JWT token
            String token = generateToken(username, role);
            
            // Log successful login
            plugin.getLogger().info("Web panel login: " + username);
            
            // Return token
            return gson.toJson(Map.of(
                    "token", token,
                    "username", username,
                    "role", role
            ));
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error processing login: " + e.getMessage(), e);
            response.status(500);
            return gson.toJson(Map.of("error", "Internal server error"));
        }
    }
    
    /**
     * Validate user credentials
     */
    private boolean validateCredentials(String username, String password) {
        if (!usersConfig.contains("users." + username)) {
            return false;
        }
        
        String storedPassword = usersConfig.getString("users." + username + ".password");
        return storedPassword != null && storedPassword.equals(hashPassword(password));
    }
    
    /**
     * Generate a JWT token for the user
     */
    private String generateToken(String username, String role) {
        // JWT expiration (24 hours)
        long expiration = System.currentTimeMillis() + 24 * 60 * 60 * 1000;
        
        // Create signing key from JWT secret
        Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        
        // Generate token
        return Jwts.builder()
                .setSubject(username)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(expiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
    
    /**
     * Initialize a default admin user if no users exist
     */
    private void initializeDefaultAdmin() {
        ConfigurationSection usersSection = usersConfig.getConfigurationSection("users");
        if (usersSection == null || usersSection.getKeys(false).isEmpty()) {
            // Create default admin user
            String defaultPassword = "admin"; // This should be changed immediately
            
            usersConfig.set("users.admin.password", hashPassword(defaultPassword));
            usersConfig.set("users.admin.role", "admin");
            
            plugin.getStorageManager().saveConfig(usersConfig, "webusers");
            
            plugin.getLogger().warning("Created default admin user with password 'admin'. Please change this immediately!");
        }
    }
    
    /**
     * Simple password hashing (in a real application, use a proper password hashing library)
     */
    private String hashPassword(String password) {
        // This is a simplified hash for demonstration
        // In a production environment, use a proper password hashing algorithm like BCrypt
        return password; // TODO: Implement proper password hashing
    }
} 