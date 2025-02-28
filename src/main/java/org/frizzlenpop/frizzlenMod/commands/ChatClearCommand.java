package org.frizzlenpop.frizzlenMod.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenMod.FrizzlenMod;
import org.frizzlenpop.frizzlenMod.utils.MessageUtils;

public class ChatClearCommand implements CommandExecutor {
    
    private final FrizzlenMod plugin;
    
    public ChatClearCommand(FrizzlenMod plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Default to 100 lines cleared
        int lines = 100;
        
        // Check if specific number of lines is specified
        if (args.length > 0) {
            try {
                lines = Integer.parseInt(args[0]);
                if (lines < 1) {
                    lines = 1;
                } else if (lines > 300) {
                    lines = 300; // Cap at 300 lines to prevent abuse
                }
            } catch (NumberFormatException e) {
                MessageUtils.sendErrorMessage(sender, "Invalid number format. Usage: /chatclear [lines]");
                return true;
            }
        }
        
        // Clear chat for all players
        for (Player player : Bukkit.getOnlinePlayers()) {
            // Staff can see who cleared the chat
            if (player.hasPermission("frizzlenmod.staff")) {
                plugin.getChatManager().clearChat(player, lines);
                player.sendMessage("§e" + (sender instanceof Player ? sender.getName() : "Console") + 
                        " §fcleard the chat.");
            } else {
                // Regular players just get a cleared chat
                plugin.getChatManager().clearChat(player, lines);
            }
        }
        
        // Log the action
        plugin.getStorageManager().logModAction(
                sender instanceof Player ? sender.getName() : "Console",
                "ClearChat",
                "GLOBAL",
                "Cleared " + lines + " lines"
        );
        
        // Confirm to the sender
        MessageUtils.sendSuccessMessage(sender, "Cleared " + lines + " lines of chat for all players.");
        
        return true;
    }
} 