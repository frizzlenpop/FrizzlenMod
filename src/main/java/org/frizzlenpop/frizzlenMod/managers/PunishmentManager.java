package org.frizzlenpop.frizzlenMod.managers;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenMod.FrizzlenMod;
import org.frizzlenpop.frizzlenMod.utils.TimeUtils;

import java.util.*;

public class PunishmentManager {
    private final FrizzlenMod plugin;
    private final Set<UUID> mutedPlayers;
    private final Map<UUID, Long> tempMutedPlayers;
    private final Map<UUID, Integer> playerWarnings;
    private final Set<UUID> frozenPlayers;

    public PunishmentManager(FrizzlenMod plugin) {
        this.plugin = plugin;
        this.mutedPlayers = new HashSet<>();
        this.tempMutedPlayers = new HashMap<>();
        this.playerWarnings = new HashMap<>();
        this.frozenPlayers = new HashSet<>();
        
        // Load existing punishments from storage
        loadPunishments();
        
        // Start a timer to check temp mutes
        startTempMuteChecker();
    }
    
    private void loadPunishments() {
        FileConfiguration config = plugin.getStorageManager().getPunishmentsConfig();
        
        if (config.contains("muted-players")) {
            for (String uuidString : config.getStringList("muted-players")) {
                try {
                    UUID uuid = UUID.fromString(uuidString);
                    mutedPlayers.add(uuid);
                } catch (IllegalArgumentException ignored) {}
            }
        }
        
        if (config.contains("temp-muted-players")) {
            for (String uuidString : config.getConfigurationSection("temp-muted-players").getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uuidString);
                    long expiryTime = config.getLong("temp-muted-players." + uuidString);
                    if (expiryTime > System.currentTimeMillis()) {
                        tempMutedPlayers.put(uuid, expiryTime);
                    }
                } catch (IllegalArgumentException ignored) {}
            }
        }
        
        if (config.contains("player-warnings")) {
            for (String uuidString : config.getConfigurationSection("player-warnings").getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uuidString);
                    int warnings = config.getInt("player-warnings." + uuidString);
                    playerWarnings.put(uuid, warnings);
                } catch (IllegalArgumentException ignored) {}
            }
        }
    }
    
    public void savePunishments() {
        FileConfiguration config = plugin.getStorageManager().getPunishmentsConfig();
        
        // Save muted players
        List<String> mutedUUIDs = new ArrayList<>();
        for (UUID uuid : mutedPlayers) {
            mutedUUIDs.add(uuid.toString());
        }
        config.set("muted-players", mutedUUIDs);
        
        // Save temp-muted players
        config.set("temp-muted-players", null); // Clear existing data
        for (Map.Entry<UUID, Long> entry : tempMutedPlayers.entrySet()) {
            config.set("temp-muted-players." + entry.getKey().toString(), entry.getValue());
        }
        
        // Save player warnings
        config.set("player-warnings", null); // Clear existing data
        for (Map.Entry<UUID, Integer> entry : playerWarnings.entrySet()) {
            config.set("player-warnings." + entry.getKey().toString(), entry.getValue());
        }
        
        plugin.getStorageManager().savePunishmentsConfig();
    }
    
    private void startTempMuteChecker() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            long currentTime = System.currentTimeMillis();
            Iterator<Map.Entry<UUID, Long>> iterator = tempMutedPlayers.entrySet().iterator();
            
            while (iterator.hasNext()) {
                Map.Entry<UUID, Long> entry = iterator.next();
                if (entry.getValue() <= currentTime) {
                    iterator.remove();
                    
                    // Notify player if online
                    Player player = Bukkit.getPlayer(entry.getKey());
                    if (player != null && player.isOnline()) {
                        player.sendMessage("§aYour temporary mute has expired.");
                    }
                }
            }
        }, 20L * 60, 20L * 60); // Check every minute
    }
    
    // Player mute methods
    public void mutePlayer(UUID playerUUID) {
        mutedPlayers.add(playerUUID);
        tempMutedPlayers.remove(playerUUID); // Remove from temp mutes if exists
        savePunishments();
    }
    
    public void tempMutePlayer(UUID playerUUID, long durationMinutes) {
        long expiryTime = System.currentTimeMillis() + (durationMinutes * 60 * 1000);
        tempMutedPlayers.put(playerUUID, expiryTime);
        savePunishments();
    }
    
    public void unmutePlayer(UUID playerUUID) {
        mutedPlayers.remove(playerUUID);
        tempMutedPlayers.remove(playerUUID);
        savePunishments();
    }
    
    public boolean isPlayerMuted(UUID playerUUID) {
        return mutedPlayers.contains(playerUUID) || tempMutedPlayers.containsKey(playerUUID);
    }
    
    public String getMuteTimeRemaining(UUID playerUUID) {
        if (!tempMutedPlayers.containsKey(playerUUID)) {
            return mutedPlayers.contains(playerUUID) ? "Permanent" : "Not muted";
        }
        
        long expiryTime = tempMutedPlayers.get(playerUUID);
        long remainingTime = expiryTime - System.currentTimeMillis();
        
        if (remainingTime <= 0) {
            tempMutedPlayers.remove(playerUUID);
            return "Not muted";
        }
        
        return TimeUtils.formatTime(remainingTime);
    }
    
    // Player warning methods
    public int warnPlayer(UUID playerUUID, String reason) {
        int currentWarnings = playerWarnings.getOrDefault(playerUUID, 0);
        int newWarnings = currentWarnings + 1;
        playerWarnings.put(playerUUID, newWarnings);
        savePunishments();
        
        // Check if we need to escalate punishment based on warning count
        checkWarningEscalation(playerUUID, newWarnings, reason);
        
        return newWarnings;
    }
    
    public int getPlayerWarnings(UUID playerUUID) {
        return playerWarnings.getOrDefault(playerUUID, 0);
    }
    
    public void clearPlayerWarnings(UUID playerUUID) {
        playerWarnings.remove(playerUUID);
        savePunishments();
    }
    
    private void checkWarningEscalation(UUID playerUUID, int warnings, String reason) {
        // This will be implemented based on the plugin's configuration
        FileConfiguration config = plugin.getConfig();
        
        if (config.getBoolean("warnings.auto-punish", true)) {
            if (warnings >= config.getInt("warnings.ban-threshold", 5)) {
                // Auto-ban the player
                OfflinePlayer player = Bukkit.getOfflinePlayer(playerUUID);
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), 
                        "ban " + player.getName() + " Exceeded maximum warnings: " + reason);
            } else if (warnings >= config.getInt("warnings.temp-ban-threshold", 4)) {
                // Auto temp-ban the player
                OfflinePlayer player = Bukkit.getOfflinePlayer(playerUUID);
                String tempBanTime = config.getString("warnings.temp-ban-duration", "1d");
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), 
                        "tempban " + player.getName() + " " + tempBanTime + " Multiple warnings: " + reason);
            } else if (warnings >= config.getInt("warnings.mute-threshold", 3)) {
                // Auto mute the player
                OfflinePlayer player = Bukkit.getOfflinePlayer(playerUUID);
                String muteTime = config.getString("warnings.mute-duration", "1h");
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), 
                        "tempmute " + player.getName() + " " + muteTime + " Multiple warnings");
            } else if (warnings >= config.getInt("warnings.kick-threshold", 2)) {
                // Auto kick the player if online
                Player player = Bukkit.getPlayer(playerUUID);
                if (player != null && player.isOnline()) {
                    player.kickPlayer("Received multiple warnings: " + reason);
                }
            }
        }
    }
    
    // Player freeze methods
    public void freezePlayer(UUID playerUUID) {
        frozenPlayers.add(playerUUID);
    }
    
    public void unfreezePlayer(UUID playerUUID) {
        frozenPlayers.remove(playerUUID);
    }
    
    public boolean isPlayerFrozen(UUID playerUUID) {
        return frozenPlayers.contains(playerUUID);
    }
} 