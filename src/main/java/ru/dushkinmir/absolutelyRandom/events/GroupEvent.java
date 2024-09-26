package ru.dushkinmir.absolutelyRandom.events;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class GroupEvent {

    private static final String EVENT_STARTING_MESSAGE = "ГРУППОВАЯ МАСТУРБАЦИЯ НАЧНЕТСЯ ЧЕРЕЗ...";
    private static final String EVENT_END_MESSAGE = "ГРУППОВАЯ МАСТУРБАЦИЯ ОКОНЧЕНА, СПАСИБО ЗА УЧАСТИЕ";
    private static final int COUNTDOWN_SECONDS = 5;
    private static final int DURATION_SECONDS = 60; // Множитель на 4 для 15 секунд

    private static boolean eventActive = false;

    public static void triggerGroupEvent(Plugin plugin) {
        if (eventActive) return;

        List<Player> players = getOnlinePlayers();
        if (players.isEmpty()) return;

        eventActive = true;
        Bukkit.broadcast(Component.text(EVENT_STARTING_MESSAGE, NamedTextColor.RED));

        new EventCountdownTask(plugin, players).runTaskTimer(plugin, 0L, 20L);
    }

    private static List<Player> getOnlinePlayers() {
        return new ArrayList<>(Bukkit.getOnlinePlayers());
    }

    private static class EventCountdownTask extends BukkitRunnable {
        private final Plugin plugin;
        private final List<Player> players;
        private int countdown = COUNTDOWN_SECONDS;

        public EventCountdownTask(Plugin plugin, List<Player> players) {
            this.plugin = plugin;
            this.players = players;
        }

        @Override
        public void run() {
            if (countdown > 0) {
                Bukkit.broadcast(Component.text(countdown + "...", NamedTextColor.RED));
                countdown--;
            } else {
                new FallingBlocksTask(plugin, players).runTaskTimer(plugin, 0L, 5L);
                this.cancel();
            }
        }
    }

    private static class FallingBlocksTask extends BukkitRunnable {
        private final Plugin plugin;
        private final List<Player> players;
        private int duration = DURATION_SECONDS;

        public FallingBlocksTask(Plugin plugin, List<Player> players) {
            this.plugin = plugin;
            this.players = players;
        }

        @Override
        public void run() {
            if (duration > 0) {
                for (Player player : players) {
                    dropItemNearPlayer(player, plugin);
                }
                duration--;
            } else {
                Bukkit.broadcast(Component.text(EVENT_END_MESSAGE, NamedTextColor.DARK_GREEN));
                eventActive = false;
                this.cancel();
            }
        }

        private void dropItemNearPlayer(Player player, Plugin plugin) {
            ItemStack item = new ItemStack(Material.WHITE_WOOL, 1);
            Vector direction = player.getEyeLocation().getDirection().normalize();
            var droppedItem = player.getWorld().dropItem(player.getLocation().add(direction.multiply(1.5)), item);

            new BukkitRunnable() {
                @Override
                public void run() {
                    droppedItem.remove();
                }
            }.runTaskLater(plugin, 9L);
        }
    }
}