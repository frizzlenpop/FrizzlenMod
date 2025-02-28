package org.frizzlenpop.frizzlenMod.api.middleware;

import com.google.gson.Gson;
import org.frizzlenpop.frizzlenMod.FrizzlenMod;
import org.frizzlenpop.frizzlenMod.api.models.ApiResponse;
import org.frizzlenpop.frizzlenMod.api.models.User;
import org.frizzlenpop.frizzlenMod.api.utils.JwtUtil;
import org.frizzlenpop.frizzlenMod.managers.UserManager;
import spark.Filter;
import spark.Request;
import spark.Response;
import spark.Spark;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Middleware for JWT authentication
 */
public class JwtAuthMiddleware {
    private final FrizzlenMod plugin;
    private final UserManager userManager;
    private final JwtUtil jwtUtil;
    private final Gson gson;
    private final Set<String> excludedPaths;
    
    /**
     * Creates a new JwtAuthMiddleware
     * @param plugin The FrizzlenMod plugin instance
     * @param userManager The UserManager instance
     * @param jwtUtil The JwtUtil instance
     */
    public JwtAuthMiddleware(FrizzlenMod plugin, UserManager userManager, JwtUtil jwtUtil) {
        this.plugin = plugin;
        this.userManager = userManager;
        this.jwtUtil = jwtUtil;
        this.gson = new Gson();
        this.excludedPaths = new HashSet<>(Arrays.asList(
            "/api/auth/login",
            "/api/appeals/submit"
        ));
    }
    
    /**
     * Creates a Spark Filter for JWT authentication
     * @return The filter
     */
    public Filter createFilter() {
        return this::handle;
    }
    
    /**
     * Handles the authentication
     * @param request The request
     * @param response The response
     */
    private void handle(Request request, Response response) {
        // Skip authentication for excluded paths
        String path = request.pathInfo();
        if (excludedPaths.contains(path)) {
            return;
        }
        
        // Get the Authorization header
        String authHeader = request.headers("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            halt(response, 401, "Unauthorized: No token provided");
            return;
        }
        
        // Extract the token
        String token = authHeader.substring(7);
        try {
            // Extract the username from the token
            String username = jwtUtil.extractUsername(token);
            
            // Get the user from the database
            User user = userManager.getUserByUsername(username);
            if (user == null) {
                halt(response, 401, "Unauthorized: Invalid token");
                return;
            }
            
            // Validate the token
            if (!jwtUtil.validateToken(token, user)) {
                halt(response, 401, "Unauthorized: Invalid token");
                return;
            }
            
            // Set the user in the request attribute for later use
            request.attribute("user", user);
        } catch (Exception e) {
            halt(response, 401, "Unauthorized: " + e.getMessage());
        }
    }
    
    /**
     * Halts the request with an error response
     * @param response The response
     * @param status The HTTP status code
     * @param message The error message
     */
    private void halt(Response response, int status, String message) {
        response.status(status);
        response.type("application/json");
        response.body(gson.toJson(ApiResponse.error(message)));
        Spark.halt();
    }
} 