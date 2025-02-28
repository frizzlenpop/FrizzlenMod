package org.frizzlenpop.frizzlenMod.managers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenMod.FrizzlenMod;

import java.util.*;

public class JailManager {
    private final FrizzlenMod plugin;
    private final Map<String, Location> jails;
    private final Map<UUID, String> jailedPlayers;
    private final Map<UUID, Long> tempJailedPlayers;
    private final Map<UUID, Location> previousLocations;
    private final double JAIL_RADIUS = 10.0; // Default radius around a jail point

    public JailManager(FrizzlenMod plugin) {
        this.plugin = plugin;
        this.jails = new HashMap<>();
        this.jailedPlayers = new HashMap<>();
        this.tempJailedPlayers = new HashMap<>();
        this.previousLocations = new HashMap<>();
        
        // Load jails from file
        loadJails();
        
        // Start jail timer
        startJailTimer();
    }
    
    private void loadJails() {
        FileConfiguration config = plugin.getStorageManager().getJailsConfig();
        
        // Load jail locations
        ConfigurationSection jailsSection = config.getConfigurationSection("jails");
        if (jailsSection != null) {
            for (String jailName : jailsSection.getKeys(false)) {
                String worldName = config.getString("jails." + jailName + ".world");
                double x = config.getDouble("jails." + jailName + ".x");
                double y = config.getDouble("jails." + jailName + ".y");
                double z = config.getDouble("jails." + jailName + ".z");
                float yaw = (float) config.getDouble("jails." + jailName + ".yaw");
                float pitch = (float) config.getDouble("jails." + jailName + ".pitch");
                
                if (worldName != null && Bukkit.getWorld(worldName) != null) {
                    Location location = new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
                    jails.put(jailName.toLowerCase(), location);
                }
            }
        }
        
        // Load jailed players
        ConfigurationSection jailedSection = config.getConfigurationSection("jailed-players");
        if (jailedSection != null) {
            for (String uuidString : jailedSection.getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uuidString);
                    String jailName = config.getString("jailed-players." + uuidString + ".jail");
                    
                    if (jailName != null && jails.containsKey(jailName.toLowerCase())) {
                        jailedPlayers.put(uuid, jailName.toLowerCase());
                        
                        // Check if it's a temp jail
                        if (config.contains("jailed-players." + uuidString + ".expiry")) {
                            long expiryTime = config.getLong("jailed-players." + uuidString + ".expiry");
                            if (expiryTime > System.currentTimeMillis()) {
                                tempJailedPlayers.put(uuid, expiryTime);
                            } else {
                                // Expired jail, remove it
                                jailedPlayers.remove(uuid);
                            }
                        }
                        
                        // Load previous location
                        if (config.contains("jailed-players." + uuidString + ".previous-location")) {
                            String prevWorldName = config.getString("jailed-players." + uuidString + ".previous-location.world");
                            double prevX = config.getDouble("jailed-players." + uuidString + ".previous-location.x");
                            double prevY = config.getDouble("jailed-players." + uuidString + ".previous-location.y");
                            double prevZ = config.getDouble("jailed-players." + uuidString + ".previous-location.z");
                            float prevYaw = (float) config.getDouble("jailed-players." + uuidString + ".previous-location.yaw");
                            float prevPitch = (float) config.getDouble("jailed-players." + uuidString + ".previous-location.pitch");
                            
                            if (prevWorldName != null && Bukkit.getWorld(prevWorldName) != null) {
                                Location prevLocation = new Location(Bukkit.getWorld(prevWorldName), prevX, prevY, prevZ, prevYaw, prevPitch);
                                previousLocations.put(uuid, prevLocation);
                            }
                        }
                    }
                } catch (IllegalArgumentException ignored) {}
            }
        }
    }
    
    public void saveJails() {
        FileConfiguration config = plugin.getStorageManager().getJailsConfig();
        
        // Save jail locations
        config.set("jails", null); // Clear existing jails
        for (Map.Entry<String, Location> entry : jails.entrySet()) {
            Location loc = entry.getValue();
            config.set("jails." + entry.getKey() + ".world", loc.getWorld().getName());
            config.set("jails." + entry.getKey() + ".x", loc.getX());
            config.set("jails." + entry.getKey() + ".y", loc.getY());
            config.set("jails." + entry.getKey() + ".z", loc.getZ());
            config.set("jails." + entry.getKey() + ".yaw", loc.getYaw());
            config.set("jails." + entry.getKey() + ".pitch", loc.getPitch());
        }
        
        // Save jailed players
        config.set("jailed-players", null); // Clear existing data
        for (Map.Entry<UUID, String> entry : jailedPlayers.entrySet()) {
            UUID uuid = entry.getKey();
            String uuidString = uuid.toString();
            String jailName = entry.getValue();
            
            config.set("jailed-players." + uuidString + ".jail", jailName);
            
            // Save expiry time if temp jailed
            if (tempJailedPlayers.containsKey(uuid)) {
                config.set("jailed-players." + uuidString + ".expiry", tempJailedPlayers.get(uuid));
            }
            
            // Save previous location if available
            if (previousLocations.containsKey(uuid)) {
                Location prevLoc = previousLocations.get(uuid);
                config.set("jailed-players." + uuidString + ".previous-location.world", prevLoc.getWorld().getName());
                config.set("jailed-players." + uuidString + ".previous-location.x", prevLoc.getX());
                config.set("jailed-players." + uuidString + ".previous-location.y", prevLoc.getY());
                config.set("jailed-players." + uuidString + ".previous-location.z", prevLoc.getZ());
                config.set("jailed-players." + uuidString + ".previous-location.yaw", prevLoc.getYaw());
                config.set("jailed-players." + uuidString + ".previous-location.pitch", prevLoc.getPitch());
            }
        }
        
        plugin.getStorageManager().saveJailsConfig();
    }
    
    private void startJailTimer() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            long currentTime = System.currentTimeMillis();
            Iterator<Map.Entry<UUID, Long>> iterator = tempJailedPlayers.entrySet().iterator();
            
            while (iterator.hasNext()) {
                Map.Entry<UUID, Long> entry = iterator.next();
                if (entry.getValue() <= currentTime) {
                    UUID uuid = entry.getKey();
                    // Release from jail
                    unjailPlayer(uuid);
                    iterator.remove();
                    
                    // Notify player if online
                    Player player = Bukkit.getPlayer(uuid);
                    if (player != null && player.isOnline()) {
                        player.sendMessage("§aYou have been released from jail!");
                    }
                }
            }
        }, 20L * 60, 20L * 60); // Check every minute
    }
    
    public boolean createJail(String name, Location location) {
        name = name.toLowerCase();
        jails.put(name, location.clone());
        saveJails();
        return true;
    }
    
    public boolean deleteJail(String name) {
        name = name.toLowerCase();
        if (jails.containsKey(name)) {
            jails.remove(name);
            
            // Release any players in this jail
            Iterator<Map.Entry<UUID, String>> iterator = jailedPlayers.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<UUID, String> entry = iterator.next();
                if (entry.getValue().equals(name)) {
                    unjailPlayer(entry.getKey());
                    iterator.remove();
                }
            }
            
            saveJails();
            return true;
        }
        return false;
    }
    
    public boolean jailExists(String name) {
        return jails.containsKey(name.toLowerCase());
    }
    
    public Map<String, Location> getJails() {
        return new HashMap<>(jails);
    }
    
    public boolean jailPlayer(Player player, String jailName) {
        jailName = jailName.toLowerCase();
        if (!jails.containsKey(jailName)) {
            return false;
        }
        
        // Save their current location before teleporting
        previousLocations.put(player.getUniqueId(), player.getLocation().clone());
        
        // Teleport to jail
        player.teleport(jails.get(jailName));
        
        // Add to jailed players
        jailedPlayers.put(player.getUniqueId(), jailName);
        
        // Save to file
        saveJails();
        return true;
    }
    
    public boolean tempJailPlayer(Player player, String jailName, long durationMinutes) {
        if (jailPlayer(player, jailName)) {
            // Set expiry time
            long expiryTime = System.currentTimeMillis() + (durationMinutes * 60 * 1000);
            tempJailedPlayers.put(player.getUniqueId(), expiryTime);
            saveJails();
            return true;
        }
        return false;
    }
    
    public boolean unjailPlayer(UUID playerUUID) {
        if (jailedPlayers.containsKey(playerUUID)) {
            // Remove from jail lists
            jailedPlayers.remove(playerUUID);
            tempJailedPlayers.remove(playerUUID);
            
            // Teleport back if player is online and we have a previous location
            Player player = Bukkit.getPlayer(playerUUID);
            if (player != null && player.isOnline() && previousLocations.containsKey(playerUUID)) {
                player.teleport(previousLocations.get(playerUUID));
                previousLocations.remove(playerUUID);
            }
            
            saveJails();
            return true;
        }
        return false;
    }
    
    public boolean isPlayerJailed(UUID playerUUID) {
        return jailedPlayers.containsKey(playerUUID);
    }
    
    public String getPlayerJail(UUID playerUUID) {
        return jailedPlayers.get(playerUUID);
    }
    
    /**
     * Gets the location of a jail by name
     * 
     * @param jailName The name of the jail
     * @return The location of the jail, or null if it doesn't exist
     */
    public Location getJailLocation(String jailName) {
        return jails.get(jailName.toLowerCase());
    }
    
    /**
     * Teleports a player to a jail
     * 
     * @param player The player to teleport
     * @param jailName The name of the jail
     * @return true if successful, false if the jail doesn't exist
     */
    public boolean teleportToJail(Player player, String jailName) {
        jailName = jailName.toLowerCase();
        if (jails.containsKey(jailName)) {
            player.teleport(jails.get(jailName));
            return true;
        }
        return false;
    }
    
    /**
     * Checks if a player is within the area of their assigned jail
     * 
     * @param player The player to check
     * @return true if the player is within a jail area, false if not
     */
    public boolean isInJailArea(Player player) {
        if (!isPlayerJailed(player.getUniqueId())) {
            return false;
        }
        
        String jailName = jailedPlayers.get(player.getUniqueId());
        Location jailLocation = jails.get(jailName);
        
        if (jailLocation == null) {
            return false;
        }
        
        // Check if player is in the same world as the jail
        if (!player.getWorld().equals(jailLocation.getWorld())) {
            return false;
        }
        
        // Get jail radius from config or use default
        double radius = plugin.getConfig().getDouble("jail.radius", JAIL_RADIUS);
        
        // Check if player is within the radius of the jail
        return player.getLocation().distance(jailLocation) <= radius;
    }
    
    /**
     * Checks if a location is within any jail area
     * 
     * @param location The location to check
     * @return true if the location is within a jail area, false if not
     */
    public boolean isLocationInJail(Location location) {
        if (location == null) {
            return false;
        }
        
        // Get jail radius from config or use default
        double radius = plugin.getConfig().getDouble("jail.radius", JAIL_RADIUS);
        
        // Check each jail
        for (Location jailLocation : jails.values()) {
            if (location.getWorld().equals(jailLocation.getWorld()) && 
                    location.distance(jailLocation) <= radius) {
                return true;
            }
        }
        
        return false;
    }
} 