package org.frizzlenpop.frizzlenMod.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.frizzlenpop.frizzlenMod.FrizzlenMod;
import org.frizzlenpop.frizzlenMod.storage.ModAction;
import org.frizzlenpop.frizzlenMod.utils.MessageUtils;

import java.util.List;

public class ModLogsCommand implements CommandExecutor {
    
    private final FrizzlenMod plugin;
    
    public ModLogsCommand(FrizzlenMod plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("frizzlenmod.modlogs.view")) {
            MessageUtils.sendErrorMessage(sender, "You don't have permission to view moderation logs.");
            return true;
        }
        
        if (args.length < 1) {
            MessageUtils.sendErrorMessage(sender, "Usage: /modlogs <player> [page]");
            return true;
        }
        
        String targetName = args[0];
        int page = 1;
        
        if (args.length > 1) {
            try {
                page = Integer.parseInt(args[1]);
                if (page < 1) {
                    page = 1;
                }
            } catch (NumberFormatException e) {
                MessageUtils.sendErrorMessage(sender, "Invalid page number. Please provide a valid number.");
                return true;
            }
        }
        
        // Number of logs to display per page
        int logsPerPage = 5;
        int startIndex = (page - 1) * logsPerPage;
        
        // Get logs for the player
        List<ModAction> logs = plugin.getStorageManager().getModActionLogs(targetName);
        
        if (logs.isEmpty()) {
            MessageUtils.sendMessage(sender, "§eNo moderation logs found for §f" + targetName + "§e.");
            return true;
        }
        
        int totalPages = (int) Math.ceil(logs.size() / (double) logsPerPage);
        
        if (page > totalPages) {
            MessageUtils.sendErrorMessage(sender, "Page " + page + " does not exist. Total pages: " + totalPages);
            return true;
        }
        
        // Display header
        MessageUtils.sendMessage(sender, "§7§m--------------------§r §e§lModeration Logs for §f" + targetName + 
                " §e§l(Page " + page + "/" + totalPages + ") §7§m--------------------");
        
        // Display logs for the current page
        int endIndex = Math.min(startIndex + logsPerPage, logs.size());
        for (int i = startIndex; i < endIndex; i++) {
            ModAction log = logs.get(i);
            String timeString = log.getTimestamp().toString();
            
            StringBuilder logMessage = new StringBuilder();
            logMessage.append("§7[").append(timeString).append("] ");
            logMessage.append("§e").append(log.getStaffName()).append(" ");
            logMessage.append("§f").append(log.getActionType()).append(" ");
            logMessage.append("§e").append(log.getTargetName()).append(" ");
            
            if (log.getDuration() != null && !log.getDuration().isEmpty()) {
                logMessage.append("§ffor §e").append(log.getDuration()).append(" ");
            }
            
            if (log.getReason() != null && !log.getReason().isEmpty()) {
                logMessage.append("§fReason: §e").append(log.getReason());
            }
            
            MessageUtils.sendMessage(sender, logMessage.toString());
        }
        
        // Display footer with navigation instructions
        MessageUtils.sendMessage(sender, "§7§m--------------------------------------------------");
        if (totalPages > 1) {
            MessageUtils.sendMessage(sender, "§7Use §e/modlogs " + targetName + " <page> §7to navigate between pages.");
        }
        
        return true;
    }
} 