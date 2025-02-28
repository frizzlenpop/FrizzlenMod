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

public class ClearWarningsCommand implements CommandExecutor {
    
    private final FrizzlenMod plugin;
    
    public ClearWarningsCommand(FrizzlenMod plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            MessageUtils.sendErrorMessage(sender, "Usage: /clearwarnings <player>");
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
            
            // Get current warning count
            int warningCount = plugin.getPunishmentManager().getPlayerWarnings(playerUUID);
            
            if (warningCount == 0) {
                MessageUtils.sendErrorMessage(sender, "Player " + targetName + " has no warnings to clear.");
                return true;
            }
            
            // Clear warnings
            plugin.getPunishmentManager().clearPlayerWarnings(playerUUID);
            
            // Notify staff
            String clearMessage = "§e" + targetName + "'s §fwarnings have been cleared by §e" + 
                    (sender instanceof Player ? sender.getName() : "Console") + 
                    " §f(Cleared " + warningCount + " warnings)";
            MessageUtils.sendStaffMessage(clearMessage);
            
            // Log the action
            plugin.getStorageManager().logModAction(
                    sender.getName(),
                    "ClearWarnings",
                    targetName,
                    "Cleared " + warningCount + " warnings"
            );
            
            // Confirm to the sender
            MessageUtils.sendSuccessMessage(sender, "Cleared " + warningCount + " warnings from " + targetName);
            
            return true;
        }
        
        // Player is online
        
        // Get current warning count
        int warningCount = plugin.getPunishmentManager().getPlayerWarnings(target.getUniqueId());
        
        if (warningCount == 0) {
            MessageUtils.sendErrorMessage(sender, "Player " + targetName + " has no warnings to clear.");
            return true;
        }
        
        // Clear warnings
        plugin.getPunishmentManager().clearPlayerWarnings(target.getUniqueId());
        
        // Notify the player
        target.sendMessage("§aYour warnings have been cleared by " + 
                (sender instanceof Player ? sender.getName() : "Console"));
        
        // Notify staff
        String clearMessage = "§e" + target.getName() + "'s §fwarnings have been cleared by §e" + 
                (sender instanceof Player ? sender.getName() : "Console") + 
                " §f(Cleared " + warningCount + " warnings)";
        MessageUtils.sendStaffMessage(clearMessage);
        
        // Log the action
        plugin.getStorageManager().logModAction(
                sender.getName(),
                "ClearWarnings",
                target.getName(),
                "Cleared " + warningCount + " warnings"
        );
        
        // Confirm to the sender
        MessageUtils.sendSuccessMessage(sender, "Cleared " + warningCount + " warnings from " + target.getName());
        
        return true;
    }
} 