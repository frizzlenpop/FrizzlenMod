package org.frizzlenpop.frizzlenMod.api.controllers;

import com.google.gson.Gson;
import org.bukkit.BanEntry;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenMod.FrizzlenMod;
import org.frizzlenpop.frizzlenMod.api.models.Punishment;
import org.frizzlenpop.frizzlenMod.storage.ModAction;
import org.frizzlenpop.frizzlenMod.utils.TimeUtils;
import spark.Request;
import spark.Response;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Handles punishment management API endpoints
 */
public class PunishmentsController {
    private final FrizzlenMod plugin;
    private final Gson gson;
    
    public PunishmentsController(FrizzlenMod plugin, Gson gson) {
        this.plugin = plugin;
        this.gson = gson;
    }
    
    /**
     * Get all punishments
     */
    public Object getAllPunishments(Request request, Response response) {
        try {
            List<Punishment> punishments = new ArrayList<>();
            
            // Get all bans
            BanList banList = Bukkit.getBanList(BanList.Type.NAME);
            Set<BanEntry> banEntries = banList.getBanEntries();
            for (BanEntry entry : banEntries) {
                Punishment punishment = new Punishment();
                punishment.setPlayerName(entry.getTarget());
                punishment.setType("BAN");
                punishment.setReason(entry.getReason());
                punishment.setModerator(entry.getSource());
                punishment.setTimestamp(entry.getCreated().getTime());
                
                // Check if the ban is temporary
                Date expiry = entry.getExpiration();
                if (expiry != null) {
                    punishment.setExpiration(expiry.getTime());
                    long duration = expiry.getTime() - entry.getCreated().getTime();
                    punishment.setDuration(TimeUtils.formatTime(duration));
                }
                
                punishments.add(punishment);
            }
            
            // Get all mutes
            Map<UUID, String> mutedPlayers = plugin.getPunishmentManager().getAllMutedPlayers();
            for (Map.Entry<UUID, String> entry : mutedPlayers.entrySet()) {
                OfflinePlayer player = Bukkit.getOfflinePlayer(entry.getKey());
                
                Punishment punishment = new Punishment();
                punishment.setPlayerName(player.getName());
                punishment.setPlayerUUID(player.getUniqueId().toString());
                punishment.setType("MUTE");
                punishment.setDuration(entry.getValue());
                
                punishments.add(punishment);
            }
            
            // Sort by timestamp descending
            punishments.sort((p1, p2) -> Long.compare(p2.getTimestamp(), p1.getTimestamp()));
            
            return gson.toJson(Map.of(
                    "success", true,
                    "punishments", punishments
            ));
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error getting punishments: " + e.getMessage(), e);
            response.status(500);
            return gson.toJson(Map.of("error", "Internal server error"));
        }
    }
    
    /**
     * Get punishments for a specific player
     */
    public Object getPlayerPunishments(Request request, Response response) {
        try {
            String playerName = request.params(":player");
            
            if (playerName == null || playerName.isEmpty()) {
                response.status(400);
                return gson.toJson(Map.of("error", "Player name is required"));
            }
            
            List<Punishment> punishments = new ArrayList<>();
            
            // Get the player
            OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
            
            // Check if player is banned
            BanList banList = Bukkit.getBanList(BanList.Type.NAME);
            BanEntry banEntry = banList.getBanEntry(playerName);
            if (banEntry != null) {
                Punishment punishment = new Punishment();
                punishment.setPlayerName(playerName);
                punishment.setPlayerUUID(player.getUniqueId().toString());
                punishment.setType("BAN");
                punishment.setReason(banEntry.getReason());
                punishment.setModerator(banEntry.getSource());
                punishment.setTimestamp(banEntry.getCreated().getTime());
                
                // Check if the ban is temporary
                Date expiry = banEntry.getExpiration();
                if (expiry != null) {
                    punishment.setExpiration(expiry.getTime());
                    long duration = expiry.getTime() - banEntry.getCreated().getTime();
                    punishment.setDuration(TimeUtils.formatTime(duration));
                }
                
                punishments.add(punishment);
            }
            
            // Check if player is muted
            if (plugin.getPunishmentManager().isPlayerMuted(player.getUniqueId())) {
                String muteTimeRemaining = plugin.getPunishmentManager().getMuteTimeRemaining(player.getUniqueId());
                
                Punishment punishment = new Punishment();
                punishment.setPlayerName(playerName);
                punishment.setPlayerUUID(player.getUniqueId().toString());
                punishment.setType("MUTE");
                punishment.setDuration(muteTimeRemaining);
                
                punishments.add(punishment);
            }
            
            // Get all warnings
            int warningCount = plugin.getPunishmentManager().getPlayerWarnings(player.getUniqueId());
            if (warningCount > 0) {
                Punishment punishment = new Punishment();
                punishment.setPlayerName(playerName);
                punishment.setPlayerUUID(player.getUniqueId().toString());
                punishment.setType("WARNING");
                punishment.setCount(warningCount);
                
                punishments.add(punishment);
            }
            
            // Get punishment history from logs
            List<ModAction> logEntries = plugin.getStorageManager().getModActionLogs(player.getName());
            for (ModAction entry : logEntries) {
                String action = entry.getAction();
                
                // Skip if it's a current punishment (already added above)
                if ((action.equals("BAN") && banEntry != null) ||
                    (action.equals("MUTE") && plugin.getPunishmentManager().isPlayerMuted(player.getUniqueId()))) {
                    continue;
                }
                
                Punishment punishment = new Punishment();
                punishment.setPlayerName(playerName);
                punishment.setType(action);
                punishment.setReason(entry.getReason());
                punishment.setModerator(entry.getModerator());
                punishment.setTimestamp(entry.getTimestamp().getTime());
                punishment.setDuration(entry.getDuration());
                
                punishments.add(punishment);
            }
            
            // Sort by timestamp descending
            punishments.sort((p1, p2) -> Long.compare(p2.getTimestamp(), p1.getTimestamp()));
            
            return gson.toJson(Map.of(
                    "success", true,
                    "player", playerName,
                    "punishments", punishments
            ));
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error getting player punishments: " + e.getMessage(), e);
            response.status(500);
            return gson.toJson(Map.of("error", "Internal server error"));
        }
    }
    
    /**
     * Add a ban (admin)
     */
    public Object addBan(Request request, Response response) {
        try {
            Map<String, Object> requestData = gson.fromJson(request.body(), Map.class);
            
            String playerName = (String) requestData.get("playerName");
            String reason = (String) requestData.get("reason");
            String duration = (String) requestData.get("duration"); // "permanent" or duration in format "1d", "2h", etc.
            String moderator = request.attribute("username");
            
            if (playerName == null || reason == null) {
                response.status(400);
                return gson.toJson(Map.of("error", "Player name and reason are required"));
            }
            
            // Check if the player exists
            OfflinePlayer player = Arrays.stream(Bukkit.getOfflinePlayers())
                    .filter(p -> p.getName() != null && p.getName().equalsIgnoreCase(playerName))
                    .findFirst()
                    .orElse(null);
            
            if (player == null) {
                response.status(404);
                return gson.toJson(Map.of("error", "Player not found"));
            }
            
            // Check if player is already banned
            if (Bukkit.getBanList(BanList.Type.NAME).isBanned(player.getName())) {
                response.status(400);
                return gson.toJson(Map.of("error", "Player is already banned"));
            }
            
            if (duration == null || "permanent".equalsIgnoreCase(duration)) {
                // Permanent ban
                Bukkit.getBanList(BanList.Type.NAME).addBan(player.getName(), reason, null, moderator);
                
                // Kick if online
                if (player.isOnline()) {
                    player.getPlayer().kickPlayer("You have been banned: " + reason);
                }
                
                // Log action
                plugin.getStorageManager().logModAction(moderator, "Ban", player.getName(), reason);
                
            } else {
                // Temporary ban - parse duration
                try {
                    // This is a simplified duration parsing
                    int value = Integer.parseInt(duration.substring(0, duration.length() - 1));
                    char unit = duration.charAt(duration.length() - 1);
                    
                    long durationMillis = 0;
                    switch (unit) {
                        case 'm':
                            durationMillis = value * 60 * 1000L; // minutes
                            break;
                        case 'h':
                            durationMillis = value * 60 * 60 * 1000L; // hours
                            break;
                        case 'd':
                            durationMillis = value * 24 * 60 * 60 * 1000L; // days
                            break;
                        default:
                            response.status(400);
                            return gson.toJson(Map.of("error", "Invalid duration format. Use format like '1d', '2h', etc."));
                    }
                    
                    Date expiryDate = new Date(System.currentTimeMillis() + durationMillis);
                    
                    // Add ban
                    Bukkit.getBanList(BanList.Type.NAME).addBan(player.getName(), reason, expiryDate, moderator);
                    
                    // Kick if online
                    if (player.isOnline()) {
                        player.getPlayer().kickPlayer("You have been temporarily banned: " + reason);
                    }
                    
                    // Log action
                    plugin.getStorageManager().logModAction(moderator, "TempBan", player.getName(), duration, reason);
                    
                } catch (NumberFormatException e) {
                    response.status(400);
                    return gson.toJson(Map.of("error", "Invalid duration format. Use format like '1d', '2h', etc."));
                }
            }
            
            return gson.toJson(Map.of(
                    "success", true,
                    "message", "Player banned successfully"
            ));
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error adding ban: " + e.getMessage(), e);
            response.status(500);
            return gson.toJson(Map.of("error", "Internal server error"));
        }
    }
    
    /**
     * Remove a ban (admin)
     */
    public Object removeBan(Request request, Response response) {
        try {
            Map<String, Object> requestData = gson.fromJson(request.body(), Map.class);
            
            String playerName = (String) requestData.get("playerName");
            String moderator = request.attribute("username");
            
            if (playerName == null) {
                response.status(400);
                return gson.toJson(Map.of("error", "Player name is required"));
            }
            
            // Check if the player is banned
            BanList banList = Bukkit.getBanList(BanList.Type.NAME);
            if (!banList.isBanned(playerName)) {
                response.status(400);
                return gson.toJson(Map.of("error", "Player is not banned"));
            }
            
            // Unban the player
            banList.pardon(playerName);
            
            // Log action
            plugin.getStorageManager().logModAction(moderator, "Unban", playerName, "Unbanned via web panel");
            
            return gson.toJson(Map.of(
                    "success", true,
                    "message", "Player unbanned successfully"
            ));
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error removing ban: " + e.getMessage(), e);
            response.status(500);
            return gson.toJson(Map.of("error", "Internal server error"));
        }
    }
    
    /**
     * Add a temporary ban for a player
     */
    public Object addTempBan(Request request, Response response) {
        try {
            // Parse request body
            Map<String, Object> body = gson.fromJson(request.body(), Map.class);
            
            String playerName = (String) body.get("playerName");
            String reason = (String) body.get("reason");
            String duration = (String) body.get("duration");
            String moderator = request.attribute("username");
            
            if (playerName == null || playerName.isEmpty()) {
                response.status(400);
                return gson.toJson(Map.of("error", "Player name is required"));
            }
            
            if (reason == null || reason.isEmpty()) {
                reason = "No reason provided";
            }
            
            if (duration == null || duration.isEmpty()) {
                response.status(400);
                return gson.toJson(Map.of("error", "Duration is required for temporary bans"));
            }
            
            // Convert duration to milliseconds
            long durationMillis = plugin.getStorageManager().parseTimeString(duration);
            
            // Get player
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
            
            // Add ban
            Date expiry = new Date(System.currentTimeMillis() + durationMillis);
            Bukkit.getBanList(BanList.Type.NAME).addBan(playerName, reason, expiry, moderator);
            
            // Kick if online
            Player player = Bukkit.getPlayer(playerName);
            if (player != null && player.isOnline()) {
                player.kickPlayer("You have been temporarily banned: " + reason);
            }
            
            // Log the action
            plugin.getStorageManager().logModAction(
                    moderator,
                    "TEMP_BAN",
                    playerName,
                    reason + " (Duration: " + duration + ")"
            );
            
            return gson.toJson(Map.of(
                    "success", true,
                    "message", "Player " + playerName + " has been temporarily banned for " + duration
            ));
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error adding temp ban: " + e.getMessage(), e);
            response.status(500);
            return gson.toJson(Map.of("error", "Internal server error"));
        }
    }
    
    /**
     * Add a mute for a player
     */
    public Object addMute(Request request, Response response) {
        try {
            // Parse request body
            Map<String, Object> body = gson.fromJson(request.body(), Map.class);
            
            String playerName = (String) body.get("playerName");
            String reason = (String) body.get("reason");
            String moderator = request.attribute("username");
            
            if (playerName == null || playerName.isEmpty()) {
                response.status(400);
                return gson.toJson(Map.of("error", "Player name is required"));
            }
            
            if (reason == null || reason.isEmpty()) {
                reason = "No reason provided";
            }
            
            // Get player
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
            
            // Check if player exists
            if (!offlinePlayer.hasPlayedBefore()) {
                response.status(404);
                return gson.toJson(Map.of("error", "Player not found"));
            }
            
            // Mute player
            plugin.getPunishmentManager().mutePlayer(offlinePlayer.getUniqueId());
            
            // Notify if online
            Player player = Bukkit.getPlayer(playerName);
            if (player != null && player.isOnline()) {
                player.sendMessage("§cYou have been muted: " + reason);
            }
            
            // Log the action
            plugin.getStorageManager().logModAction(
                    moderator,
                    "MUTE",
                    playerName,
                    reason
            );
            
            return gson.toJson(Map.of(
                    "success", true,
                    "message", "Player " + playerName + " has been muted"
            ));
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error adding mute: " + e.getMessage(), e);
            response.status(500);
            return gson.toJson(Map.of("error", "Internal server error"));
        }
    }
    
    /**
     * Add a temporary mute for a player
     */
    public Object addTempMute(Request request, Response response) {
        try {
            // Parse request body
            Map<String, Object> body = gson.fromJson(request.body(), Map.class);
            
            String playerName = (String) body.get("playerName");
            String reason = (String) body.get("reason");
            String duration = (String) body.get("duration");
            String moderator = request.attribute("username");
            
            if (playerName == null || playerName.isEmpty()) {
                response.status(400);
                return gson.toJson(Map.of("error", "Player name is required"));
            }
            
            if (reason == null || reason.isEmpty()) {
                reason = "No reason provided";
            }
            
            if (duration == null || duration.isEmpty()) {
                response.status(400);
                return gson.toJson(Map.of("error", "Duration is required for temporary mutes"));
            }
            
            // Convert duration to milliseconds
            long durationMillis = plugin.getStorageManager().parseTimeString(duration);
            
            // Get player
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
            
            // Check if player exists
            if (!offlinePlayer.hasPlayedBefore()) {
                response.status(404);
                return gson.toJson(Map.of("error", "Player not found"));
            }
            
            // Mute player
            plugin.getPunishmentManager().tempMutePlayer(offlinePlayer.getUniqueId(), durationMillis);
            
            // Notify if online
            Player player = Bukkit.getPlayer(playerName);
            if (player != null && player.isOnline()) {
                player.sendMessage("§cYou have been temporarily muted: " + reason);
                player.sendMessage("§cDuration: " + duration);
            }
            
            // Log the action
            plugin.getStorageManager().logModAction(
                    moderator,
                    "TEMP_MUTE",
                    playerName,
                    reason + " (Duration: " + duration + ")"
            );
            
            return gson.toJson(Map.of(
                    "success", true,
                    "message", "Player " + playerName + " has been temporarily muted for " + duration
            ));
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error adding temp mute: " + e.getMessage(), e);
            response.status(500);
            return gson.toJson(Map.of("error", "Internal server error"));
        }
    }
    
    /**
     * Remove a mute from a player
     */
    public Object removeMute(Request request, Response response) {
        try {
            String playerName = request.params(":player");
            String moderator = request.attribute("username");
            
            if (playerName == null || playerName.isEmpty()) {
                response.status(400);
                return gson.toJson(Map.of("error", "Player name is required"));
            }
            
            // Get player
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
            
            // Check if player exists
            if (!offlinePlayer.hasPlayedBefore()) {
                response.status(404);
                return gson.toJson(Map.of("error", "Player not found"));
            }
            
            // Check if player is muted
            if (!plugin.getPunishmentManager().isMuted(offlinePlayer.getUniqueId())) {
                response.status(400);
                return gson.toJson(Map.of("error", "Player is not muted"));
            }
            
            // Unmute player
            plugin.getPunishmentManager().unmutePlayer(offlinePlayer.getUniqueId());
            
            // Notify if online
            Player player = Bukkit.getPlayer(playerName);
            if (player != null && player.isOnline()) {
                player.sendMessage("§aYou have been unmuted.");
            }
            
            // Log the action
            plugin.getStorageManager().logModAction(
                    moderator,
                    "UNMUTE",
                    playerName,
                    "Unmuted by " + moderator
            );
            
            return gson.toJson(Map.of(
                    "success", true,
                    "message", "Player " + playerName + " has been unmuted"
            ));
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error removing mute: " + e.getMessage(), e);
            response.status(500);
            return gson.toJson(Map.of("error", "Internal server error"));
        }
    }
    
    /**
     * Warn a player
     */
    public Object warnPlayer(Request request, Response response) {
        try {
            String playerName = request.params(":player");
            Map<String, Object> body = gson.fromJson(request.body(), Map.class);
            String reason = (String) body.get("reason");
            String moderator = request.attribute("username");
            
            if (playerName == null || playerName.isEmpty()) {
                response.status(400);
                return gson.toJson(Map.of("error", "Player name is required"));
            }
            
            if (reason == null || reason.isEmpty()) {
                reason = "No reason provided";
            }
            
            // Get player
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
            
            // Check if player exists
            if (!offlinePlayer.hasPlayedBefore()) {
                response.status(404);
                return gson.toJson(Map.of("error", "Player not found"));
            }
            
            // Add warning
            int warningCount = plugin.getPunishmentManager().warnPlayer(offlinePlayer.getUniqueId(), reason);
            
            // Notify if online
            Player player = Bukkit.getPlayer(playerName);
            if (player != null && player.isOnline()) {
                player.sendMessage("§cYou have been warned: " + reason);
                player.sendMessage("§cThis is warning #" + warningCount);
            }
            
            // Log the action
            plugin.getStorageManager().logModAction(
                    moderator,
                    "WARNING",
                    playerName,
                    "Warning #" + warningCount + ": " + reason
            );
            
            return gson.toJson(Map.of(
                    "success", true,
                    "message", "Player " + playerName + " has been warned (Warning #" + warningCount + ")",
                    "warningCount", warningCount
            ));
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error warning player: " + e.getMessage(), e);
            response.status(500);
            return gson.toJson(Map.of("error", "Internal server error"));
        }
    }
    
    /**
     * Get punishment history for a player
     */
    public Object getPlayerHistory(Request request, Response response) {
        try {
            String playerName = request.params(":player");
            
            if (playerName == null || playerName.isEmpty()) {
                response.status(400);
                return gson.toJson(Map.of("error", "Player name is required"));
            }
            
            // Get player
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
            
            // Check if player exists
            if (!offlinePlayer.hasPlayedBefore()) {
                response.status(404);
                return gson.toJson(Map.of("error", "Player not found"));
            }
            
            // Get player punishment history
            List<ModAction> modActions = plugin.getStorageManager().getPlayerModActions(playerName);
            
            // Convert to punishment objects
            List<Map<String, Object>> history = new ArrayList<>();
            
            for (ModAction action : modActions) {
                Map<String, Object> entry = new HashMap<>();
                entry.put("timestamp", action.getTimestamp());
                entry.put("moderator", action.getModerator());
                entry.put("action", action.getAction());
                entry.put("reason", action.getDetails());
                entry.put("active", false); // Need to check if active
                
                if (action.getAction().equals("BAN") && Bukkit.getBanList(BanList.Type.NAME).isBanned(playerName)) {
                    entry.put("active", true);
                } else if (action.getAction().equals("MUTE") && 
                           plugin.getPunishmentManager().isMuted(offlinePlayer.getUniqueId())) {
                    entry.put("active", true);
                }
                
                history.add(entry);
            }
            
            return gson.toJson(Map.of(
                    "success", true,
                    "history", history
            ));
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error getting player history: " + e.getMessage(), e);
            response.status(500);
            return gson.toJson(Map.of("error", "Internal server error"));
        }
    }
} 