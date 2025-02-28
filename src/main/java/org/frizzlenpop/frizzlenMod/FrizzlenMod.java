package org.frizzlenpop.frizzlenMod;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.frizzlenpop.frizzlenMod.api.WebApiManager;
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
    private UserManager userManager;
    private WebApiManager webApiManager;
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
        userManager = new UserManager(this);
        
        // Register commands
        registerCommands();
        
        // Register listeners
        registerListeners();
        
        // Initialize and start web API if enabled
        if (getConfig().getBoolean("web-api.enabled", true)) {
            webApiManager = new WebApiManager(this);
            webApiManager.start();
        }
        
        // Log successful startup
        logger.info("FrizzlenMod has been enabled!");
    }
    
    private void registerCommands() {
        // Register all commands
        getCommand("ban").setExecutor(new BanCommand(this));
        getCommand("tempban").setExecutor(new TempBanCommand(this));
        getCommand("unban").setExecutor(new UnbanCommand(this));
        getCommand("mute").setExecutor(new MuteCommand(this));
        getCommand("tempmute").setExecutor(new TempMuteCommand(this));
        getCommand("unmute").setExecutor(new UnmuteCommand(this));
        getCommand("kick").setExecutor(new KickCommand(this));
        getCommand("warn").setExecutor(new WarnCommand(this));
        getCommand("freeze").setExecutor(new FreezeCommand(this));
        getCommand("unfreeze").setExecutor(new UnfreezeCommand(this));
        getCommand("vanish").setExecutor(new VanishCommand(this));
        getCommand("invsee").setExecutor(new InvseeCommand(this));
        getCommand("endersee").setExecutor(new EnderseeCommand(this));
        getCommand("chatmute").setExecutor(new ChatMuteCommand(this));
        getCommand("chatclear").setExecutor(new ChatClearCommand(this));
        getCommand("slowmode").setExecutor(new SlowModeCommand(this));
        getCommand("report").setExecutor(new ReportCommand(this));
        getCommand("setjail").setExecutor(new SetJailCommand(this));
        getCommand("jail").setExecutor(new JailCommand(this));
        getCommand("unjail").setExecutor(new UnjailCommand(this));
        getCommand("modlogs").setExecutor(new ModLogsCommand(this));
        
        // Check if clearwarnings command exists in plugin.yml
        if (getCommand("clearwarnings") != null) {
            getCommand("clearwarnings").setExecutor(new ClearWarningsCommand(this));
        }
    }
    
    private void registerListeners() {
        // Register all listeners
        Bukkit.getPluginManager().registerEvents(new PlayerConnectionListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerChatListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerMovementListener(this), this);
        Bukkit.getPluginManager().registerEvents(new InventoryListener(this), this);
    }
    
    @Override
    public void onDisable() {
        // Stop web API if it's running
        if (webApiManager != null) {
            webApiManager.stop();
        }
        
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
    
    public UserManager getUserManager() {
        return userManager;
    }
    
    public WebApiManager getWebApiManager() {
        return webApiManager;
    }
}
