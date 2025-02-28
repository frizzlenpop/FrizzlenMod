package org.frizzlenpop.frizzlenMod.storage;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.frizzlenpop.frizzlenMod.FrizzlenMod;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class ConfigManager {
    private final FrizzlenMod plugin;
    
    // Default config values
    private final List<String> DEFAULT_BLACKLISTED_WORDS = Arrays.asList(
        "badword1", "badword2", "badword3"
    );
    
    private final List<String> DEFAULT_BLACKLISTED_PATTERNS = Arrays.asList(
        "\\b(https?://|www\\.)\\S+\\b",     // URL matching
        "\\b\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\b"  // IP address matching
    );

    public ConfigManager(FrizzlenMod plugin) {
        this.plugin = plugin;
        
        // Initialize default config
        setupDefaultConfig();
    }
    
    /**
     * Sets up the default configuration file with all needed settings
     */
    private void setupDefaultConfig() {
        FileConfiguration config = plugin.getConfig();
        boolean updated = false;
        
        // Chat settings
        if (!config.contains("chat.blacklisted-words")) {
            config.set("chat.blacklisted-words", DEFAULT_BLACKLISTED_WORDS);
            updated = true;
        }
        
        if (!config.contains("chat.blacklisted-patterns")) {
            config.set("chat.blacklisted-patterns", DEFAULT_BLACKLISTED_PATTERNS);
            updated = true;
        }
        
        if (!config.contains("chat.filter-enabled")) {
            config.set("chat.filter-enabled", true);
            updated = true;
        }
        
        if (!config.contains("chat.anti-caps-enabled")) {
            config.set("chat.anti-caps-enabled", true);
            updated = true;
        }
        
        // Warning settings
        if (!config.contains("warnings.auto-punish")) {
            config.set("warnings.auto-punish", true);
            updated = true;
        }
        
        if (!config.contains("warnings.kick-threshold")) {
            config.set("warnings.kick-threshold", 2);
            updated = true;
        }
        
        if (!config.contains("warnings.mute-threshold")) {
            config.set("warnings.mute-threshold", 3);
            updated = true;
        }
        
        if (!config.contains("warnings.mute-duration")) {
            config.set("warnings.mute-duration", "1h");
            updated = true;
        }
        
        if (!config.contains("warnings.temp-ban-threshold")) {
            config.set("warnings.temp-ban-threshold", 4);
            updated = true;
        }
        
        if (!config.contains("warnings.temp-ban-duration")) {
            config.set("warnings.temp-ban-duration", "1d");
            updated = true;
        }
        
        if (!config.contains("warnings.ban-threshold")) {
            config.set("warnings.ban-threshold", 5);
            updated = true;
        }
        
        // General settings
        if (!config.contains("general.save-interval")) {
            config.set("general.save-interval", 5); // Save data every 5 minutes
            updated = true;
        }
        
        if (updated) {
            plugin.saveConfig();
        }
    }
    
    /**
     * Reloads the configuration from disk
     */
    public void reloadConfig() {
        plugin.reloadConfig();
    }
    
    /**
     * Creates a configuration file if it doesn't exist
     * 
     * @param fileName The name of the file (without .yml extension)
     * @return The FileConfiguration object
     */
    public FileConfiguration createConfigFile(String fileName) {
        File file = new File(plugin.getDataFolder(), fileName + ".yml");
        
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create " + fileName + ".yml: " + e.getMessage());
            }
        }
        
        return YamlConfiguration.loadConfiguration(file);
    }
    
    /**
     * Saves a configuration file
     * 
     * @param config The configuration to save
     * @param fileName The name of the file (without .yml extension)
     */
    public void saveConfigFile(FileConfiguration config, String fileName) {
        try {
            config.save(new File(plugin.getDataFolder(), fileName + ".yml"));
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save " + fileName + ".yml: " + e.getMessage());
        }
    }
} 