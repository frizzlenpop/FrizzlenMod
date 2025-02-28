package org.frizzlenpop.frizzlenMod.api.services;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenMod.FrizzlenMod;
import org.frizzlenpop.frizzlenMod.api.models.ApiResponse;
import org.frizzlenpop.frizzlenMod.api.models.PaginatedResponse;
import org.frizzlenpop.frizzlenMod.api.models.Punishment;
import org.frizzlenpop.frizzlenMod.managers.PunishmentManager;
import org.frizzlenpop.frizzlenMod.storage.StorageManager;
import org.frizzlenpop.frizzlenMod.utils.TimeUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing player punishments
 */
public class PunishmentsService {
    private final FrizzlenMod plugin;
    private final PunishmentManager punishmentManager;
    private final StorageManager storageManager;
    
    /**
     * Creates a new PunishmentsService
     * @param plugin The FrizzlenMod plugin instance
     */
    public PunishmentsService(FrizzlenMod plugin) {
        this.plugin = plugin;
        this.punishmentManager = plugin.getPunishmentManager();
        this.storageManager = plugin.getStorageManager();
    }
    
    /**
     * Gets all punishments with pagination
     * @param page The page number (1-based)
     * @param pageSize The page size
     * @return A paginated response with punishments
     */
    public PaginatedResponse<Punishment> getAllPunishments(int page, int pageSize) {
        List<Punishment> allPunishments = getAllPunishments();
        allPunishments.sort(Comparator.comparing(Punishment::getTimestamp).reversed());
        
        return paginateResults(allPunishments, page, pageSize);
    }
    
    /**
     * Gets all punishments for a specific player with pagination
     * @param playerName The name of the player
     * @param page The page number (1-based)
     * @param pageSize The page size
     * @return A paginated response with punishments
     */
    public PaginatedResponse<Punishment> getPunishmentsForPlayer(String playerName, int page, int pageSize) {
        List<Punishment> allPunishments = getAllPunishments();
        List<Punishment> playerPunishments = allPunishments.stream()
                .filter(punishment -> punishment.getPlayerName().equalsIgnoreCase(playerName))
                .sorted(Comparator.comparing(Punishment::getTimestamp).reversed())
                .collect(Collectors.toList());
        
        return paginateResults(playerPunishments, page, pageSize);
    }
    
    /**
     * Gets all punishments of a specific type with pagination
     * @param type The type of punishment
     * @param page The page number (1-based)
     * @param pageSize The page size
     * @return A paginated response with punishments
     */
    public PaginatedResponse<Punishment> getPunishmentsByType(String type, int page, int pageSize) {
        List<Punishment> allPunishments = getAllPunishments();
        List<Punishment> typePunishments = allPunishments.stream()
                .filter(punishment -> punishment.getType().equalsIgnoreCase(type))
                .sorted(Comparator.comparing(Punishment::getTimestamp).reversed())
                .collect(Collectors.toList());
        
        return paginateResults(typePunishments, page, pageSize);
    }
    
    /**
     * Bans a player
     * @param playerName The name of the player to ban
     * @param reason The reason for the ban
     * @param moderator The name of the moderator performing the ban
     * @return An API response with the result
     */
    public ApiResponse<Punishment> banPlayer(String playerName, String reason, String moderator) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
        if (offlinePlayer == null || !offlinePlayer.hasPlayedBefore()) {
            return ApiResponse.error("Player not found");
        }
        
        UUID playerUUID = offlinePlayer.getUniqueId();
        if (punishmentManager.isBanned(playerUUID)) {
            return ApiResponse.error("Player is already banned");
        }
        
        punishmentManager.banPlayer(playerUUID, reason);
        
        // Log the action
        storageManager.logModAction(moderator, "BAN", playerName, reason);
        
        // Create a punishment object for the response
        Punishment punishment = new Punishment();
        punishment.setPlayerName(playerName);
        punishment.setPlayerUUID(playerUUID.toString());
        punishment.setType("BAN");
        punishment.setReason(reason);
        punishment.setModerator(moderator);
        punishment.setTimestamp(System.currentTimeMillis());
        
        return ApiResponse.success("Player banned successfully", punishment);
    }
    
    /**
     * Temporarily bans a player
     * @param playerName The name of the player to ban
     * @param reason The reason for the ban
     * @param duration The duration of the ban
     * @param moderator The name of the moderator performing the ban
     * @return An API response with the result
     */
    public ApiResponse<Punishment> tempBanPlayer(String playerName, String reason, String duration, String moderator) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
        if (offlinePlayer == null || !offlinePlayer.hasPlayedBefore()) {
            return ApiResponse.error("Player not found");
        }
        
        UUID playerUUID = offlinePlayer.getUniqueId();
        if (punishmentManager.isBanned(playerUUID)) {
            return ApiResponse.error("Player is already banned");
        }
        
        long durationMillis = TimeUtils.parseDuration(duration);
        if (durationMillis <= 0) {
            return ApiResponse.error("Invalid duration format");
        }
        
        punishmentManager.tempBanPlayer(playerUUID, reason, durationMillis);
        
        // Log the action
        storageManager.logModAction(moderator, "TEMP_BAN", playerName, duration, reason);
        
        // Create a punishment object for the response
        Punishment punishment = new Punishment();
        punishment.setPlayerName(playerName);
        punishment.setPlayerUUID(playerUUID.toString());
        punishment.setType("TEMP_BAN");
        punishment.setReason(reason);
        punishment.setModerator(moderator);
        punishment.setDuration(duration);
        punishment.setTimestamp(System.currentTimeMillis());
        punishment.setExpiration(System.currentTimeMillis() + durationMillis);
        
        return ApiResponse.success("Player temporarily banned successfully", punishment);
    }
    
    /**
     * Unbans a player
     * @param playerName The name of the player to unban
     * @param moderator The name of the moderator performing the unban
     * @return An API response with the result
     */
    public ApiResponse<Punishment> unbanPlayer(String playerName, String moderator) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
        if (offlinePlayer == null || !offlinePlayer.hasPlayedBefore()) {
            return ApiResponse.error("Player not found");
        }
        
        UUID playerUUID = offlinePlayer.getUniqueId();
        if (!punishmentManager.isBanned(playerUUID)) {
            return ApiResponse.error("Player is not banned");
        }
        
        punishmentManager.unbanPlayer(playerUUID);
        
        // Log the action
        storageManager.logModAction(moderator, "UNBAN", playerName, "Unbanned by " + moderator);
        
        // Create a punishment object for the response
        Punishment punishment = new Punishment();
        punishment.setPlayerName(playerName);
        punishment.setPlayerUUID(playerUUID.toString());
        punishment.setType("UNBAN");
        punishment.setReason("Unbanned by " + moderator);
        punishment.setModerator(moderator);
        punishment.setTimestamp(System.currentTimeMillis());
        
        return ApiResponse.success("Player unbanned successfully", punishment);
    }
    
    /**
     * Mutes a player
     * @param playerName The name of the player to mute
     * @param reason The reason for the mute
     * @param moderator The name of the moderator performing the mute
     * @return An API response with the result
     */
    public ApiResponse<Punishment> mutePlayer(String playerName, String reason, String moderator) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
        if (offlinePlayer == null || !offlinePlayer.hasPlayedBefore()) {
            return ApiResponse.error("Player not found");
        }
        
        UUID playerUUID = offlinePlayer.getUniqueId();
        if (punishmentManager.isMuted(playerUUID)) {
            return ApiResponse.error("Player is already muted");
        }
        
        punishmentManager.mutePlayer(playerUUID);
        
        // Log the action
        storageManager.logModAction(moderator, "MUTE", playerName, reason);
        
        // Create a punishment object for the response
        Punishment punishment = new Punishment();
        punishment.setPlayerName(playerName);
        punishment.setPlayerUUID(playerUUID.toString());
        punishment.setType("MUTE");
        punishment.setReason(reason);
        punishment.setModerator(moderator);
        punishment.setTimestamp(System.currentTimeMillis());
        
        return ApiResponse.success("Player muted successfully", punishment);
    }
    
    /**
     * Temporarily mutes a player
     * @param playerName The name of the player to mute
     * @param reason The reason for the mute
     * @param duration The duration of the mute
     * @param moderator The name of the moderator performing the mute
     * @return An API response with the result
     */
    public ApiResponse<Punishment> tempMutePlayer(String playerName, String reason, String duration, String moderator) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
        if (offlinePlayer == null || !offlinePlayer.hasPlayedBefore()) {
            return ApiResponse.error("Player not found");
        }
        
        UUID playerUUID = offlinePlayer.getUniqueId();
        if (punishmentManager.isMuted(playerUUID)) {
            return ApiResponse.error("Player is already muted");
        }
        
        long durationMillis = TimeUtils.parseDuration(duration);
        if (durationMillis <= 0) {
            return ApiResponse.error("Invalid duration format");
        }
        
        punishmentManager.tempMutePlayer(playerUUID, durationMillis);
        
        // Log the action
        storageManager.logModAction(moderator, "TEMP_MUTE", playerName, duration, reason);
        
        // Create a punishment object for the response
        Punishment punishment = new Punishment();
        punishment.setPlayerName(playerName);
        punishment.setPlayerUUID(playerUUID.toString());
        punishment.setType("TEMP_MUTE");
        punishment.setReason(reason);
        punishment.setModerator(moderator);
        punishment.setDuration(duration);
        punishment.setTimestamp(System.currentTimeMillis());
        punishment.setExpiration(System.currentTimeMillis() + durationMillis);
        
        return ApiResponse.success("Player temporarily muted successfully", punishment);
    }
    
    /**
     * Unmutes a player
     * @param playerName The name of the player to unmute
     * @param moderator The name of the moderator performing the unmute
     * @return An API response with the result
     */
    public ApiResponse<Punishment> unmutePlayer(String playerName, String moderator) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
        if (offlinePlayer == null || !offlinePlayer.hasPlayedBefore()) {
            return ApiResponse.error("Player not found");
        }
        
        UUID playerUUID = offlinePlayer.getUniqueId();
        if (!punishmentManager.isMuted(playerUUID)) {
            return ApiResponse.error("Player is not muted");
        }
        
        punishmentManager.unmutePlayer(playerUUID);
        
        // Log the action
        storageManager.logModAction(moderator, "UNMUTE", playerName, "Unmuted by " + moderator);
        
        // Create a punishment object for the response
        Punishment punishment = new Punishment();
        punishment.setPlayerName(playerName);
        punishment.setPlayerUUID(playerUUID.toString());
        punishment.setType("UNMUTE");
        punishment.setReason("Unmuted by " + moderator);
        punishment.setModerator(moderator);
        punishment.setTimestamp(System.currentTimeMillis());
        
        return ApiResponse.success("Player unmuted successfully", punishment);
    }
    
    /**
     * Warns a player
     * @param playerName The name of the player to warn
     * @param reason The reason for the warning
     * @param moderator The name of the moderator issuing the warning
     * @return An API response with the result
     */
    public ApiResponse<Punishment> warnPlayer(String playerName, String reason, String moderator) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
        if (offlinePlayer == null || !offlinePlayer.hasPlayedBefore()) {
            return ApiResponse.error("Player not found");
        }
        
        UUID playerUUID = offlinePlayer.getUniqueId();
        int warningCount = punishmentManager.warnPlayer(playerUUID, reason);
        
        // Notify the player if online
        Player player = offlinePlayer.getPlayer();
        if (player != null && player.isOnline()) {
            player.sendMessage("§cYou have been warned: §f" + reason);
            player.sendMessage("§cThis is warning #" + warningCount);
        }
        
        // Log the action
        storageManager.logModAction(moderator, "WARNING", playerName, reason);
        
        // Create a punishment object for the response
        Punishment punishment = new Punishment();
        punishment.setPlayerName(playerName);
        punishment.setPlayerUUID(playerUUID.toString());
        punishment.setType("WARNING");
        punishment.setReason(reason);
        punishment.setModerator(moderator);
        punishment.setTimestamp(System.currentTimeMillis());
        punishment.setCount(warningCount);
        
        return ApiResponse.success("Player warned successfully", punishment);
    }
    
    /**
     * Kicks a player
     * @param playerName The name of the player to kick
     * @param reason The reason for the kick
     * @param moderator The name of the moderator performing the kick
     * @return An API response with the result
     */
    public ApiResponse<Punishment> kickPlayer(String playerName, String reason, String moderator) {
        Player player = Bukkit.getPlayer(playerName);
        if (player == null || !player.isOnline()) {
            return ApiResponse.error("Player is not online");
        }
        
        player.kickPlayer(reason);
        
        // Log the action
        storageManager.logModAction(moderator, "KICK", playerName, reason);
        
        // Create a punishment object for the response
        Punishment punishment = new Punishment();
        punishment.setPlayerName(playerName);
        punishment.setPlayerUUID(player.getUniqueId().toString());
        punishment.setType("KICK");
        punishment.setReason(reason);
        punishment.setModerator(moderator);
        punishment.setTimestamp(System.currentTimeMillis());
        
        return ApiResponse.success("Player kicked successfully", punishment);
    }
    
    /**
     * Paginates a list of results
     * @param results The list of results
     * @param page The page number (1-based)
     * @param pageSize The page size
     * @return A paginated response
     */
    private <T> PaginatedResponse<T> paginateResults(List<T> results, int page, int pageSize) {
        int totalItems = results.size();
        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalItems);
        
        if (startIndex >= totalItems) {
            return new PaginatedResponse<>(Collections.emptyList(), page, pageSize, totalItems);
        }
        
        List<T> pageItems = results.subList(startIndex, endIndex);
        return new PaginatedResponse<>(pageItems, page, pageSize, totalItems);
    }
    
    /**
     * Gets all punishments
     * @return A list of all punishments
     */
    private List<Punishment> getAllPunishments() {
        // This is a placeholder implementation
        // In a real implementation, you would retrieve punishments from a database or file
        return new ArrayList<>();
    }
} 