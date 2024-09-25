package ru.dushkinmir.absolutlyRandom.events;

import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class RandomMessageEvent extends JavaPlugin {
    private static final List<String> MESSAGES = new ArrayList<>();
    private static final Random RANDOM = new Random();
    private static final int MAX_MESSAGE_COUNT = 3;
    private static final long TASK_INTERVAL_TICKS = 20 * 4;

    static {
        reloadMessages();
    }

    public static void triggerRandomMessageEvent(Plugin plugin) {
        List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
        if (onlinePlayers.isEmpty()) return;

        Player randomPlayer = getRandomPlayerFromList(onlinePlayers);
        scheduleRandomMessagesTask(plugin, randomPlayer);
    }

    private static void reloadMessages() {
        Plugin plugin = Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("AbsolutelyRandom"));
        FileConfiguration config = plugin.getConfig();
        List<String> configMessages = config.getStringList("random-messages");

        if (!configMessages.isEmpty()) {
            MESSAGES.clear();
            MESSAGES.addAll(configMessages);
        }
    }

    private static Player getRandomPlayerFromList(List<Player> players) {
        int randomIndex = RANDOM.nextInt(players.size());
        return players.get(randomIndex);
    }

    private static void scheduleRandomMessagesTask(Plugin plugin, Player player) {
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
            if (messageCount < MAX_MESSAGE_COUNT) {
                showRandomMessageToPlayer();
                messageCount++;
            } else {
                reloadMessages();
                this.cancel();
            }
        }

        private void showRandomMessageToPlayer() {
            String randomMessage = MESSAGES.get(RANDOM.nextInt(MESSAGES.size()));
            var titleText = Component.text("вова пидор", NamedTextColor.GRAY, TextDecoration.OBFUSCATED);
            var subTitleText = Component.text(randomMessage, NamedTextColor.GOLD);

            player.showTitle(Title.title(titleText, subTitleText));
            player.sendActionBar(titleText);
            MESSAGES.remove(randomMessage);
        }
    }
}