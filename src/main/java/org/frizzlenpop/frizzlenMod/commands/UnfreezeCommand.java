package org.frizzlenpop.frizzlenMod.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenMod.FrizzlenMod;
import org.frizzlenpop.frizzlenMod.utils.MessageUtils;

public class UnfreezeCommand implements CommandExecutor {
    
    private final FrizzlenMod plugin;
    
    public UnfreezeCommand(FrizzlenMod plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            MessageUtils.sendErrorMessage(sender, "Usage: /unfreeze <player>");
            return true;
        }
        
        String targetName = args[0];
        Player target = Bukkit.getPlayer(targetName);
        
        if (target == null) {
            MessageUtils.sendErrorMessage(sender, "Player " + targetName + " is not online.");
            return true;
        }
        
        // Check if the player is frozen
        if (!plugin.getPunishmentManager().isPlayerFrozen(target.getUniqueId())) {
            MessageUtils.sendErrorMessage(sender, "Player " + targetName + " is not frozen.");
            return true;
        }
        
        // Unfreeze the player
        plugin.getPunishmentManager().unfreezePlayer(target.getUniqueId());
        
        // Notify the player
        target.sendMessage("§aYou have been unfrozen by " + 
                (sender instanceof Player ? sender.getName() : "Console"));
        
        // Notify staff
        String unfreezeMessage = "§e" + target.getName() + " §fhas been unfrozen by §e" + 
                (sender instanceof Player ? sender.getName() : "Console");
        MessageUtils.sendStaffMessage(unfreezeMessage);
        
        // Log the action
        plugin.getStorageManager().logModAction(
                sender.getName(),
                "Unfreeze",
                target.getName(),
                "Player unfrozen"
        );
        
        // Confirm to the sender
        MessageUtils.sendSuccessMessage(sender, "Unfrozen " + target.getName());
        
        return true;
    }
} 