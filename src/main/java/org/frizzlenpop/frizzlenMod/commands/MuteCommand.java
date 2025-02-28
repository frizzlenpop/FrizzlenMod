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

public class MuteCommand implements CommandExecutor {
    
    private final FrizzlenMod plugin;
    
    public MuteCommand(FrizzlenMod plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            MessageUtils.sendErrorMessage(sender, "Usage: /mute <player> [reason]");
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
            
            // Mute the offline player
            UUID playerUUID = offlinePlayer.getUniqueId();
            
            // Check if already muted
            if (plugin.getPunishmentManager().isPlayerMuted(playerUUID)) {
                MessageUtils.sendErrorMessage(sender, "Player " + targetName + " is already muted.");
                return true;
            }
            
            // Build the reason if provided
            String reason = "No reason specified";
            if (args.length > 1) {
                StringBuilder reasonBuilder = new StringBuilder();
                for (int i = 1; i < args.length; i++) {
                    reasonBuilder.append(args[i]).append(" ");
                }
                reason = reasonBuilder.toString().trim();
            }
            
            // Mute the player
            plugin.getPunishmentManager().mutePlayer(playerUUID);
            
            // Notify staff
            String muteMessage = "§e" + targetName + " §fhas been permanently muted by §e" + 
                    (sender instanceof Player ? sender.getName() : "Console") + 
                    " §ffor: §e" + reason;
            MessageUtils.sendStaffMessage(muteMessage);
            
            // Log the action
            plugin.getStorageManager().logModAction(
                    sender.getName(),
                    "Mute",
                    targetName,
                    reason
            );
            
            // Confirm to the sender
            MessageUtils.sendSuccessMessage(sender, "Permanently muted " + targetName + " for: " + reason);
            
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
        if (args.length > 1) {
            StringBuilder reasonBuilder = new StringBuilder();
            for (int i = 1; i < args.length; i++) {
                reasonBuilder.append(args[i]).append(" ");
            }
            reason = reasonBuilder.toString().trim();
        }
        
        // Mute the player
        plugin.getPunishmentManager().mutePlayer(target.getUniqueId());
        
        // Notify the player
        target.sendMessage("§cYou have been permanently muted by " + 
                (sender instanceof Player ? sender.getName() : "Console") + 
                " for: " + reason);
        
        // Notify staff
        String muteMessage = "§e" + target.getName() + " §fhas been permanently muted by §e" + 
                (sender instanceof Player ? sender.getName() : "Console") + 
                " §ffor: §e" + reason;
        MessageUtils.sendStaffMessage(muteMessage);
        
        // Log the action
        plugin.getStorageManager().logModAction(
                sender.getName(),
                "Mute",
                target.getName(),
                reason
        );
        
        // Confirm to the sender
        MessageUtils.sendSuccessMessage(sender, "Permanently muted " + target.getName() + " for: " + reason);
        
        return true;
    }
} 