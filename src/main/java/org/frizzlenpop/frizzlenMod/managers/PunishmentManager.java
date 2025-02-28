package org.frizzlenpop.frizzlenMod.managers;

import org.bukkit.Bukkit;
import org.bukkit.BanList;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenMod.FrizzlenMod;
import org.frizzlenpop.frizzlenMod.utils.TimeUtils;

import java.util.*;
import java.util.Date;

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
    
    public void tempMutePlayer(UUID playerUUID, long durationMillis) {
        long expiryTime = System.currentTimeMillis() + durationMillis;
        tempMutedPlayers.put(playerUUID, expiryTime);
        savePunishments();
    }
    
    public void unmutePlayer(UUID playerUUID) {
        mutedPlayers.remove(playerUUID);
        tempMutedPlayers.remove(playerUUID);
        savePunishments();
    }
    
    public boolean isMuted(UUID playerUUID) {
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
    
    private void checkWarningEscalation(UUID playerUUID, int warningCount, String reason) {
        // Get the warning thresholds from config
        int muteThreshold = plugin.getConfig().getInt("warnings.mute-threshold", 3);
        int kickThreshold = plugin.getConfig().getInt("warnings.kick-threshold", 5);
        int tempBanThreshold = plugin.getConfig().getInt("warnings.temp-ban-threshold", 7);
        int banThreshold = plugin.getConfig().getInt("warnings.ban-threshold", 10);
        
        Player player = Bukkit.getPlayer(playerUUID);
        if (player == null) return; // Player is offline
        
        if (warningCount >= banThreshold) {
            // Permanent ban
            banPlayer(playerUUID, "Exceeded warning limit: " + reason);
            return;
        }
        
        if (warningCount >= tempBanThreshold) {
            // Temporary ban
            String banDuration = plugin.getConfig().getString("warnings.temp-ban-duration", "1d");
            long durationMillis = TimeUtils.parseTimeString(banDuration);
            tempBanPlayer(playerUUID, "Exceeded warning limit: " + reason, durationMillis);
            return;
        }
        
        if (warningCount >= kickThreshold) {
            // Kick player
            player.kickPlayer("You have received " + warningCount + " warnings. Last warning: " + reason);
            return;
        }
        
        if (warningCount >= muteThreshold) {
            // Mute player
            mutePlayer(playerUUID);
            player.sendMessage("§cYou have been muted due to receiving " + warningCount + " warnings.");
        }
    }
    
    // Player freeze methods
    public void freezePlayer(UUID playerUUID) {
        frozenPlayers.add(playerUUID);
        
        // Notify the player
        Player player = Bukkit.getPlayer(playerUUID);
        if (player != null && player.isOnline()) {
            player.sendMessage("§cYou have been frozen by a staff member. Do not disconnect!");
        }
    }
    
    public void unfreezePlayer(UUID playerUUID) {
        frozenPlayers.remove(playerUUID);
        
        // Notify the player
        Player player = Bukkit.getPlayer(playerUUID);
        if (player != null && player.isOnline()) {
            player.sendMessage("§aYou have been unfrozen.");
        }
    }
    
    public boolean isFrozen(UUID playerUUID) {
        return frozenPlayers.contains(playerUUID);
    }
    
    // Ban methods
    public void banPlayer(UUID playerUUID, String reason) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerUUID);
        Bukkit.getBanList(BanList.Type.NAME).addBan(player.getName(), reason, null, null);
        
        // Kick if online
        Player onlinePlayer = player.getPlayer();
        if (onlinePlayer != null && onlinePlayer.isOnline()) {
            onlinePlayer.kickPlayer("You have been banned: " + reason);
        }
    }
    
    public void tempBanPlayer(UUID playerUUID, String reason, long durationMillis) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerUUID);
        Date expiry = new Date(System.currentTimeMillis() + durationMillis);
        Bukkit.getBanList(BanList.Type.NAME).addBan(player.getName(), reason, expiry, null);
        
        // Kick if online
        Player onlinePlayer = player.getPlayer();
        if (onlinePlayer != null && onlinePlayer.isOnline()) {
            onlinePlayer.kickPlayer("You have been temporarily banned: " + reason);
        }
    }
    
    public void unbanPlayer(UUID playerUUID) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerUUID);
        Bukkit.getBanList(BanList.Type.NAME).pardon(player.getName());
    }
    
    public boolean isBanned(UUID playerUUID) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerUUID);
        return Bukkit.getBanList(BanList.Type.NAME).isBanned(player.getName());
    }
    
    /**
     * Gets all muted players
     * 
     * @return A map of player UUIDs to mute reasons
     */
    public Map<UUID, String> getAllMutedPlayers() {
        Map<UUID, String> mutedPlayers = new HashMap<>();
        
        // Add permanently muted players
        for (UUID uuid : this.mutedPlayers) {
            mutedPlayers.put(uuid, "Permanent");
        }
        
        // Add temporarily muted players with remaining time
        for (Map.Entry<UUID, Long> entry : this.tempMutedPlayers.entrySet()) {
            long remaining = entry.getValue() - System.currentTimeMillis();
            if (remaining > 0) {
                mutedPlayers.put(entry.getKey(), TimeUtils.formatTime(remaining));
            }
        }
        
        return mutedPlayers;
    }
    
    /**
     * Checks if a player is muted (alias for isMuted for backward compatibility)
     * 
     * @param playerUUID The UUID of the player to check
     * @return true if the player is muted
     */
    public boolean isPlayerMuted(UUID playerUUID) {
        return isMuted(playerUUID);
    }
    
    /**
     * Checks if a player is frozen (alias for isFrozen for backward compatibility)
     * 
     * @param playerUUID The UUID of the player to check
     * @return true if the player is frozen
     */
    public boolean isPlayerFrozen(UUID playerUUID) {
        return isFrozen(playerUUID);
    }
    
    /**
     * Clears all warnings for a player
     * 
     * @param playerUUID The UUID of the player to clear warnings for
     */
    public void clearPlayerWarnings(UUID playerUUID) {
        playerWarnings.remove(playerUUID);
        savePunishments();
    }
} 