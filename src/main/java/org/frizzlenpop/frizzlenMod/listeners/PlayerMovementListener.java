package org.frizzlenpop.frizzlenMod.listeners;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.frizzlenpop.frizzlenMod.FrizzlenMod;
import org.frizzlenpop.frizzlenMod.utils.MessageUtils;

import java.util.UUID;

public class PlayerMovementListener implements Listener {
    
    private final FrizzlenMod plugin;
    
    public PlayerMovementListener(FrizzlenMod plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        
        // Check if player is frozen
        if (plugin.getPunishmentManager().isPlayerFrozen(playerUUID)) {
            // Only cancel if player's XYZ position changed (allow looking around)
            Location from = event.getFrom();
            Location to = event.getTo();
            
            if (to != null && (from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ())) {
                event.setCancelled(true);
                MessageUtils.sendErrorMessage(player, "You are frozen and cannot move.");
            }
        }
        
        // Check if player is jailed
        if (plugin.getJailManager().isPlayerJailed(playerUUID)) {
            // Check if player is trying to leave jail area
            if (!plugin.getJailManager().isInJailArea(player)) {
                // Teleport player back to their assigned jail
                String jailName = plugin.getJailManager().getPlayerJail(playerUUID);
                if (jailName != null) {
                    // Cancel the movement event
                    event.setCancelled(true);
                    
                    // Teleport player back to jail
                    Location jailLocation = plugin.getJailManager().getJailLocation(jailName);
                    if (jailLocation != null) {
                        player.teleport(jailLocation);
                        MessageUtils.sendErrorMessage(player, "You cannot leave the jail area.");
                    }
                }
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        
        // Block teleportation for frozen players
        if (plugin.getPunishmentManager().isPlayerFrozen(playerUUID)) {
            // Staff with bypass permission can still teleport when frozen
            if (!player.hasPermission("frizzlenmod.bypass.freeze")) {
                event.setCancelled(true);
                MessageUtils.sendErrorMessage(player, "You are frozen and cannot teleport.");
                return;
            }
        }
        
        // Block teleportation out of jail for jailed players
        if (plugin.getJailManager().isPlayerJailed(playerUUID)) {
            // Check if target location is outside the jail
            if (!plugin.getJailManager().isLocationInJail(event.getTo())) {
                // Staff with bypass permission can still teleport
                if (!player.hasPermission("frizzlenmod.bypass.jail")) {
                    event.setCancelled(true);
                    MessageUtils.sendErrorMessage(player, "You are jailed and cannot teleport out.");
                }
            }
        }
    }
} 