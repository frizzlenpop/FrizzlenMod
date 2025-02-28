package org.frizzlenpop.frizzlenMod.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.frizzlenpop.frizzlenMod.FrizzlenMod;
import org.frizzlenpop.frizzlenMod.utils.MessageUtils;

public class InvseeCommand implements CommandExecutor {
    
    private final FrizzlenMod plugin;
    
    public InvseeCommand(FrizzlenMod plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtils.sendErrorMessage(sender, "This command can only be used by players.");
            return true;
        }
        
        if (args.length < 1) {
            MessageUtils.sendErrorMessage(sender, "Usage: /invsee <player>");
            return true;
        }
        
        Player staff = (Player) sender;
        String targetName = args[0];
        Player target = Bukkit.getPlayer(targetName);
        
        if (target == null) {
            MessageUtils.sendErrorMessage(sender, "Player " + targetName + " is not online.");
            return true;
        }
        
        // Prevent players from viewing staff inventories unless they have admin permissions
        if (target.hasPermission("frizzlenmod.staff") && !staff.hasPermission("frizzlenmod.admin")) {
            MessageUtils.sendErrorMessage(sender, "You cannot view this player's inventory.");
            return true;
        }
        
        // Open the target's inventory to the staff member
        Inventory targetInventory = target.getInventory();
        staff.openInventory(targetInventory);
        
        // Log the action
        plugin.getStorageManager().logModAction(
                staff.getName(),
                "Invsee",
                target.getName(),
                "Staff viewed player's inventory"
        );
        
        return true;
    }
} 