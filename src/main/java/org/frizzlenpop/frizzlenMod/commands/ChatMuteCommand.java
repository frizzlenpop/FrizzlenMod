package org.frizzlenpop.frizzlenMod.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenMod.FrizzlenMod;
import org.frizzlenpop.frizzlenMod.utils.MessageUtils;

public class ChatMuteCommand implements CommandExecutor {
    
    private final FrizzlenMod plugin;
    
    public ChatMuteCommand(FrizzlenMod plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Toggle chat mute state
        boolean wasMuted = plugin.getChatManager().isChatMuted();
        plugin.getChatManager().setChatMuted(!wasMuted);
        
        // Get reason if provided
        String reason = "No reason specified";
        if (args.length > 0) {
            StringBuilder reasonBuilder = new StringBuilder();
            for (String arg : args) {
                reasonBuilder.append(arg).append(" ");
            }
            reason = reasonBuilder.toString().trim();
        }
        
        // Broadcast notification to all players
        if (wasMuted) {
            // Chat was unmuted
            Bukkit.broadcastMessage("§a§lServer chat has been unmuted by " + 
                    (sender instanceof Player ? sender.getName() : "Console"));
            
            // Log the action
            plugin.getStorageManager().logModAction(
                    sender instanceof Player ? sender.getName() : "Console",
                    "UnmuteChat",
                    "GLOBAL",
                    "Server chat unmuted"
            );
        } else {
            // Chat was muted
            Bukkit.broadcastMessage("§c§lServer chat has been muted by " + 
                    (sender instanceof Player ? sender.getName() : "Console") + 
                    " for: §f" + reason);
            
            // Log the action
            plugin.getStorageManager().logModAction(
                    sender instanceof Player ? sender.getName() : "Console",
                    "MuteChat",
                    "GLOBAL",
                    reason
            );
        }
        
        return true;
    }
} 