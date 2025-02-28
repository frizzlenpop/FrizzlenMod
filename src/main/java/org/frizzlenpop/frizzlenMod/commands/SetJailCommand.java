package org.frizzlenpop.frizzlenMod.commands;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenMod.FrizzlenMod;
import org.frizzlenpop.frizzlenMod.utils.MessageUtils;

public class SetJailCommand implements CommandExecutor {
    
    private final FrizzlenMod plugin;
    
    public SetJailCommand(FrizzlenMod plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtils.sendErrorMessage(sender, "This command can only be used by players.");
            return true;
        }
        
        if (args.length < 1) {
            MessageUtils.sendErrorMessage(sender, "Usage: /setjail <name>");
            return true;
        }
        
        Player player = (Player) sender;
        String jailName = args[0].toLowerCase();
        Location location = player.getLocation();
        
        // Check if jail already exists
        if (plugin.getJailManager().jailExists(jailName)) {
            MessageUtils.sendErrorMessage(player, "A jail with the name '" + jailName + "' already exists. Use a different name.");
            return true;
        }
        
        // Create the jail
        boolean success = plugin.getJailManager().createJail(jailName, location);
        
        if (success) {
            // Log the action
            plugin.getStorageManager().logModAction(
                    player.getName(),
                    "SetJail",
                    jailName,
                    "Created jail at X:" + location.getBlockX() + 
                    ", Y:" + location.getBlockY() + 
                    ", Z:" + location.getBlockZ() + 
                    " in world " + location.getWorld().getName()
            );
            
            // Confirm to the sender
            MessageUtils.sendSuccessMessage(player, "Jail '" + jailName + "' has been created at your current location.");
        } else {
            MessageUtils.sendErrorMessage(player, "Failed to create jail '" + jailName + "'. Please try again.");
        }
        
        return true;
    }
} 