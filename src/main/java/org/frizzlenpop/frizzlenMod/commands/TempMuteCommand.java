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

public class TempMuteCommand implements CommandExecutor {
    
    private final FrizzlenMod plugin;
    
    public TempMuteCommand(FrizzlenMod plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2) {
            MessageUtils.sendErrorMessage(sender, "Usage: /tempmute <player> <duration> [reason]");
            return true;
        }
        
        String targetName = args[0];
        String durationString = args[1];
        
        // Parse the duration
        long durationMillis;
        try {
            // Assuming the plugin has a utility method to parse time strings like "1d2h3m"
            // If not, we'll need to implement this logic directly here
            durationMillis = parseTimeString(durationString);
            if (durationMillis <= 0) {
                MessageUtils.sendErrorMessage(sender, "Invalid duration. Use format: 1d2h3m (days, hours, minutes)");
                return true;
            }
        } catch (IllegalArgumentException e) {
            MessageUtils.sendErrorMessage(sender, "Invalid duration format. Use format: 1d2h3m (days, hours, minutes)");
            return true;
        }
        
        // Format the duration for display
        String formattedDuration = formatTimeString(durationMillis);
        
        Player target = Bukkit.getPlayer(targetName);
        
        // Check if the player is online
        if (target == null) {
            // Try to get an offline player
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(targetName);
            if (!offlinePlayer.hasPlayedBefore()) {
                MessageUtils.sendErrorMessage(sender, "Player " + targetName + " has never played on this server.");
                return true;
            }
            
            // Mute the offline player
            UUID playerUUID = offlinePlayer.getUniqueId();
            
            // Check if already muted
            if (plugin.getPunishmentManager().isPlayerMuted(playerUUID)) {
                MessageUtils.sendErrorMessage(sender, "Player " + targetName + " is already muted.");
                return true;
            }
            
            // Build the reason if provided
            String reason = "No reason specified";
            if (args.length > 2) {
                StringBuilder reasonBuilder = new StringBuilder();
                for (int i = 2; i < args.length; i++) {
                    reasonBuilder.append(args[i]).append(" ");
                }
                reason = reasonBuilder.toString().trim();
            }
            
            // Temp mute the player
            plugin.getPunishmentManager().tempMutePlayer(playerUUID, durationMillis);
            
            // Notify staff
            String muteMessage = "§e" + targetName + " §fhas been temporarily muted for §e" + formattedDuration + 
                    " §fby §e" + (sender instanceof Player ? sender.getName() : "Console") + 
                    " §ffor: §e" + reason;
            MessageUtils.sendStaffMessage(muteMessage);
            
            // Log the action
            plugin.getStorageManager().logModAction(
                    sender.getName(),
                    "TempMute",
                    targetName,
                    "Duration: " + formattedDuration + ", Reason: " + reason
            );
            
            // Confirm to the sender
            MessageUtils.sendSuccessMessage(sender, "Temporarily muted " + targetName + " for " + formattedDuration + ". Reason: " + reason);
            
            return true;
        }
        
        // Check if the sender is trying to mute someone with the same or higher permission level
        if (sender instanceof Player && target.hasPermission("frizzlenmod.mute.exempt")) {
            Player playerSender = (Player) sender;
            if (!playerSender.hasPermission("frizzlenmod.admin")) {
                MessageUtils.sendErrorMessage(sender, "You cannot mute this player.");
                return true;
            }
        }
        
        // Check if already muted
        if (plugin.getPunishmentManager().isPlayerMuted(target.getUniqueId())) {
            MessageUtils.sendErrorMessage(sender, "Player " + targetName + " is already muted.");
            return true;
        }
        
        // Build the reason if provided
        String reason = "No reason specified";
        if (args.length > 2) {
            StringBuilder reasonBuilder = new StringBuilder();
            for (int i = 2; i < args.length; i++) {
                reasonBuilder.append(args[i]).append(" ");
            }
            reason = reasonBuilder.toString().trim();
        }
        
        // Temp mute the player
        plugin.getPunishmentManager().tempMutePlayer(target.getUniqueId(), durationMillis);
        
        // Notify the player
        target.sendMessage("§cYou have been temporarily muted for " + formattedDuration + 
                " by " + (sender instanceof Player ? sender.getName() : "Console") + 
                " for: " + reason);
        
        // Notify staff
        String muteMessage = "§e" + target.getName() + " §fhas been temporarily muted for §e" + formattedDuration + 
                " §fby §e" + (sender instanceof Player ? sender.getName() : "Console") + 
                " §ffor: §e" + reason;
        MessageUtils.sendStaffMessage(muteMessage);
        
        // Log the action
        plugin.getStorageManager().logModAction(
                sender.getName(),
                "TempMute",
                target.getName(),
                "Duration: " + formattedDuration + ", Reason: " + reason
        );
        
        // Confirm to the sender
        MessageUtils.sendSuccessMessage(sender, "Temporarily muted " + target.getName() + " for " + formattedDuration + ". Reason: " + reason);
        
        return true;
    }
    
    /**
     * Parse a time string like "1d2h3m" into milliseconds
     * @param timeString The time string to parse
     * @return The duration in milliseconds
     */
    private long parseTimeString(String timeString) {
        long totalMillis = 0;
        StringBuilder numBuilder = new StringBuilder();
        
        for (int i = 0; i < timeString.length(); i++) {
            char c = timeString.charAt(i);
            
            if (Character.isDigit(c)) {
                numBuilder.append(c);
            } else {
                if (numBuilder.length() == 0) {
                    throw new IllegalArgumentException("Invalid time format");
                }
                
                int num = Integer.parseInt(numBuilder.toString());
                numBuilder = new StringBuilder();
                
                switch (c) {
                    case 'd':
                        totalMillis += num * 24 * 60 * 60 * 1000L; // days to millis
                        break;
                    case 'h':
                        totalMillis += num * 60 * 60 * 1000L; // hours to millis
                        break;
                    case 'm':
                        totalMillis += num * 60 * 1000L; // minutes to millis
                        break;
                    case 's':
                        totalMillis += num * 1000L; // seconds to millis
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid time unit: " + c);
                }
            }
        }
        
        // If there are trailing digits without a unit, throw an exception
        if (numBuilder.length() > 0) {
            throw new IllegalArgumentException("Time unit missing for value: " + numBuilder);
        }
        
        return totalMillis;
    }
    
    /**
     * Format a duration in milliseconds to a human-readable string
     * @param millis The duration in milliseconds
     * @return A formatted string like "1d 2h 3m"
     */
    private String formatTimeString(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        seconds %= 60;
        minutes %= 60;
        hours %= 24;
        
        StringBuilder result = new StringBuilder();
        
        if (days > 0) {
            result.append(days).append("d ");
        }
        if (hours > 0) {
            result.append(hours).append("h ");
        }
        if (minutes > 0) {
            result.append(minutes).append("m ");
        }
        if (seconds > 0 && days == 0 && hours == 0) {
            result.append(seconds).append("s");
        }
        
        return result.toString().trim();
    }
} 