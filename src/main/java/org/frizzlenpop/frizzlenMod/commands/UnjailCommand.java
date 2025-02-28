package org.frizzlenpop.frizzlenMod.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenMod.FrizzlenMod;
import org.frizzlenpop.frizzlenMod.utils.MessageUtils;

import java.util.UUID;

public class UnjailCommand implements CommandExecutor {
    
    private final FrizzlenMod plugin;
    
    public UnjailCommand(FrizzlenMod plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            MessageUtils.sendErrorMessage(sender, "Usage: /unjail <player>");
            return true;
        }
        
        String targetName = args[0];
        Player target = Bukkit.getPlayer(targetName);
        
        if (target == null) {
            MessageUtils.sendErrorMessage(sender, "Player " + targetName + " is not online.");
            return true;
        }
        
        UUID playerUUID = target.getUniqueId();
        
        // Check if the player is actually jailed
        if (!plugin.getJailManager().isPlayerJailed(playerUUID)) {
            MessageUtils.sendErrorMessage(sender, "Player " + targetName + " is not jailed.");
            return true;
        }
        
        // Get the jail name for logging
        String jailName = plugin.getJailManager().getPlayerJail(playerUUID);
        
        // Release the player
        boolean success = plugin.getJailManager().unjailPlayer(playerUUID);
        
        if (success) {
            // Notify the player
            target.sendMessage("§a§lYou have been released from jail by " + 
                    (sender instanceof Player ? sender.getName() : "Console"));
            
            // Notify staff
            String unjailMessage = "§e" + target.getName() + " §fhas been released from jail by §e" + 
                    (sender instanceof Player ? sender.getName() : "Console");
            MessageUtils.sendStaffMessage(unjailMessage);
            
            // Log the action
            plugin.getStorageManager().logModAction(
                    sender instanceof Player ? sender.getName() : "Console",
                    "Unjail",
                    target.getName(),
                    "Released from jail: " + jailName
            );
            
            // Confirm to the sender
            MessageUtils.sendSuccessMessage(sender, "Released " + target.getName() + " from jail.");
        } else {
            MessageUtils.sendErrorMessage(sender, "Failed to release player. Please try again.");
        }
        
        return true;
    }
} 