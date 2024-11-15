package ru.dushkinmir.absolutelyRandom.betters;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerChatHandler implements Listener {

    private final JavaPlugin plugin;

    public PlayerChatHandler(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        // Check if the player is holding the special item (radio)
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (itemInHand != null && itemInHand.getType() == Material.NOTE_BLOCK) { // "NOTE_BLOCK" as the radio
            event.setFormat(player.getName() + ChatColor.RESET + ": " + message);
            return; // Let the chat message go through as usual
        }

        // Cancel the original chat event to prevent it from being broadcast
        event.setCancelled(true);

        // Display the message above the player's head
        player.setCustomName(ChatColor.YELLOW + player.getName() + ChatColor.RESET + ": " + message);
        player.setCustomNameVisible(true);

        // Calculate the display time (n)
        int displayTime = message.length() > 15 ? (int) (message.length() * 1.5) : 15;

        // Schedule task to reset the player's name after n seconds
        new BukkitRunnable() {
            @Override
            public void run() {
                player.setCustomName(player.getName());
                player.setCustomNameVisible(false);
            }
        }.runTaskLater(plugin, displayTime * 20L); // Convert seconds to ticks
    }
}
