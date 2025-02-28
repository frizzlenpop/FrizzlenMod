package org.frizzlenpop.frizzlenMod.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenMod.FrizzlenMod;
import org.frizzlenpop.frizzlenMod.utils.MessageUtils;

import java.util.UUID;

public class UnmuteCommand implements CommandExecutor {
    
    private final FrizzlenMod plugin;
    
    public UnmuteCommand(FrizzlenMod plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            MessageUtils.sendErrorMessage(sender, "Usage: /unmute <player>");
            return true;
        }
        
        String targetName = args[0];
        Player target = Bukkit.getPlayer(targetName);
        
        // Check if the player is online
        if (target == null) {
            // Try to get an offline player
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(targetName);
            if (!offlinePlayer.hasPlayedBefore()) {
                MessageUtils.sendErrorMessage(sender, "Player " + targetName + " has never played on this server.");
                return true;
            }
            
            UUID playerUUID = offlinePlayer.getUniqueId();
            
            // Check if the player is muted
            if (!plugin.getPunishmentManager().isPlayerMuted(playerUUID)) {
                MessageUtils.sendErrorMessage(sender, "Player " + targetName + " is not muted.");
                return true;
            }
            
            // Unmute the player
            plugin.getPunishmentManager().unmutePlayer(playerUUID);
            
            // Notify staff
            String unmuteMessage = "§e" + targetName + " §fhas been unmuted by §e" + 
                    (sender instanceof Player ? sender.getName() : "Console");
            MessageUtils.sendStaffMessage(unmuteMessage);
            
            // Log the action
            plugin.getStorageManager().logModAction(
                    sender.getName(),
                    "Unmute",
                    targetName,
                    "Player unmuted"
            );
            
            // Confirm to the sender
            MessageUtils.sendSuccessMessage(sender, "Unmuted " + targetName);
            
            return true;
        }
        
        // Player is online
        
        // Check if the player is muted
        if (!plugin.getPunishmentManager().isPlayerMuted(target.getUniqueId())) {
            MessageUtils.sendErrorMessage(sender, "Player " + targetName + " is not muted.");
            return true;
        }
        
        // Unmute the player
        plugin.getPunishmentManager().unmutePlayer(target.getUniqueId());
        
        // Notify the player
        target.sendMessage("§aYou have been unmuted by " + 
                (sender instanceof Player ? sender.getName() : "Console"));
        
        // Notify staff
        String unmuteMessage = "§e" + target.getName() + " §fhas been unmuted by §e" + 
                (sender instanceof Player ? sender.getName() : "Console");
        MessageUtils.sendStaffMessage(unmuteMessage);
        
        // Log the action
        plugin.getStorageManager().logModAction(
                sender.getName(),
                "Unmute",
                target.getName(),
                "Player unmuted"
        );
        
        // Confirm to the sender
        MessageUtils.sendSuccessMessage(sender, "Unmuted " + target.getName());
        
        return true;
    }
} 