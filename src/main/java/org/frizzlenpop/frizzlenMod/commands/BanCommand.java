package org.frizzlenpop.frizzlenMod.commands;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenMod.FrizzlenMod;
import org.frizzlenpop.frizzlenMod.utils.MessageUtils;

import java.util.Date;

public class BanCommand implements CommandExecutor {
    
    private final FrizzlenMod plugin;
    
    public BanCommand(FrizzlenMod plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2) {
            MessageUtils.sendErrorMessage(sender, "Usage: /ban <player> <reason>");
            return true;
        }
        
        String targetName = args[0];
        
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
        
        // Build the reason from the remaining arguments
        StringBuilder reasonBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
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
        
        // Ban the player (null expiry date means permanent ban)
        Date expiryDate = null;
        banList.addBan(targetName, reason, expiryDate, sender.getName());
        
        // Kick the player if they're online
        if (onlineTarget != null) {
            onlineTarget.kickPlayer("§cYou have been banned from the server.\n§rReason: §f" + reason);
        }
        
        // Notify staff
        String banMessage = "§e" + targetName + " §fhas been permanently banned by §e" + 
                (sender instanceof Player ? sender.getName() : "Console") + 
                " §ffor: §e" + reason;
        MessageUtils.sendStaffMessage(banMessage);
        
        // Log the action
        plugin.getStorageManager().logModAction(
                sender.getName(),
                "Ban",
                targetName,
                reason
        );
        
        // Confirm to the sender
        MessageUtils.sendSuccessMessage(sender, "Banned " + targetName + " for: " + reason);
        
        return true;
    }
} 