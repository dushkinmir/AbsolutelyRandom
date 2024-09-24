package ru.dushkinmir.absolutlyRandom.events;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class RandomMessageEvent extends JavaPlugin {
    private static final List<String> MESSAGES = new ArrayList<>();
    private static final Random RANDOM = new Random();
    private static final int MAX_MESSAGES_COUNT = 3;
    private static final long TASK_INTERVAL_TICKS = 40L;

    static {
        loadMessages();
    }

    public static void triggerRandomMessage(Plugin plugin) {
        List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
        if (onlinePlayers.isEmpty()) return;

        Player selectedPlayer = getRandomPlayer(onlinePlayers);
        sendRandomMessages(plugin, selectedPlayer);
    }

    private static void loadMessages() {
        Plugin plugin = Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("AbsolutelyRandom"));
        FileConfiguration config = plugin.getConfig();

        List<String> configMessages = config.getStringList("random-messages");
        if (!configMessages.isEmpty()) {
            MESSAGES.clear();
            MESSAGES.addAll(configMessages);
        }
    }

    private static Player getRandomPlayer(List<Player> players) {
        int randomIndex = RANDOM.nextInt(players.size());
        return players.get(randomIndex);
    }

    private static void sendRandomMessages(Plugin plugin, Player player) {
        new MessageTask(player).runTaskTimer(plugin, 0L, TASK_INTERVAL_TICKS);
    }

    private static class MessageTask extends BukkitRunnable {
        private final Player player;
        private int messageCount;

        public MessageTask(Player player) {
            this.player = player;
            this.messageCount = 0;
        }

        @Override
        public void run() {
            if (messageCount < MAX_MESSAGES_COUNT) {
                String randomMessage = MESSAGES.get(RANDOM.nextInt(MESSAGES.size()));
                String formattedMessage = String.format("[SERVER] %s", randomMessage);
                player.sendMessage(Component.text(formattedMessage, NamedTextColor.YELLOW));
                messageCount++;
            } else {
                this.cancel();
            }
        }
    }
}