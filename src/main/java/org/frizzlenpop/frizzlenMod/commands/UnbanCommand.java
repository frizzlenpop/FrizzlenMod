package org.frizzlenpop.frizzlenMod.commands;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenMod.FrizzlenMod;
import org.frizzlenpop.frizzlenMod.utils.MessageUtils;

public class UnbanCommand implements CommandExecutor {
    
    private final FrizzlenMod plugin;
    
    public UnbanCommand(FrizzlenMod plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            MessageUtils.sendErrorMessage(sender, "Usage: /unban <player>");
            return true;
        }
        
        String targetName = args[0];
        
        // Get the ban list
        BanList banList = Bukkit.getBanList(BanList.Type.NAME);
        
        // Check if the player is actually banned
        if (!banList.isBanned(targetName)) {
            MessageUtils.sendErrorMessage(sender, "Player " + targetName + " is not banned.");
            return true;
        }
        
        // Unban the player
        banList.pardon(targetName);
        
        // Notify staff
        String unbanMessage = "§e" + targetName + " §fhas been unbanned by §e" + 
                (sender instanceof Player ? sender.getName() : "Console");
        MessageUtils.sendStaffMessage(unbanMessage);
        
        // Log the action
        plugin.getStorageManager().logModAction(
                sender.getName(),
                "Unban",
                targetName,
                "Player unbanned"
        );
        
        // Confirm to the sender
        MessageUtils.sendSuccessMessage(sender, "Unbanned " + targetName);
        
        return true;
    }
} 