package org.frizzlenpop.frizzlenMod.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenMod.FrizzlenMod;
import org.frizzlenpop.frizzlenMod.utils.MessageUtils;
import org.frizzlenpop.frizzlenMod.utils.TimeUtils;

import java.util.Map;

public class JailCommand implements CommandExecutor {
    
    private final FrizzlenMod plugin;
    
    public JailCommand(FrizzlenMod plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2) {
            MessageUtils.sendErrorMessage(sender, "Usage: /jail <player> <jail> [time] [reason]");
            
            // List available jails
            Map<String, org.bukkit.Location> jails = plugin.getJailManager().getJails();
            if (!jails.isEmpty()) {
                StringBuilder jailList = new StringBuilder("Available jails: ");
                boolean first = true;
                for (String jailName : jails.keySet()) {
                    if (!first) {
                        jailList.append(", ");
                    }
                    jailList.append(jailName);
                    first = false;
                }
                MessageUtils.sendMessage(sender, jailList.toString());
            } else {
                MessageUtils.sendMessage(sender, "No jails have been set up. Use /setjail to create one.");
            }
            
            return true;
        }
        
        String targetName = args[0];
        String jailName = args[1].toLowerCase();
        
        // Check if the player is online
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            MessageUtils.sendErrorMessage(sender, "Player " + targetName + " is not online.");
            return true;
        }
        
        // Check if the jail exists
        if (!plugin.getJailManager().jailExists(jailName)) {
            MessageUtils.sendErrorMessage(sender, "Jail '" + jailName + "' does not exist.");
            return true;
        }
        
        // Check if the sender is trying to jail someone with the same or higher permission level
        if (sender instanceof Player && target.hasPermission("frizzlenmod.jail.exempt")) {
            Player playerSender = (Player) sender;
            if (!playerSender.hasPermission("frizzlenmod.admin")) {
                MessageUtils.sendErrorMessage(sender, "You cannot jail this player.");
                return true;
            }
        }
        
        // Check if they're already jailed
        if (plugin.getJailManager().isPlayerJailed(target.getUniqueId())) {
            MessageUtils.sendErrorMessage(sender, "Player " + targetName + " is already jailed.");
            return true;
        }
        
        // Parse jail time if provided
        long durationMinutes = 0;
        int reasonIndex = 2;
        
        if (args.length > 2) {
            try {
                // Try to parse the third argument as a time duration
                durationMinutes = TimeUtils.parseTimeString(args[2]) / (60 * 1000); // Convert ms to minutes
                reasonIndex = 3;
            } catch (Exception e) {
                // If not a valid time, assume it's the start of the reason
                durationMinutes = 0;
                reasonIndex = 2;
            }
        }
        
        // Build the reason if provided
        String reason = "No reason specified";
        if (args.length > reasonIndex) {
            StringBuilder reasonBuilder = new StringBuilder();
            for (int i = reasonIndex; i < args.length; i++) {
                reasonBuilder.append(args[i]).append(" ");
            }
            reason = reasonBuilder.toString().trim();
        }
        
        boolean success;
        if (durationMinutes > 0) {
            // Temporary jail
            success = plugin.getJailManager().tempJailPlayer(target, jailName, durationMinutes);
            
            if (success) {
                String formattedTime = TimeUtils.formatTime(durationMinutes * 60 * 1000);
                
                // Notify the player
                target.sendMessage("§c§lYou have been jailed in '" + jailName + "' for " + formattedTime + 
                        " by " + (sender instanceof Player ? sender.getName() : "Console") + 
                        " for: " + reason);
                
                // Notify staff
                String jailMessage = "§e" + target.getName() + " §fhas been temporarily jailed in §e" + 
                        jailName + " §ffor §e" + formattedTime + " §fby §e" + 
                        (sender instanceof Player ? sender.getName() : "Console") + 
                        " §ffor: §e" + reason;
                MessageUtils.sendStaffMessage(jailMessage);
                
                // Log the action
                plugin.getStorageManager().logModAction(
                        sender instanceof Player ? sender.getName() : "Console",
                        "TempJail",
                        target.getName(),
                        "Jailed in " + jailName + " for " + formattedTime + ", Reason: " + reason
                );
                
                // Confirm to the sender
                MessageUtils.sendSuccessMessage(sender, "Temporarily jailed " + target.getName() + 
                        " in '" + jailName + "' for " + formattedTime + ". Reason: " + reason);
            }
        } else {
            // Permanent jail
            success = plugin.getJailManager().jailPlayer(target, jailName);
            
            if (success) {
                // Notify the player
                target.sendMessage("§c§lYou have been permanently jailed in '" + jailName + 
                        "' by " + (sender instanceof Player ? sender.getName() : "Console") + 
                        " for: " + reason);
                
                // Notify staff
                String jailMessage = "§e" + target.getName() + " §fhas been permanently jailed in §e" + 
                        jailName + " §fby §e" + 
                        (sender instanceof Player ? sender.getName() : "Console") + 
                        " §ffor: §e" + reason;
                MessageUtils.sendStaffMessage(jailMessage);
                
                // Log the action
                plugin.getStorageManager().logModAction(
                        sender instanceof Player ? sender.getName() : "Console",
                        "Jail",
                        target.getName(),
                        "Permanently jailed in " + jailName + ", Reason: " + reason
                );
                
                // Confirm to the sender
                MessageUtils.sendSuccessMessage(sender, "Permanently jailed " + target.getName() + 
                        " in '" + jailName + "'. Reason: " + reason);
            }
        }
        
        if (!success) {
            MessageUtils.sendErrorMessage(sender, "Failed to jail player. Please try again.");
        }
        
        return true;
    }
} 