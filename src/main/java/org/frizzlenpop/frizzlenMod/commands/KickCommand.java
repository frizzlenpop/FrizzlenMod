package org.frizzlenpop.frizzlenMod.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenMod.FrizzlenMod;
import org.frizzlenpop.frizzlenMod.utils.MessageUtils;

public class KickCommand implements CommandExecutor {
    
    private final FrizzlenMod plugin;
    
    public KickCommand(FrizzlenMod plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2) {
            MessageUtils.sendErrorMessage(sender, "Usage: /kick <player> <reason>");
            return true;
        }
        
        String targetName = args[0];
        Player target = Bukkit.getPlayer(targetName);
        
        if (target == null) {
            MessageUtils.sendErrorMessage(sender, "Player " + targetName + " is not online.");
            return true;
        }
        
        // Check if the sender is trying to kick someone with the same or higher permission level
        if (sender instanceof Player && target.hasPermission("frizzlenmod.kick.exempt")) {
            Player playerSender = (Player) sender;
            if (!playerSender.hasPermission("frizzlenmod.admin")) {
                MessageUtils.sendErrorMessage(sender, "You cannot kick this player.");
                return true;
            }
        }
        
        // Build the reason from the remaining arguments
        StringBuilder reasonBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            reasonBuilder.append(args[i]).append(" ");
        }
        String reason = reasonBuilder.toString().trim();
        
        // Kick the player
        target.kickPlayer("§cYou have been kicked from the server.\n§rReason: §f" + reason);
        
        // Notify staff
        String kickMessage = "§e" + target.getName() + " §fhas been kicked by §e" + 
                (sender instanceof Player ? sender.getName() : "Console") + 
                " §ffor: §e" + reason;
        MessageUtils.sendStaffMessage(kickMessage);
        
        // Log the action
        plugin.getStorageManager().logModAction(
                sender.getName(),
                "Kick",
                target.getName(),
                reason
        );
        
        // Confirm to the sender
        MessageUtils.sendSuccessMessage(sender, "Kicked " + target.getName() + " for: " + reason);
        
        return true;
    }
} 