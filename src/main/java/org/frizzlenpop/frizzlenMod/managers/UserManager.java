package org.frizzlenpop.frizzlenMod.managers;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.frizzlenpop.frizzlenMod.FrizzlenMod;
import org.frizzlenpop.frizzlenMod.api.models.User;
import org.frizzlenpop.frizzlenMod.api.utils.PasswordUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * Manager for web panel users
 */
public class UserManager {
    private final FrizzlenMod plugin;
    private final Map<String, User> users;
    private final File usersFile;
    private FileConfiguration usersConfig;
    
    /**
     * Creates a new UserManager
     * @param plugin The FrizzlenMod plugin instance
     */
    public UserManager(FrizzlenMod plugin) {
        this.plugin = plugin;
        this.users = new HashMap<>();
        this.usersFile = new File(plugin.getDataFolder(), "users.yml");
        loadUsers();
        
        // Create default admin user if no users exist
        if (users.isEmpty()) {
            createDefaultAdminUser();
        }
    }
    
    /**
     * Loads users from the users.yml file
     */
    private void loadUsers() {
        if (!usersFile.exists()) {
            try {
                usersFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create users file", e);
                return;
            }
        }
        
        usersConfig = YamlConfiguration.loadConfiguration(usersFile);
        ConfigurationSection usersSection = usersConfig.getConfigurationSection("users");
        
        if (usersSection == null) {
            return;
        }
        
        for (String username : usersSection.getKeys(false)) {
            ConfigurationSection userSection = usersSection.getConfigurationSection(username);
            if (userSection == null) {
                continue;
            }
            
            User user = new User();
            user.setUsername(username);
            user.setPasswordHash(userSection.getString("passwordHash"));
            user.setRole(userSection.getString("role"));
            user.setMinecraftUUID(userSection.getString("minecraftUUID"));
            user.setLastLogin(userSection.getLong("lastLogin"));
            user.setActive(userSection.getBoolean("active", true));
            
            users.put(username.toLowerCase(), user);
        }
    }
    
    /**
     * Saves users to the users.yml file
     */
    public void saveUsers() {
        if (usersConfig == null) {
            usersConfig = new YamlConfiguration();
        }
        
        ConfigurationSection usersSection = usersConfig.createSection("users");
        
        for (User user : users.values()) {
            ConfigurationSection userSection = usersSection.createSection(user.getUsername());
            userSection.set("passwordHash", user.getPasswordHash());
            userSection.set("role", user.getRole());
            userSection.set("minecraftUUID", user.getMinecraftUUID());
            userSection.set("lastLogin", user.getLastLogin());
            userSection.set("active", user.isActive());
        }
        
        try {
            usersConfig.save(usersFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save users file", e);
        }
    }
    
    /**
     * Creates a default admin user
     */
    private void createDefaultAdminUser() {
        String defaultUsername = "admin";
        String defaultPassword = "admin"; // This should be changed immediately
        
        User adminUser = new User(defaultUsername, PasswordUtil.hashPassword(defaultPassword), "ADMIN");
        users.put(defaultUsername.toLowerCase(), adminUser);
        
        plugin.getLogger().warning("Created default admin user with username 'admin' and password 'admin'. Please change this password immediately!");
        
        saveUsers();
    }
    
    /**
     * Gets a user by username
     * @param username The username
     * @return The user, or null if not found
     */
    public User getUserByUsername(String username) {
        return users.get(username.toLowerCase());
    }
    
    /**
     * Creates a new user
     * @param username The username
     * @param password The password
     * @param role The role
     * @return The created user
     */
    public User createUser(String username, String password, String role) {
        if (users.containsKey(username.toLowerCase())) {
            return null;
        }
        
        User user = new User(username, PasswordUtil.hashPassword(password), role);
        users.put(username.toLowerCase(), user);
        saveUsers();
        
        return user;
    }
    
    /**
     * Updates a user's password
     * @param username The username
     * @param newPassword The new password
     * @return True if successful, false otherwise
     */
    public boolean updatePassword(String username, String newPassword) {
        User user = getUserByUsername(username);
        if (user == null) {
            return false;
        }
        
        user.setPasswordHash(PasswordUtil.hashPassword(newPassword));
        saveUsers();
        
        return true;
    }
    
    /**
     * Updates a user's role
     * @param username The username
     * @param newRole The new role
     * @return True if successful, false otherwise
     */
    public boolean updateRole(String username, String newRole) {
        User user = getUserByUsername(username);
        if (user == null) {
            return false;
        }
        
        user.setRole(newRole);
        saveUsers();
        
        return true;
    }
    
    /**
     * Deletes a user
     * @param username The username
     * @return True if successful, false otherwise
     */
    public boolean deleteUser(String username) {
        if (!users.containsKey(username.toLowerCase())) {
            return false;
        }
        
        users.remove(username.toLowerCase());
        saveUsers();
        
        return true;
    }
    
    /**
     * Gets all users
     * @return A list of all users
     */
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }
    
    /**
     * Authenticates a user
     * @param username The username
     * @param password The password
     * @return The authenticated user, or null if authentication failed
     */
    public User authenticateUser(String username, String password) {
        User user = getUserByUsername(username);
        if (user == null || !user.isActive()) {
            return null;
        }
        
        if (PasswordUtil.verifyPassword(password, user.getPasswordHash())) {
            user.setLastLogin(System.currentTimeMillis());
            saveUsers();
            return user;
        }
        
        return null;
    }
} 