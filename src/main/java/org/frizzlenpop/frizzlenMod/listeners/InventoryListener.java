package org.frizzlenpop.frizzlenMod.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.frizzlenpop.frizzlenMod.FrizzlenMod;
import org.frizzlenpop.frizzlenMod.utils.MessageUtils;

public class InventoryListener implements Listener {
    
    private final FrizzlenMod plugin;
    
    public InventoryListener(FrizzlenMod plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        
        // Block inventory interactions for frozen players
        if (plugin.getPunishmentManager().isPlayerFrozen(player.getUniqueId())) {
            if (!player.hasPermission("frizzlenmod.bypass.freeze")) {
                event.setCancelled(true);
                MessageUtils.sendErrorMessage(player, "You are frozen and cannot interact with your inventory.");
            }
        }
        
        // Optionally restrict jailed players' inventory access
        if (plugin.getConfig().getBoolean("jail.restrict-inventory", true)) {
            if (plugin.getJailManager().isPlayerJailed(player.getUniqueId())) {
                if (!player.hasPermission("frizzlenmod.bypass.jail")) {
                    event.setCancelled(true);
                    MessageUtils.sendErrorMessage(player, "You are jailed and cannot interact with your inventory.");
                }
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryInteract(InventoryInteractEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        
        // Block inventory interactions for frozen players
        if (plugin.getPunishmentManager().isPlayerFrozen(player.getUniqueId())) {
            if (!player.hasPermission("frizzlenmod.bypass.freeze")) {
                event.setCancelled(true);
            }
        }
        
        // Optionally restrict jailed players' inventory access
        if (plugin.getConfig().getBoolean("jail.restrict-inventory", true)) {
            if (plugin.getJailManager().isPlayerJailed(player.getUniqueId())) {
                if (!player.hasPermission("frizzlenmod.bypass.jail")) {
                    event.setCancelled(true);
                }
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getPlayer();
        
        // Block opening inventories for frozen players
        if (plugin.getPunishmentManager().isPlayerFrozen(player.getUniqueId())) {
            if (!player.hasPermission("frizzlenmod.bypass.freeze")) {
                event.setCancelled(true);
                MessageUtils.sendErrorMessage(player, "You are frozen and cannot open inventories.");
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        
        // Block item dropping for frozen players
        if (plugin.getPunishmentManager().isPlayerFrozen(player.getUniqueId())) {
            if (!player.hasPermission("frizzlenmod.bypass.freeze")) {
                event.setCancelled(true);
                MessageUtils.sendErrorMessage(player, "You are frozen and cannot drop items.");
            }
        }
        
        // Optionally restrict jailed players from dropping items
        if (plugin.getConfig().getBoolean("jail.restrict-item-drop", true)) {
            if (plugin.getJailManager().isPlayerJailed(player.getUniqueId())) {
                if (!player.hasPermission("frizzlenmod.bypass.jail")) {
                    event.setCancelled(true);
                    MessageUtils.sendErrorMessage(player, "You are jailed and cannot drop items.");
                }
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        
        // Block interactions for frozen players
        if (plugin.getPunishmentManager().isPlayerFrozen(player.getUniqueId())) {
            if (!player.hasPermission("frizzlenmod.bypass.freeze")) {
                event.setCancelled(true);
                
                // Don't spam messages for continuous interactions
                if (event.getAction().name().contains("RIGHT_CLICK")) {
                    MessageUtils.sendErrorMessage(player, "You are frozen and cannot interact with the world.");
                }
            }
        }
        
        // Optionally restrict jailed players from interacting
        if (plugin.getConfig().getBoolean("jail.restrict-interaction", true)) {
            if (plugin.getJailManager().isPlayerJailed(player.getUniqueId())) {
                if (!player.hasPermission("frizzlenmod.bypass.jail")) {
                    event.setCancelled(true);
                    
                    // Don't spam messages for continuous interactions
                    if (event.getAction().name().contains("RIGHT_CLICK")) {
                        MessageUtils.sendErrorMessage(player, "You are jailed and cannot interact with the world.");
                    }
                }
            }
        }
    }
} 