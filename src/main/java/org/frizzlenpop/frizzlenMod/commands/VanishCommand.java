package org.frizzlenpop.frizzlenMod.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenMod.FrizzlenMod;
import org.frizzlenpop.frizzlenMod.utils.MessageUtils;

public class VanishCommand implements CommandExecutor {
    
    private final FrizzlenMod plugin;
    
    public VanishCommand(FrizzlenMod plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtils.sendErrorMessage(sender, "This command can only be used by players.");
            return true;
        }
        
        Player player = (Player) sender;
        
        // Toggle vanish state
        boolean wasVanished = plugin.getVanishManager().isVanished(player.getUniqueId());
        
        if (wasVanished) {
            // Unvanish the player
            plugin.getVanishManager().unvanishPlayer(player);
            MessageUtils.sendSuccessMessage(player, "You are now visible to all players.");
            
            // Log the action
            plugin.getStorageManager().logModAction(
                    player.getName(),
                    "Unvanish",
                    player.getName(),
                    "Player made themselves visible"
            );
        } else {
            // Vanish the player
            plugin.getVanishManager().vanishPlayer(player);
            MessageUtils.sendSuccessMessage(player, "You are now vanished. Other players cannot see you.");
            
            // Log the action
            plugin.getStorageManager().logModAction(
                    player.getName(),
                    "Vanish",
                    player.getName(),
                    "Player made themselves invisible"
            );
        }
        
        return true;
    }
} 