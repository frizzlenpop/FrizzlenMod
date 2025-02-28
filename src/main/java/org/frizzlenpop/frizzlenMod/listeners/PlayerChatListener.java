package org.frizzlenpop.frizzlenMod.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.frizzlenpop.frizzlenMod.FrizzlenMod;
import org.frizzlenpop.frizzlenMod.utils.MessageUtils;

import java.util.HashMap;
import java.util.UUID;

public class PlayerChatListener implements Listener {
    
    private final FrizzlenMod plugin;
    private final HashMap<UUID, Long> lastChatTimes;
    
    public PlayerChatListener(FrizzlenMod plugin) {
        this.plugin = plugin;
        this.lastChatTimes = new HashMap<>();
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        
        // Check if global chat is muted
        if (plugin.getChatManager().isChatMuted()) {
            // Staff can still chat when global chat is muted
            if (!player.hasPermission("frizzlenmod.bypass.chatmute")) {
                event.setCancelled(true);
                MessageUtils.sendErrorMessage(player, "The global chat is currently muted.");
                return;
            }
        }
        
        // Check if the player is muted
        if (plugin.getPunishmentManager().isPlayerMuted(player.getUniqueId())) {
            event.setCancelled(true);
            MessageUtils.sendErrorMessage(player, "You are muted and cannot chat.");
            return;
        }
        
        // Check slow mode
        int slowModeSeconds = plugin.getChatManager().getSlowModeSeconds();
        if (slowModeSeconds > 0 && !player.hasPermission("frizzlenmod.bypass.slowmode")) {
            long currentTime = System.currentTimeMillis();
            long lastChat = lastChatTimes.getOrDefault(player.getUniqueId(), 0L);
            long elapsedTime = (currentTime - lastChat) / 1000; // Convert to seconds
            
            if (elapsedTime < slowModeSeconds) {
                event.setCancelled(true);
                int remainingTime = slowModeSeconds - (int) elapsedTime;
                MessageUtils.sendErrorMessage(player, "Slow mode is enabled. You can chat again in " + 
                        remainingTime + " second" + (remainingTime == 1 ? "" : "s") + ".");
                return;
            }
            
            // Update last chat time
            lastChatTimes.put(player.getUniqueId(), currentTime);
        }
        
        // Optional: Chat filtering (if implemented in the ChatManager)
        if (plugin.getChatManager().hasFilters()) {
            String filteredMessage = plugin.getChatManager().filterMessage(event.getMessage());
            event.setMessage(filteredMessage);
        }
    }
} 