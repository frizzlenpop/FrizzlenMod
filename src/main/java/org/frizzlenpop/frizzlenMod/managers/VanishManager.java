package org.frizzlenpop.frizzlenMod.managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenMod.FrizzlenMod;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class VanishManager {
    private final FrizzlenMod plugin;
    private final Set<UUID> vanishedPlayers;

    public VanishManager(FrizzlenMod plugin) {
        this.plugin = plugin;
        this.vanishedPlayers = new HashSet<>();
        
        // Start vanish task to handle new players joining
        startVanishTask();
    }
    
    private void startVanishTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                updateVisibility(player);
            }
        }, 20L, 20L * 10); // Check every 10 seconds
    }
    
    /**
     * Vanishes a player so they are invisible to other players who don't have permission
     * to see vanished players
     * 
     * @param player The player to vanish
     * @return True if the player was vanished, false if they were already vanished
     */
    public boolean vanishPlayer(Player player) {
        UUID uuid = player.getUniqueId();
        
        if (vanishedPlayers.contains(uuid)) {
            return false; // Already vanished
        }
        
        vanishedPlayers.add(uuid);
        
        // Hide this player from all other players
        updateVisibility(player);
        
        return true;
    }
    
    /**
     * Makes a vanished player visible again
     * 
     * @param player The player to make visible
     * @return True if the player was made visible, false if they weren't vanished
     */
    public boolean unvanishPlayer(Player player) {
        UUID uuid = player.getUniqueId();
        
        if (!vanishedPlayers.contains(uuid)) {
            return false; // Not vanished
        }
        
        vanishedPlayers.remove(uuid);
        
        // Show this player to all other players
        updateVisibility(player);
        
        return true;
    }
    
    /**
     * Checks if a player is currently vanished
     * 
     * @param uuid The UUID of the player to check
     * @return True if the player is vanished
     */
    public boolean isVanished(UUID uuid) {
        return vanishedPlayers.contains(uuid);
    }
    
    /**
     * Updates the visibility of a player for all other players
     * 
     * @param player The player whose visibility to update
     */
    public void updateVisibility(Player player) {
        boolean isVanished = vanishedPlayers.contains(player.getUniqueId());
        
        for (Player otherPlayer : Bukkit.getOnlinePlayers()) {
            if (player.equals(otherPlayer)) continue;
            
            if (isVanished) {
                // If this player is vanished, only show them to players with permission
                if (otherPlayer.hasPermission("frizzlenmod.vanish.see")) {
                    otherPlayer.showPlayer(plugin, player);
                } else {
                    otherPlayer.hidePlayer(plugin, player);
                }
            } else {
                // If not vanished, show to everyone
                otherPlayer.showPlayer(plugin, player);
            }
            
            // Also handle the case where the other player is vanished
            if (vanishedPlayers.contains(otherPlayer.getUniqueId()) && !player.hasPermission("frizzlenmod.vanish.see")) {
                player.hidePlayer(plugin, otherPlayer);
            } else {
                player.showPlayer(plugin, otherPlayer);
            }
        }
    }
    
    /**
     * Updates visibility for all players
     */
    public void updateAllPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            updateVisibility(player);
        }
    }
    
    /**
     * Gets the set of all vanished players
     * 
     * @return A set of player UUIDs who are vanished
     */
    public Set<UUID> getVanishedPlayers() {
        return new HashSet<>(vanishedPlayers);
    }
} 