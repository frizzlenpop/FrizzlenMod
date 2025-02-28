package org.frizzlenpop.frizzlenMod.managers;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenMod.FrizzlenMod;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatManager {
    private final FrizzlenMod plugin;
    private boolean chatMuted;
    private int slowModeSeconds;
    private final Map<UUID, Long> lastMessageTime;
    private final Set<UUID> shadowMutedPlayers;
    private List<String> blacklistedWords;
    private List<Pattern> blacklistedPatterns;
    private final Map<UUID, List<String>> chatLogs;
    private final int MAX_CHAT_LOG_SIZE = 100;

    public ChatManager(FrizzlenMod plugin) {
        this.plugin = plugin;
        this.chatMuted = false;
        this.slowModeSeconds = 0;
        this.lastMessageTime = new HashMap<>();
        this.shadowMutedPlayers = new HashSet<>();
        this.blacklistedWords = new ArrayList<>();
        this.blacklistedPatterns = new ArrayList<>();
        this.chatLogs = new HashMap<>();
        
        // Load config
        loadConfig();
    }
    
    private void loadConfig() {
        FileConfiguration config = plugin.getConfig();
        
        // Load chat filter words
        blacklistedWords = config.getStringList("chat.blacklisted-words");
        
        // Load regex patterns
        List<String> regexPatterns = config.getStringList("chat.blacklisted-patterns");
        blacklistedPatterns = new ArrayList<>();
        
        for (String pattern : regexPatterns) {
            try {
                blacklistedPatterns.add(Pattern.compile(pattern, Pattern.CASE_INSENSITIVE));
            } catch (Exception e) {
                plugin.getLogger().warning("Invalid regex pattern: " + pattern);
            }
        }
    }
    
    /**
     * Processes a chat message, applies filtering, and checks slow mode
     * 
     * @param player The player sending the message
     * @param message The message they're trying to send
     * @return true if the message should be allowed, false if it should be blocked
     */
    public boolean processMessage(Player player, String message) {
        UUID playerUUID = player.getUniqueId();
        
        // Log the message
        logChat(playerUUID, player.getName(), message);
        
        // Check if chat is muted for non-staff
        if (chatMuted && !player.hasPermission("frizzlenmod.bypass.chatmute")) {
            player.sendMessage("§cChat is currently muted.");
            return false;
        }
        
        // Check if this player is shadow muted
        if (isShadowMuted(playerUUID)) {
            return false;
        }
        
        // Check if the player is muted
        if (plugin.getPunishmentManager().isPlayerMuted(playerUUID)) {
            String timeRemaining = plugin.getPunishmentManager().getMuteTimeRemaining(playerUUID);
            player.sendMessage("§cYou are muted. " + (timeRemaining.equals("Permanent") ? "" : "Time remaining: " + timeRemaining));
            return false;
        }
        
        // Check slow mode
        if (slowModeSeconds > 0 && !player.hasPermission("frizzlenmod.bypass.slowmode")) {
            long currentTime = System.currentTimeMillis();
            long lastTime = lastMessageTime.getOrDefault(playerUUID, 0L);
            
            if (currentTime - lastTime < slowModeSeconds * 1000) {
                long remainingSeconds = (slowModeSeconds * 1000 - (currentTime - lastTime)) / 1000;
                player.sendMessage("§cSlow mode is enabled. Please wait " + remainingSeconds + " seconds before sending another message.");
                return false;
            }
            
            // Update last message time
            lastMessageTime.put(playerUUID, currentTime);
        }
        
        // Check for filtered words
        if (!player.hasPermission("frizzlenmod.bypass.filter")) {
            String filteredMessage = filterMessage(message);
            if (!filteredMessage.equals(message)) {
                player.sendMessage("§cYour message contains blacklisted words.");
                return false;
            }
        }
        
        // Check for excessive caps
        if (containsExcessiveCaps(message) && !player.hasPermission("frizzlenmod.bypass.capsfilter")) {
            player.sendMessage("§cPlease don't use excessive capitalization.");
            return false;
        }
        
        return true;
    }
    
    /**
     * Checks if a message contains excessive capitalization
     * 
     * @param message The message to check
     * @return true if the message has too many capital letters
     */
    public boolean containsExcessiveCaps(String message) {
        if (message.length() < 5) return false;
        
        int caps = 0;
        for (char c : message.toCharArray()) {
            if (Character.isUpperCase(c)) {
                caps++;
            }
        }
        
        // If more than 50% of the message is caps and the message is at least 5 characters
        return (double) caps / message.length() > 0.5;
    }
    
    /**
     * Logs a chat message for a player
     * 
     * @param playerUUID The UUID of the player
     * @param playerName The name of the player
     * @param message The message that was sent
     */
    public void logChat(UUID playerUUID, String playerName, String message) {
        String logEntry = System.currentTimeMillis() + ":" + playerName + ":" + message;
        
        if (!chatLogs.containsKey(playerUUID)) {
            chatLogs.put(playerUUID, new ArrayList<>());
        }
        
        List<String> logs = chatLogs.get(playerUUID);
        logs.add(logEntry);
        
        // Trim log if it gets too big
        if (logs.size() > MAX_CHAT_LOG_SIZE) {
            logs = logs.subList(logs.size() - MAX_CHAT_LOG_SIZE, logs.size());
            chatLogs.put(playerUUID, logs);
        }
    }
    
    /**
     * Gets the chat logs for a player
     * 
     * @param playerUUID The UUID of the player
     * @return A list of chat log entries
     */
    public List<String> getChatLogs(UUID playerUUID) {
        return chatLogs.getOrDefault(playerUUID, new ArrayList<>());
    }
    
    /**
     * Sets the chat muted state
     * 
     * @param muted Whether chat should be muted
     */
    public void setChatMuted(boolean muted) {
        this.chatMuted = muted;
    }
    
    /**
     * Checks if chat is currently muted
     * 
     * @return true if chat is muted
     */
    public boolean isChatMuted() {
        return chatMuted;
    }
    
    /**
     * Sets the slow mode cooldown in seconds
     * 
     * @param seconds The number of seconds between messages, 0 to disable
     */
    public void setSlowMode(int seconds) {
        this.slowModeSeconds = seconds;
        if (seconds == 0) {
            // Clear the last message times when disabling
            lastMessageTime.clear();
        }
    }
    
    /**
     * Gets the current slow mode setting
     * 
     * @return The number of seconds for slow mode, 0 if disabled
     */
    public int getSlowModeSeconds() {
        return slowModeSeconds;
    }
    
    /**
     * Shadow mutes a player (they can type but messages aren't sent)
     * 
     * @param playerUUID The UUID of the player to shadow mute
     */
    public void shadowMute(UUID playerUUID) {
        shadowMutedPlayers.add(playerUUID);
    }
    
    /**
     * Removes a shadow mute from a player
     * 
     * @param playerUUID The UUID of the player to unshadow mute
     */
    public void removeShadowMute(UUID playerUUID) {
        shadowMutedPlayers.remove(playerUUID);
    }
    
    /**
     * Checks if a player is shadow muted
     * 
     * @param playerUUID The UUID of the player to check
     * @return true if the player is shadow muted
     */
    public boolean isShadowMuted(UUID playerUUID) {
        return shadowMutedPlayers.contains(playerUUID);
    }
    
    /**
     * Clears the chat for all players or a specific player
     * 
     * @param player The player to clear chat for, or null for all players
     * @param linesCount The number of blank lines to send
     */
    public void clearChat(Player player, int linesCount) {
        StringBuilder blankLines = new StringBuilder();
        for (int i = 0; i < linesCount; i++) {
            blankLines.append("\n");
        }
        
        String message = blankLines.toString();
        
        if (player != null) {
            player.sendMessage(message);
        } else {
            Bukkit.broadcastMessage(message);
        }
    }

    // Check for filtered words
    public boolean containsFilteredWord(String message) {
        String lowerCaseMessage = message.toLowerCase();
        
        // Check direct matches
        for (String word : blacklistedWords) {
            if (lowerCaseMessage.contains(word.toLowerCase())) {
                return true;
            }
        }
        
        // Check regex patterns
        for (Pattern pattern : blacklistedPatterns) {
            if (pattern.matcher(message).find()) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Filters a message, replacing blacklisted words with asterisks
     * 
     * @param message The message to filter
     * @return The filtered message
     */
    public String filterMessage(String message) {
        if (blacklistedWords.isEmpty() && blacklistedPatterns.isEmpty()) {
            return message;
        }
        
        String filteredMessage = message;
        
        // Replace direct words
        for (String word : blacklistedWords) {
            // Create a pattern that matches the word with word boundaries
            Pattern pattern = Pattern.compile("\\b" + Pattern.quote(word) + "\\b", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(filteredMessage);
            
            // Replace with asterisks of the same length
            StringBuilder replacement = new StringBuilder();
            for (int i = 0; i < word.length(); i++) {
                replacement.append("*");
            }
            
            filteredMessage = matcher.replaceAll(replacement.toString());
        }
        
        // Replace regex matches
        for (Pattern pattern : blacklistedPatterns) {
            Matcher matcher = pattern.matcher(filteredMessage);
            StringBuilder sb = new StringBuilder();
            
            while (matcher.find()) {
                // Replace with asterisks of the same length
                StringBuilder replacement = new StringBuilder();
                for (int i = 0; i < matcher.group().length(); i++) {
                    replacement.append("*");
                }
                matcher.appendReplacement(sb, replacement.toString());
            }
            
            matcher.appendTail(sb);
            filteredMessage = sb.toString();
        }
        
        return filteredMessage;
    }
    
    /**
     * Checks if the chat manager has filtering enabled
     * 
     * @return true if filtering is enabled
     */
    public boolean hasFilters() {
        return !blacklistedWords.isEmpty() || !blacklistedPatterns.isEmpty();
    }
} 