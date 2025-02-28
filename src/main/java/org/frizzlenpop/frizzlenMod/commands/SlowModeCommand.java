package org.frizzlenpop.frizzlenMod.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenMod.FrizzlenMod;
import org.frizzlenpop.frizzlenMod.utils.MessageUtils;

public class SlowModeCommand implements CommandExecutor {
    
    private final FrizzlenMod plugin;
    
    public SlowModeCommand(FrizzlenMod plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            MessageUtils.sendErrorMessage(sender, "Usage: /slowmode <seconds> [reason]");
            return true;
        }
        
        int seconds;
        try {
            seconds = Integer.parseInt(args[0]);
            if (seconds < 0) {
                MessageUtils.sendErrorMessage(sender, "Slow mode time cannot be negative.");
                return true;
            }
            
            // Cap at a reasonable maximum to prevent abuse
            if (seconds > 3600) {
                seconds = 3600; // Max 1 hour
            }
        } catch (NumberFormatException e) {
            MessageUtils.sendErrorMessage(sender, "Invalid number format. Usage: /slowmode <seconds> [reason]");
            return true;
        }
        
        // Build reason if provided
        String reason = "No reason specified";
        if (args.length > 1) {
            StringBuilder reasonBuilder = new StringBuilder();
            for (int i = 1; i < args.length; i++) {
                reasonBuilder.append(args[i]).append(" ");
            }
            reason = reasonBuilder.toString().trim();
        }
        
        // Set slow mode
        plugin.getChatManager().setSlowMode(seconds);
        
        // Broadcast to all players
        if (seconds == 0) {
            // Slow mode disabled
            Bukkit.broadcastMessage("§a§lChat slow mode has been disabled by " + 
                    (sender instanceof Player ? sender.getName() : "Console"));
            
            // Log the action
            plugin.getStorageManager().logModAction(
                    sender instanceof Player ? sender.getName() : "Console",
                    "SlowModeOff",
                    "GLOBAL",
                    "Slow mode disabled"
            );
        } else {
            // Slow mode enabled/changed
            Bukkit.broadcastMessage("§e§lChat slow mode has been set to " + seconds + " seconds by " + 
                    (sender instanceof Player ? sender.getName() : "Console") + 
                    " for: §f" + reason);
            
            // Log the action
            plugin.getStorageManager().logModAction(
                    sender instanceof Player ? sender.getName() : "Console",
                    "SlowModeOn",
                    "GLOBAL",
                    "Set to " + seconds + " seconds, Reason: " + reason
            );
        }
        
        return true;
    }
} 