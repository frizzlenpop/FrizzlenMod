package org.frizzlenpop.frizzlenMod.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenMod.FrizzlenMod;
import org.frizzlenpop.frizzlenMod.utils.MessageUtils;

public class FreezeCommand implements CommandExecutor {
    
    private final FrizzlenMod plugin;
    
    public FreezeCommand(FrizzlenMod plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            MessageUtils.sendErrorMessage(sender, "Usage: /freeze <player> [reason]");
            return true;
        }
        
        String targetName = args[0];
        Player target = Bukkit.getPlayer(targetName);
        
        if (target == null) {
            MessageUtils.sendErrorMessage(sender, "Player " + targetName + " is not online.");
            return true;
        }
        
        // Check if the sender is trying to freeze someone with the same or higher permission level
        if (sender instanceof Player && target.hasPermission("frizzlenmod.freeze.exempt")) {
            Player playerSender = (Player) sender;
            if (!playerSender.hasPermission("frizzlenmod.admin")) {
                MessageUtils.sendErrorMessage(sender, "You cannot freeze this player.");
                return true;
            }
        }
        
        // Check if already frozen
        if (plugin.getPunishmentManager().isPlayerFrozen(target.getUniqueId())) {
            MessageUtils.sendErrorMessage(sender, "Player " + targetName + " is already frozen.");
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
        
        // Freeze the player
        plugin.getPunishmentManager().freezePlayer(target.getUniqueId());
        
        // Notify the player
        target.sendMessage("§cYou have been frozen by " + 
                (sender instanceof Player ? sender.getName() : "Console") + 
                " for: " + reason);
        target.sendMessage("§cPlease do not log out or you may be banned.");
        
        // Notify staff
        String freezeMessage = "§e" + target.getName() + " §fhas been frozen by §e" + 
                (sender instanceof Player ? sender.getName() : "Console") + 
                " §ffor: §e" + reason;
        MessageUtils.sendStaffMessage(freezeMessage);
        
        // Log the action
        plugin.getStorageManager().logModAction(
                sender.getName(),
                "Freeze",
                target.getName(),
                reason
        );
        
        // Confirm to the sender
        MessageUtils.sendSuccessMessage(sender, "Frozen " + target.getName() + " for: " + reason);
        
        return true;
    }
} 