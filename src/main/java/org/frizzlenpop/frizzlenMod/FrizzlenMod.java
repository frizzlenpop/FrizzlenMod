package org.frizzlenpop.frizzlenMod;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.frizzlenpop.frizzlenMod.commands.*;
import org.frizzlenpop.frizzlenMod.listeners.*;
import org.frizzlenpop.frizzlenMod.managers.*;
import org.frizzlenpop.frizzlenMod.storage.ConfigManager;
import org.frizzlenpop.frizzlenMod.storage.StorageManager;
import org.frizzlenpop.frizzlenMod.utils.MessageUtils;

import java.io.File;
import java.util.logging.Logger;

public final class FrizzlenMod extends JavaPlugin {
    
    private static FrizzlenMod instance;
    private ConfigManager configManager;
    private StorageManager storageManager;
    private PunishmentManager punishmentManager;
    private JailManager jailManager;
    private VanishManager vanishManager;
    private ChatManager chatManager;
    private Logger logger;
    
    @Override
    public void onEnable() {
        // Save instance for static access
        instance = this;
        this.logger = getLogger();
        
        // Initialize config files
        saveDefaultConfig();
        configManager = new ConfigManager(this);
        storageManager = new StorageManager(this);
        
        // Initialize managers
        punishmentManager = new PunishmentManager(this);
        jailManager = new JailManager(this);
        vanishManager = new VanishManager(this);
        chatManager = new ChatManager(this);
        
        // Register commands
        registerCommands();
        
        // Register listeners
        registerListeners();
        
        // Log successful startup
        logger.info("FrizzlenMod has been enabled!");
    }
    
    private void registerCommands() {
        // Player moderation commands
        getCommand("kick").setExecutor(new KickCommand(this));
        getCommand("ban").setExecutor(new BanCommand(this));
        getCommand("tempban").setExecutor(new TempBanCommand(this));
        getCommand("unban").setExecutor(new UnbanCommand(this));
        getCommand("mute").setExecutor(new MuteCommand(this));
        getCommand("tempmute").setExecutor(new TempMuteCommand(this));
        getCommand("unmute").setExecutor(new UnmuteCommand(this));
        getCommand("warn").setExecutor(new WarnCommand(this));
        
        // Player surveillance commands
        getCommand("freeze").setExecutor(new FreezeCommand(this));
        getCommand("vanish").setExecutor(new VanishCommand(this));
        getCommand("invsee").setExecutor(new InvseeCommand(this));
        getCommand("endersee").setExecutor(new EnderseeCommand(this));
        
        // Chat control commands
        getCommand("chatmute").setExecutor(new ChatMuteCommand(this));
        getCommand("chatclear").setExecutor(new ChatClearCommand(this));
        getCommand("slowmode").setExecutor(new SlowModeCommand(this));
        
        // Jail commands
        getCommand("setjail").setExecutor(new SetJailCommand(this));
        getCommand("jail").setExecutor(new JailCommand(this));
        getCommand("unjail").setExecutor(new UnjailCommand(this));
        
        // Logging and report commands
        getCommand("report").setExecutor(new ReportCommand(this));
        getCommand("modlogs").setExecutor(new ModLogsCommand(this));
    }
    
    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new PlayerChatListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerConnectionListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerMovementListener(this), this);
        Bukkit.getPluginManager().registerEvents(new InventoryListener(this), this);
    }
    
    @Override
    public void onDisable() {
        // Save all data
        if (storageManager != null) {
            storageManager.saveAll();
        }
        
        // Release resources
        instance = null;
        logger.info("FrizzlenMod has been disabled!");
    }
    
    // Getters for managers
    public static FrizzlenMod getInstance() {
        return instance;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public StorageManager getStorageManager() {
        return storageManager;
    }
    
    public PunishmentManager getPunishmentManager() {
        return punishmentManager;
    }
    
    public JailManager getJailManager() {
        return jailManager;
    }
    
    public VanishManager getVanishManager() {
        return vanishManager;
    }
    
    public ChatManager getChatManager() {
        return chatManager;
    }
}
