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

public class WarnCommand implements CommandExecutor {
    
    private final FrizzlenMod plugin;
    
    public WarnCommand(FrizzlenMod plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2) {
            MessageUtils.sendErrorMessage(sender, "Usage: /warn <player> <reason>");
            return true;
        }
        
        String targetName = args[0];
        Player target = Bukkit.getPlayer(targetName);
        
        // Build the reason
        StringBuilder reasonBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            reasonBuilder.append(args[i]).append(" ");
        }
        String reason = reasonBuilder.toString().trim();
        
        // Check if the player is online
        if (target == null) {
            // Try to get an offline player
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(targetName);
            if (!offlinePlayer.hasPlayedBefore()) {
                MessageUtils.sendErrorMessage(sender, "Player " + targetName + " has never played on this server.");
                return true;
            }
            
            UUID playerUUID = offlinePlayer.getUniqueId();
            
            // Add warning to the player
            int warningCount = plugin.getPunishmentManager().warnPlayer(playerUUID, reason);
            
            // Notify staff
            String warnMessage = "§e" + targetName + " §fhas been warned by §e" + 
                    (sender instanceof Player ? sender.getName() : "Console") + 
                    " §ffor: §e" + reason + " §f(Warning #" + warningCount + ")";
            MessageUtils.sendStaffMessage(warnMessage);
            
            // Log the action
            plugin.getStorageManager().logModAction(
                    sender.getName(),
                    "Warn",
                    targetName,
                    "Warning #" + warningCount + ": " + reason
            );
            
            // Confirm to the sender
            MessageUtils.sendSuccessMessage(sender, "Warned " + targetName + " for: " + reason + " (Warning #" + warningCount + ")");
            
            return true;
        }
        
        // Check if the sender is trying to warn someone with the same or higher permission level
        if (sender instanceof Player && target.hasPermission("frizzlenmod.warn.exempt")) {
            Player playerSender = (Player) sender;
            if (!playerSender.hasPermission("frizzlenmod.admin")) {
                MessageUtils.sendErrorMessage(sender, "You cannot warn this player.");
                return true;
            }
        }
        
        // Add warning to the player
        int warningCount = plugin.getPunishmentManager().warnPlayer(target.getUniqueId(), reason);
        
        // Notify the player
        target.sendMessage("§cYou have been warned by " + 
                (sender instanceof Player ? sender.getName() : "Console") + 
                " for: " + reason + " (Warning #" + warningCount + ")");
        
        // Check if automatic actions should be taken based on warning count
        checkAutomaticActions(target, warningCount, sender);
        
        // Notify staff
        String warnMessage = "§e" + target.getName() + " §fhas been warned by §e" + 
                (sender instanceof Player ? sender.getName() : "Console") + 
                " §ffor: §e" + reason + " §f(Warning #" + warningCount + ")";
        MessageUtils.sendStaffMessage(warnMessage);
        
        // Log the action
        plugin.getStorageManager().logModAction(
                sender.getName(),
                "Warn",
                target.getName(),
                "Warning #" + warningCount + ": " + reason
        );
        
        // Confirm to the sender
        MessageUtils.sendSuccessMessage(sender, "Warned " + target.getName() + " for: " + reason + " (Warning #" + warningCount + ")");
        
        return true;
    }
    
    /**
     * Check if automatic actions should be taken based on warning count
     * @param player The player to check
     * @param warningCount The current warning count
     * @param sender The command sender
     */
    private void checkAutomaticActions(Player player, int warningCount, CommandSender sender) {
        // Get the configuration for automatic actions
        int kickThreshold = plugin.getConfig().getInt("warnings.kick-threshold", 3);
        int tempBanThreshold = plugin.getConfig().getInt("warnings.temp-ban-threshold", 5);
        int banThreshold = plugin.getConfig().getInt("warnings.ban-threshold", 7);
        
        String senderName = sender instanceof Player ? sender.getName() : "Console";
        
        if (warningCount >= banThreshold) {
            // Permanent ban
            String banReason = "Automatic ban after " + warningCount + " warnings";
            
            // Add to ban list
            Bukkit.getBanList(org.bukkit.BanList.Type.NAME).addBan(
                    player.getName(), 
                    banReason, 
                    null, 
                    senderName
            );
            
            // Kick the player
            player.kickPlayer("§cYou have been permanently banned: " + banReason);
            
            // Notify staff
            MessageUtils.sendStaffMessage("§e" + player.getName() + " §fhas been automatically banned after reaching " + warningCount + " warnings.");
            
            // Log the action
            plugin.getStorageManager().logModAction(
                    "SYSTEM",
                    "AutoBan",
                    player.getName(),
                    banReason
            );
        } else if (warningCount >= tempBanThreshold) {
            // Temporary ban (1 day)
            String banReason = "Automatic temporary ban after " + warningCount + " warnings";
            long banDuration = 24 * 60 * 60 * 1000L; // 1 day in milliseconds
            
            // Temp ban the player using Bukkit's ban system instead
            java.util.Date expiry = new java.util.Date(System.currentTimeMillis() + banDuration);
            Bukkit.getBanList(org.bukkit.BanList.Type.NAME).addBan(
                    player.getName(),
                    banReason,
                    expiry,
                    senderName
            );
            
            // Kick the player
            player.kickPlayer("§cYou have been temporarily banned for 1 day: " + banReason);
            
            // Notify staff
            MessageUtils.sendStaffMessage("§e" + player.getName() + " §fhas been automatically temp-banned for 1 day after reaching " + warningCount + " warnings.");
            
            // Log the action
            plugin.getStorageManager().logModAction(
                    "SYSTEM",
                    "AutoTempBan",
                    player.getName(),
                    banReason + " (1 day)"
            );
        } else if (warningCount >= kickThreshold) {
            // Kick
            String kickReason = "Automatic kick after " + warningCount + " warnings";
            
            // Kick the player
            player.kickPlayer("§cYou have been kicked: " + kickReason);
            
            // Notify staff
            MessageUtils.sendStaffMessage("§e" + player.getName() + " §fhas been automatically kicked after reaching " + warningCount + " warnings.");
            
            // Log the action
            plugin.getStorageManager().logModAction(
                    "SYSTEM",
                    "AutoKick",
                    player.getName(),
                    kickReason
            );
        }
    }
} 