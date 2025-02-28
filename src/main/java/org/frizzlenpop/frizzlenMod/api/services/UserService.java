package org.frizzlenpop.frizzlenMod.api.services;

import org.frizzlenpop.frizzlenMod.FrizzlenMod;
import org.frizzlenpop.frizzlenMod.api.models.ApiResponse;
import org.frizzlenpop.frizzlenMod.api.models.LoginResponse;
import org.frizzlenpop.frizzlenMod.api.models.User;
import org.frizzlenpop.frizzlenMod.api.utils.JwtUtil;
import org.frizzlenpop.frizzlenMod.api.utils.PasswordUtil;
import org.frizzlenpop.frizzlenMod.managers.UserManager;

import java.util.List;

/**
 * Service for managing web panel users
 */
public class UserService {
    private final FrizzlenMod plugin;
    private final UserManager userManager;
    private final JwtUtil jwtUtil;
    
    /**
     * Creates a new UserService
     * @param plugin The FrizzlenMod plugin instance
     * @param jwtUtil The JwtUtil instance
     */
    public UserService(FrizzlenMod plugin, JwtUtil jwtUtil) {
        this.plugin = plugin;
        this.userManager = plugin.getUserManager();
        this.jwtUtil = jwtUtil;
    }
    
    /**
     * Authenticates a user
     * @param username The username
     * @param password The password
     * @return A login response
     */
    public LoginResponse authenticate(String username, String password) {
        User user = userManager.authenticateUser(username, password);
        if (user == null) {
            return new LoginResponse(false, "Invalid username or password");
        }
        
        String token = jwtUtil.generateToken(user);
        return new LoginResponse(true, token, user.getUsername(), user.getRole());
    }
    
    /**
     * Gets all users
     * @return A list of all users
     */
    public List<User> getAllUsers() {
        return userManager.getAllUsers();
    }
    
    /**
     * Gets a user by username
     * @param username The username
     * @return The user, or null if not found
     */
    public User getUserByUsername(String username) {
        return userManager.getUserByUsername(username);
    }
    
    /**
     * Creates a new user
     * @param username The username
     * @param password The password
     * @param role The role
     * @return An API response with the result
     */
    public ApiResponse<User> createUser(String username, String password, String role) {
        if (userManager.getUserByUsername(username) != null) {
            return ApiResponse.error("Username already exists");
        }
        
        User user = userManager.createUser(username, password, role);
        return ApiResponse.success("User created successfully", user);
    }
    
    /**
     * Updates a user's password
     * @param username The username
     * @param newPassword The new password
     * @return An API response with the result
     */
    public ApiResponse<Void> updatePassword(String username, String newPassword) {
        if (userManager.updatePassword(username, newPassword)) {
            return ApiResponse.success("Password updated successfully");
        } else {
            return ApiResponse.error("User not found");
        }
    }
    
    /**
     * Updates a user's role
     * @param username The username
     * @param newRole The new role
     * @return An API response with the result
     */
    public ApiResponse<Void> updateRole(String username, String newRole) {
        if (userManager.updateRole(username, newRole)) {
            return ApiResponse.success("Role updated successfully");
        } else {
            return ApiResponse.error("User not found");
        }
    }
    
    /**
     * Deletes a user
     * @param username The username
     * @return An API response with the result
     */
    public ApiResponse<Void> deleteUser(String username) {
        if (userManager.deleteUser(username)) {
            return ApiResponse.success("User deleted successfully");
        } else {
            return ApiResponse.error("User not found");
        }
    }
} 