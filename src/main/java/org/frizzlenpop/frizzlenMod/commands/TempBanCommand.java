package org.frizzlenpop.frizzlenMod.commands;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenMod.FrizzlenMod;
import org.frizzlenpop.frizzlenMod.utils.MessageUtils;
import org.frizzlenpop.frizzlenMod.utils.TimeUtils;

import java.util.Date;

public class TempBanCommand implements CommandExecutor {
    
    private final FrizzlenMod plugin;
    
    public TempBanCommand(FrizzlenMod plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 3) {
            MessageUtils.sendErrorMessage(sender, "Usage: /tempban <player> <time> <reason>");
            return true;
        }
        
        String targetName = args[0];
        String timeString = args[1];
        
        // Check if the player is exempt from bans
        Player onlineTarget = Bukkit.getPlayer(targetName);
        if (onlineTarget != null && onlineTarget.hasPermission("frizzlenmod.ban.exempt")) {
            if (sender instanceof Player) {
                Player playerSender = (Player) sender;
                if (!playerSender.hasPermission("frizzlenmod.admin")) {
                    MessageUtils.sendErrorMessage(sender, "You cannot ban this player.");
                    return true;
                }
            }
        }
        
        // Parse the time duration
        long durationMillis = TimeUtils.parseTimeString(timeString);
        if (durationMillis <= 0) {
            MessageUtils.sendErrorMessage(sender, "Invalid time format. Use values like 1d, 12h, 30m, etc.");
            return true;
        }
        
        // Calculate expiry date
        Date expiryDate = new Date(System.currentTimeMillis() + durationMillis);
        
        // Build the reason from the remaining arguments
        StringBuilder reasonBuilder = new StringBuilder();
        for (int i = 2; i < args.length; i++) {
            reasonBuilder.append(args[i]).append(" ");
        }
        String reason = reasonBuilder.toString().trim();
        
        // Get the ban list
        BanList banList = Bukkit.getBanList(BanList.Type.NAME);
        
        // Check if the player is already banned
        if (banList.isBanned(targetName)) {
            MessageUtils.sendErrorMessage(sender, "Player " + targetName + " is already banned.");
            return true;
        }
        
        // Ban the player
        banList.addBan(targetName, reason, expiryDate, sender.getName());
        
        // Kick the player if they're online
        if (onlineTarget != null) {
            onlineTarget.kickPlayer("§cYou have been temporarily banned from the server.\n§rReason: §f" + reason + 
                    "\n§rExpires in: §f" + TimeUtils.formatTime(durationMillis));
        }
        
        // Notify staff
        String banMessage = "§e" + targetName + " §fhas been temporarily banned by §e" + 
                (sender instanceof Player ? sender.getName() : "Console") + 
                " §ffor §e" + TimeUtils.formatTime(durationMillis) + 
                " §ffor: §e" + reason;
        MessageUtils.sendStaffMessage(banMessage);
        
        // Log the action
        plugin.getStorageManager().logModAction(
                sender.getName(),
                "TempBan",
                targetName,
                "Duration: " + TimeUtils.formatTime(durationMillis) + ", Reason: " + reason
        );
        
        // Confirm to the sender
        MessageUtils.sendSuccessMessage(sender, "Temporarily banned " + targetName + " for " + 
                TimeUtils.formatTime(durationMillis) + " for: " + reason);
        
        return true;
    }
} 