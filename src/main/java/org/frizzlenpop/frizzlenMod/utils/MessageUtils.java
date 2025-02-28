package org.frizzlenpop.frizzlenMod.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class MessageUtils {
    
    // Prefix for all plugin messages
    private static final String PREFIX = ChatColor.DARK_GRAY + "[" + 
                                        ChatColor.AQUA + "FrizzlenMod" + 
                                        ChatColor.DARK_GRAY + "] " + 
                                        ChatColor.RESET;
    
    // Staff notification prefix
    private static final String STAFF_PREFIX = ChatColor.DARK_GRAY + "[" + 
                                              ChatColor.RED + "Staff" + 
                                              ChatColor.DARK_GRAY + "] " + 
                                              ChatColor.RESET;
    
    /**
     * Sends a formatted message to a CommandSender (player or console)
     * 
     * @param sender The recipient of the message
     * @param message The message to send
     */
    public static void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(PREFIX + ChatColor.translateAlternateColorCodes('&', message));
    }
    
    /**
     * Sends an error message to a CommandSender
     * 
     * @param sender The recipient of the message
     * @param message The message to send
     */
    public static void sendErrorMessage(CommandSender sender, String message) {
        sender.sendMessage(PREFIX + ChatColor.RED + ChatColor.translateAlternateColorCodes('&', message));
    }
    
    /**
     * Sends a success message to a CommandSender
     * 
     * @param sender The recipient of the message
     * @param message The message to send
     */
    public static void sendSuccessMessage(CommandSender sender, String message) {
        sender.sendMessage(PREFIX + ChatColor.GREEN + ChatColor.translateAlternateColorCodes('&', message));
    }
    
    /**
     * Sends a message to all online players with a specific permission
     * 
     * @param permission The permission required to receive the message
     * @param message The message to send
     */
    public static void broadcastToPermission(String permission, String message) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission(permission)) {
                player.sendMessage(PREFIX + ChatColor.translateAlternateColorCodes('&', message));
            }
        }
        // Also send to console
        Bukkit.getConsoleSender().sendMessage(PREFIX + ChatColor.translateAlternateColorCodes('&', message));
    }
    
    /**
     * Sends a staff notification to all online staff members
     * 
     * @param message The message to send
     */
    public static void sendStaffMessage(String message) {
        broadcastToPermission("frizzlenmod.staff", STAFF_PREFIX + 
                ChatColor.translateAlternateColorCodes('&', message));
    }
    
    /**
     * Formats a list of strings into a colored, numbered list
     * 
     * @param title The title for the list
     * @param items The items to include in the list
     * @return A formatted list as a string array
     */
    public static String[] formatList(String title, List<String> items) {
        List<String> result = new ArrayList<>();
        
        result.add(ChatColor.DARK_AQUA + "==== " + ChatColor.AQUA + title + 
                  ChatColor.DARK_AQUA + " ====");
        
        if (items.isEmpty()) {
            result.add(ChatColor.GRAY + "No items to display.");
        } else {
            int i = 1;
            for (String item : items) {
                result.add(ChatColor.AQUA + "" + i + ". " + ChatColor.WHITE + 
                          ChatColor.translateAlternateColorCodes('&', item));
                i++;
            }
        }
        
        result.add(ChatColor.DARK_AQUA + "==================");
        
        return result.toArray(new String[0]);
    }
    
    /**
     * Formats a help message showing command usage
     * 
     * @param command The command name
     * @param syntax The command syntax
     * @param description The command description
     * @return A formatted help message
     */
    public static String formatHelp(String command, String syntax, String description) {
        return ChatColor.AQUA + "/" + command + " " + 
              ChatColor.GRAY + syntax + " - " + 
              ChatColor.WHITE + description;
    }
    
    /**
     * Creates a horizontal line for visual separation in chat
     * 
     * @param color The color of the line
     * @return The line as a string
     */
    public static String createLine(ChatColor color) {
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < 52; i++) {
            line.append("-");
        }
        return color + line.toString();
    }
} 