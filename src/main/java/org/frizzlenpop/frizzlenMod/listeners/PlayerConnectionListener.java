package org.frizzlenpop.frizzlenMod.listeners;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.frizzlenpop.frizzlenMod.FrizzlenMod;
import org.frizzlenpop.frizzlenMod.utils.MessageUtils;

import java.util.UUID;

public class PlayerConnectionListener implements Listener {
    
    private final FrizzlenMod plugin;
    
    public PlayerConnectionListener(FrizzlenMod plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerLogin(PlayerLoginEvent event) {
        // Check if player is banned
        if (event.getResult() == PlayerLoginEvent.Result.KICK_BANNED) {
            // Log the failed login attempt
            plugin.getStorageManager().logModAction(
                    "Server",
                    "BanBlock",
                    event.getPlayer().getName(),
                    "Player attempted to join while banned"
            );
            return;
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        
        // Handle vanished players - hide new player from them
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (plugin.getVanishManager().isVanished(onlinePlayer.getUniqueId())) {
                player.hidePlayer(plugin, onlinePlayer);
            }
        }
        
        // Check if this player is vanished and hide them from others
        if (plugin.getVanishManager().isVanished(playerUUID)) {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (!onlinePlayer.hasPermission("frizzlenmod.vanish.see")) {
                    onlinePlayer.hidePlayer(plugin, player);
                }
            }
            
            // Send reminder message to vanished player
            MessageUtils.sendMessage(player, "§7You are currently §evanished§7. Other players cannot see you.");
            
            // Hide join message for vanished players
            event.setJoinMessage(null);
        }
        
        // Check if player is jailed and teleport them back if needed
        if (plugin.getJailManager().isPlayerJailed(playerUUID)) {
            String jailName = plugin.getJailManager().getPlayerJail(playerUUID);
            if (jailName != null) {
                // Teleport player back to jail
                plugin.getJailManager().teleportToJail(player, jailName);
                
                // Inform player they are still jailed
                MessageUtils.sendMessage(player, "§cYou are currently jailed.");
            }
        }
        
        // Check if player is muted and notify them
        if (plugin.getPunishmentManager().isPlayerMuted(playerUUID)) {
            MessageUtils.sendMessage(player, "§cYou are currently muted and cannot chat.");
        }
        
        // Check if player has warnings and remind them
        int warnings = plugin.getPunishmentManager().getPlayerWarnings(playerUUID);
        if (warnings > 0) {
            MessageUtils.sendMessage(player, "§eYou currently have " + warnings + " warning" + 
                    (warnings == 1 ? "" : "s") + ".");
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // Hide quit message for vanished players
        if (plugin.getVanishManager().isVanished(player.getUniqueId())) {
            event.setQuitMessage(null);
        }
    }
} 