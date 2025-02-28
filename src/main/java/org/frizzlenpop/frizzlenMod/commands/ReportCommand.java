package org.frizzlenpop.frizzlenMod.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenMod.FrizzlenMod;
import org.frizzlenpop.frizzlenMod.utils.MessageUtils;

public class ReportCommand implements CommandExecutor {
    
    private final FrizzlenMod plugin;
    
    public ReportCommand(FrizzlenMod plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2) {
            MessageUtils.sendErrorMessage(sender, "Usage: /report <player> <reason>");
            return true;
        }
        
        String reporterName = sender instanceof Player ? sender.getName() : "Console";
        String targetName = args[0];
        
        // Build the reason from the remaining arguments
        StringBuilder reasonBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            reasonBuilder.append(args[i]).append(" ");
        }
        String reason = reasonBuilder.toString().trim();
        
        // Check if the player exists on the server
        boolean playerExists = false;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getName().equalsIgnoreCase(targetName)) {
                playerExists = true;
                targetName = player.getName(); // Get the correct capitalization
                break;
            }
        }
        
        if (!playerExists) {
            // Check offline players
            playerExists = Bukkit.getOfflinePlayer(targetName).hasPlayedBefore();
        }
        
        if (!playerExists) {
            MessageUtils.sendErrorMessage(sender, "Player " + targetName + " has never played on this server.");
            return true;
        }
        
        // Add the report
        String reportId = plugin.getStorageManager().addReport(reporterName, targetName, reason);
        
        if (reportId != null) {
            // Notify staff
            String reportMessage = "§c§l[REPORT] §e" + reporterName + " §fhas reported §e" + 
                    targetName + " §ffor: §e" + reason + " §f(ID: §e" + reportId + "§f)";
            MessageUtils.broadcastToPermission("frizzlenmod.reports.view", reportMessage);
            
            // Confirm to the sender
            MessageUtils.sendSuccessMessage(sender, "Your report has been submitted (ID: " + reportId + ").");
            MessageUtils.sendMessage(sender, "Staff will review your report as soon as possible.");
        } else {
            MessageUtils.sendErrorMessage(sender, "Failed to submit report. Please try again later.");
        }
        
        return true;
    }
} 